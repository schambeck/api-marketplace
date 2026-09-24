# PLAN-004 — Reserva, alocação e consumo de inventory

**Prioridade:** MVP essencial, com estoque pertencente diretamente ao seller.

**Issue:** [#6](https://github.com/schambeck/api-marketplace/issues/6)

**PRD:** [`PRD-004`](../prd/PRD-004-inventory.md) · **Dependências:** PRD-001 e PRD-002. O `PLAN-003` somente será necessário quando houver múltiplas fontes de fulfillment.

## 1. Objetivo e estado atual

O catálogo atual usa `Product.quantity` e `Product.inventoryStatus` como campos editáveis. A publicação e o início do checkout consultam diretamente essa quantidade; o pagamento, por sua vez, referencia apenas `Cart`. Não existe pedido/linha de pedido, reserva, allocation, expiração ou trilha de operações.

O alvo deste plano é garantir que a quantidade comercializável seja controlada por uma operação transacional e idempotente, vinculada à linha de pedido, sem permitir overselling. O fluxo mínimo será:

```text
cart -> order/order_item -> reservation -> allocation -> delivered consumption
                                      \\-> release/expiration
```

## 2. Decisões de MVP

### 2.1 Granularidade

Usar **saldo quantitativo decimal** por oferta/produto, não uma linha por unidade nem códigos individuais. A escala será `decimal(12,2)`, compatível com `Product.quantity` e `CartItem.quantity`; quantidades devem ser positivas e não podem exceder duas casas decimais.

Cada produto seller-owned terá um único `inventory_balance`, compartilhado por todas as suas ofertas. A migração inicial copiará `Product.quantity` para `available_quantity`, com `reserved_quantity`, `allocated_quantity` e `consumed_quantity` iniciando em zero; isso evita duplicar o mesmo estoque quando o produto aparece em mais de uma oferta.

### 2.2 Modelo de estado

Os estados públicos de inventory serão `AVAILABLE`, `RESERVED`, `ALLOCATED`, `CONSUMED`, `RELEASED` e `EXPIRED`.

- `InventoryBalance` representa o saldo agregado do produto seller-owned; a oferta da linha do pedido identifica a apresentação comercial, mas não cria um segundo saldo.
- `InventoryReservation` representa a quantidade reservada para uma `OrderItem`, seu `product_id`, `seller_id`, prazo e estado.
- `InventoryAllocation` representa a quantidade promovida da reserva para o fulfillment do seller. No MVP ela referencia somente a reservation; não há `source_type` nem `source_reference`.
- `InventoryMovement` é o ledger imutável/auditável de cada mudança de quantidade e estado.

`AVAILABLE` será o saldo livre calculado como quantidade total menos as quantidades atualmente reservadas, alocadas e consumidas; esses buckets são mutuamente exclusivos e a transição move a quantidade de um bucket para outro. Não haverá reserva persistida por unidade. `RELEASED` e `EXPIRED` serão estados terminais de uma reserva, e o saldo liberado retornará ao disponível. `CONSUMED` somente poderá ser criado para quantidade alocada e confirmada como entregue.

### 2.3 Concorrência, oversell e TTL

- Usar locking pessimista PostgreSQL (`SELECT ... FOR UPDATE`) no `inventory_balance` durante reserva, release, allocation e consumo. O lock deve ser adquirido antes da validação do saldo e mantido até o commit.
- Não haverá oversell: quantidade solicitada maior que o saldo livre falhará deterministicamente com erro de domínio e não alterará o saldo.
- O TTL padrão da reserva será configurável em `inventory.reservation-ttl` e começará em **30 minutos**. A criação da reserva grava `expires_at` no banco; não dependerá do relógio do cliente.
- Um job periódico, em lotes pequenos e transacionais, expirará reservas vencidas. Também haverá operação administrativa idempotente para recuperação operacional quando o scheduler estiver indisponível.

### 2.4 Idempotência

Toda operação mutável receberá uma `operation_key` estável. Uma tabela de operações, com índice único por `(reservation_id, operation_type, operation_key)`, armazenará o resultado e impedirá duplicação de reserva, allocation, release, expiration ou consumo. Repetições devem devolver o mesmo resultado, sem novo movimento.

### 2.5 Limite com pedido e pagamento

Este plano não cria `Order/OrderItem`, não altera `Payment` e não implementa webhook. O `PLAN-005` deverá chamar `reserve` durante o checkout, depois de criar o `OrderItem` e antes de solicitar a preferência Mercado Pago. O inventory recebe apenas o identificador da linha de pedido (`orderItemId`) como referência de integração.

O `PLAN-006` deverá validar o pagamento e disparar a próxima transição de inventory, normalmente `allocate` após pagamento aprovado. O `PLAN-007` consumirá allocation para criar o fulfillment; a confirmação de entrega chamará `consumeDelivered`. Nenhum desses fluxos será implementado aqui.

## 3. Persistência e migrações

Criar a próxima migration Flyway, sem editar migrations aplicadas, contendo:

1. `inventory_balance`, com `product_id` único, quantidades não negativas, `version` e auditoria;
2. `inventory_reservation`, com `order_item_id` como referência externa, quantidade, status, `expires_at`, `operation_key`, timestamps e índices para reservas ativas/vencidas;
3. `inventory_allocation`, com reservation, quantidade, estado e chave de idempotência;
4. `inventory_movement`, append-only, com operação, estado anterior/novo, quantidade, ator, origem, chave idempotente e data;
5. backfill de um `inventory_balance` por produto elegível, usando a quantidade atual de `product`.

As constraints devem impedir quantidades negativas, allocations/consumos acima da reserva e mais de uma reserva ativa incompatível para a mesma operação. A migration deve ser testada sobre banco limpo e sobre uma base que já tenha as migrations V1–V14.

`Product.quantity` será mantido inicialmente como projeção de compatibilidade, atualizada na mesma transação do inventory. Nenhum novo fluxo poderá alterá-lo diretamente para controlar estoque. Consultas de publicação/oferta/checkout deverão consultar o `inventory_balance` do produto; a remoção definitiva dos campos fica fora deste plano.

## 4. Código e limites de módulo

Adicionar o pacote `usecase.inventory` com:

- entidades `InventoryBalance`, `InventoryReservation`, `InventoryAllocation`, `InventoryMovement` e enums de estado/tipo;
- repositories com consulta pessimista para saldo e consultas de reservas vencidas/ativas;
- `InventoryService` transacional para `reserve`, `allocate`, `release`, `expire` e `consumeDelivered`;
- validação de transições, quantidade, TTL, ownership do seller e resultado idempotente;
- exceções de domínio mapeadas para respostas HTTP determinísticas;
- DTOs de operação e histórico sem expor códigos ou detalhes internos da fonte.

Alterar os seguintes pontos existentes:

- `OfferService`/`OfferRepository`: derivar disponibilidade do saldo livre do inventory, inclusive `validateForCheckout`;
- `ProductService`: impedir que edição comum altere estoque sem passar por uma operação administrativa de ajuste auditada; se ajuste de saldo não entrar no MVP, rejeitar alteração de `quantity` no endpoint existente;
- `SecurityConfig` e controllers: manter leitura pública do catálogo/ofertas e restringir operações de inventory/pedido/fulfillment ao usuário autenticado, seller dono ou `ADMIN`.

APIs operacionais mínimas, preferencialmente sob `/inventory`, serão:

- `POST /inventory/reservations` — reservar uma linha de pedido com `operationKey`;
- `POST /inventory/reservations/{id}/allocate` — promover a reserva para fulfillment do seller;
- `POST /inventory/reservations/{id}/release` — liberar saldo não consumido;
- `POST /inventory/reservations/{id}/consume` — registrar quantidade efetivamente entregue;
- `POST /inventory/reservations/{id}/expire` — fallback operacional idempotente;
- `GET /inventory/orders/{orderId}` — consultar estado resumido e histórico autorizado.

Os endpoints podem ser internos ao fluxo de checkout no primeiro incremento; caso expostos publicamente, devem exigir autenticação, autorização por seller/admin e `operationKey` obrigatório. O contrato e o schema `schema/api-schema-marketplace.json` devem ser atualizados somente para os endpoints efetivamente expostos.

## 5. Sequência de implementação

1. Confirmar TTL configurável, política de ajuste manual, referência externa de `orderItemId` e semântica de `operationKey`.
2. Criar migration, backfill de saldo e constraints do inventory.
3. Implementar repositories com lock pessimista e o ledger idempotente de movimentos.
4. Implementar `InventoryService` com transições, release, expiração e consumo parcial.
5. Atualizar disponibilidade de oferta e projeção legada de produto.
6. Implementar scheduler de expiração e endpoints operacionais.
7. Atualizar schema/API, documentação de configuração e testes.

## 6. Testes e evidências

Adicionar testes versionados, usando Testcontainers PostgreSQL — já disponível no `pom.xml` — e Flyway real:

- concorrência: duas transações tentando reservar mais que o saldo; exatamente uma vence e o saldo nunca fica negativo;
- idempotência: repetir cada operation key não cria nova reserva, allocation, movimento ou consumo;
- saldo insuficiente e quantidade inválida;
- transições inválidas (`CONSUMED` sem allocation, release após consumo, allocation acima da reserva);
- cancelamento/falha libera saldo não consumido;
- timeout expira a reserva e a torna novamente disponível;
- integração recebe `orderItemId` válido e não permite reservation sem essa referência;
- entrega parcial consome somente a quantidade confirmada e mantém o restante alocado;
- autorização: seller não acessa inventory de outro seller e catálogo não expõe histórico operacional indevido;
- migração V1–V14 + nova migration preserva ofertas/produtos e cria saldos coerentes.

Validação obrigatória ao implementar:

```bash
./mvnw clean test -Dmaven.plugin.validation=BRIEF
./mvnw clean package
```

Registrar também a evidência de uma execução concorrente real contra PostgreSQL e, quando o ambiente permitir, uma chamada manual do fluxo checkout → pagamento → fulfillment. Sem PostgreSQL/Testcontainers disponível, os testes unitários não comprovam o critério de não overselling.

## 7. Rastreabilidade dos critérios de aceite

| Critério do PRD-004 | Evidência obrigatória |
|---|---|
| Duas compras concorrentes não reservam a mesma unidade | Teste de integração PostgreSQL com duas transações, lock e saldo final não negativo |
| Reserva expirada é liberada automaticamente ou por processo seguro | Teste do scheduler/job e teste do endpoint operacional idempotente |
| Pedido pago não avança sem reserva/alocação válida | Critério integrado nos PLAN-005/006/007; o PLAN-004 fornece as transições e rejeita allocation sem reservation válida |
| Entrega parcial consome somente o confirmado | Teste de consumo parcial, movimentos e saldos restantes |
| Toda mudança é idempotente e auditável | Testes de repetição de `operationKey` e verificação de `inventory_movement` |

## 8. Riscos, pressupostos e rollback

- **Risco:** a dependência de pedido aumenta o escopo do MVP. **Mitigação:** manter o modelo mínimo, sem catálogo de fulfillment ou múltiplas fontes; deixar roteamento avançado para PLAN-003.
- **Risco:** operações externas do Mercado Pago podem ocorrer entre commits. **Mitigação:** pedido/reserva idempotentes, status intermediários explícitos e reconciliação pelo webhook; nunca consumir por simples criação de preferência.
- **Risco:** edição legada de `Product.quantity` pode divergir do saldo. **Mitigação:** bloquear alteração direta, atualizar a projeção apenas no serviço de inventory e monitorar divergências na migração.
- **Risco:** expiração concorrente com pagamento/cancelamento. **Mitigação:** mesmo lock pessimista, transições condicionais e operação idempotente.
- **Rollback:** migrations são aditivas; em caso de falha, desabilitar novos endpoints/integrações por configuração, manter tabelas novas sem apagar dados e reverter o código em release. Não editar nem desfazer migrations já aplicadas em produção.

## 9. Fora de escopo

Não entram neste plano: inventário por unidade/código serial, múltiplos sellers para a mesma oferta, roteamento para armazéns externos, split de fulfillment, ajuste manual sem auditoria, reconciliação financeira completa, ou remoção imediata dos campos legados de `Product`.
