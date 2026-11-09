# PLAN-005 — Pedido e lifecycle

**Prioridade:** MVP essencial. Criar o agregado comercial que congela a compra entre carrinho, pagamento, inventory e fulfillment.

**Issue:** [#7](https://github.com/schambeck/api-marketplace/issues/7)

**PRD:** [`PRD-005`](../prd/PRD-005-pedido-lifecycle.md) · **Dependências:** PRD-001, PRD-002 e PLAN-004. É a base de PLAN-006, PLAN-007 e PLAN-008.

## 1. Objetivo e estado atual

Hoje `Cart`/`CartItem` são a única representação local da compra. `CreatePreferenceService` lê o carrinho diretamente, envia `Cart.id` como `external_reference` ao Mercado Pago e `PaymentService` resolve esse valor de volta para `Cart`. Não existe pedido, snapshot imutável, transição de estado ou vínculo comercial com inventory/fulfillment.

O alvo é introduzir `Order`/`OrderItem` como registro definitivo da venda:

```text
cart --checkout idempotente--> order + order_items --preference--> payment
                                      |                         |
                                      +--> inventory references  +--> future events
                                      +--> fulfillment/delivery references
```

O carrinho continuará sendo uma origem editável e não será usado como identidade do pagamento ou da venda depois que o pedido for criado.

## 2. Decisões fechadas para o MVP

Estas decisões eliminam as pendências do PRD necessárias para criar a migration:

### 2.1 Unidade de checkout e idempotência

- Cada chamada representa **um pedido por checkout**. O pedido é criado a partir de todos os itens do carrinho do comprador autenticado.
- O cliente envia `Idempotency-Key` (header HTTP) estável para a tentativa de checkout. A chave é persistida em `order.checkout_key` e é única por comprador; repetir a mesma chave devolve o mesmo pedido, sem duplicar linhas, reserva ou preferência.
- A ausência da chave será rejeitada com `400`; não haverá deduplicação baseada somente em `Cart.id`, porque o carrinho pode ser reutilizado em outro checkout.
- O checkout deve congelar o pedido antes de chamar o gateway. Se a criação da preferência falhar, o pedido permanece `CREATED` (com a reserva tratada de forma compensatória) e uma nova chamada com a mesma chave pode tentar concluir a integração sem recriar o agregado.

### 2.2 Estados e transições

O enum persistido conterá todos os estados exigidos pelo PRD: `CREATED`, `PAYMENT_PENDING`, `PAID`, `REJECTED`, `FULFILLING`, `DELIVERED`, `COMPLETED`, `CANCELLED`, `EXPIRED`, `REFUNDED`, `DISPUTED` e `PARTIALLY_DELIVERED`.

No primeiro incremento, o serviço de lifecycle permitirá somente estas transições:

| De | Para | Origem/causa esperada |
|---|---|---|
| `CREATED` | `PAYMENT_PENDING` | checkout/preference criada |
| `PAYMENT_PENDING` | `PAID` | confirmação aprovada do gateway; detalhada em PLAN-006 |
| `PAYMENT_PENDING` | `REJECTED` | gateway recusou o pagamento |
| `PAYMENT_PENDING` | `EXPIRED` | prazo de pagamento expirado |
| `CREATED`, `PAYMENT_PENDING`, `REJECTED` | `CANCELLED` | cancelamento autorizado |
| `PAID` | `FULFILLING` | fulfillment elegível; detalhada em PLAN-007 |
| `FULFILLING` | `DELIVERED` ou `PARTIALLY_DELIVERED` | entrega registrada; detalhada em PLAN-008 |
| `PARTIALLY_DELIVERED` | `DELIVERED` | entrega restante concluída |
| `DELIVERED` | `COMPLETED` | fechamento pós-entrega |
| `PAID`, `DELIVERED`, `COMPLETED` | `REFUNDED` | estorno confirmado; fora da implementação deste plano |
| `PAID`, `FULFILLING`, `DELIVERED`, `COMPLETED` | `DISPUTED` | disputa/chargeback; fora da implementação deste plano |

As transições ainda não suportadas pelo módulo dependente ficam protegidas por regra explícita e serão habilitadas pelos planos seguintes. Não haverá `setStatus` público. Toda transição exige `cause` e `source`, é registrada em histórico append-only e usa `@Version` para rejeitar concorrência perdida.

### 2.3 Valores, numeração e retenção

- `public_id` será um UUID gerado pela aplicação, único e não sequencial; ele será a referência pública e o `external_reference` do Mercado Pago. O `id` interno continua sendo `serial` apenas para relações locais.
- O MVP terá somente subtotal das linhas. `discount_total`, `tax_total`, `shipping_total` e `fee_total` serão persistidos como zero; `total_amount = subtotal_amount`.
- Taxas do marketplace configuradas no Mercado Pago não alteram o total cobrado do comprador e não serão tratadas como receita do pedido neste plano.
- O snapshot comercial e seu histórico serão retidos indefinidamente por padrão. Não haverá edição nem exclusão física de pedido, linha ou transição; retenção/anonimização regulatória fica para decisão própria.

### 2.4 Snapshot e referências futuras

Cada `OrderItem` congelará `product_id` e `offer_id` como referências históricas, além de nome/descrição do produto, contexto da oferta, seller, método de entrega, preço unitário, quantidade, subtotal e um `snapshot` JSON versionado para atributos adicionais. O pedido congelará comprador, contato disponível no cadastro no momento do checkout e instruções aplicáveis; se não houver instrução fornecida pelo carrinho/cliente, o campo será `null` e isso será documentado, sem inventar dados.

As referências a payment, inventory, fulfillment e delivery serão inicialmente nullable e idempotentes (`payment_id`, chaves/IDs externos e/ou `*_reference`), pois os agregados dos planos seguintes ainda não existem. Não serão criadas FKs para tabelas futuras nem cópias mutáveis de seus estados. A referência será preenchida pelo caso de uso responsável e mantida no pedido para consulta e reconciliação.

## 3. Persistência e migration

Criar a próxima migration Flyway, sem editar V1–V17:

1. `order`: `id`, `public_id` UUID único, `buyer_id`, `cart_id` nullable como origem histórica, `checkout_key`, `status`, totais monetários, snapshot de comprador/instruções, referências externas a payment/inventory/fulfillment/delivery, `version` e timestamps.
2. `order_item`: `id`, `order_id`, referências históricas de product/offer, seller, dados comerciais congelados, quantidade, preço, subtotal, `snapshot_version`, snapshot JSON, `version` e timestamps.
3. `order_status_history`: `id`, `order_id`, `from_status`, `to_status`, `cause`, `source`, `actor`, `idempotency_key` opcional, metadata JSON e `created_at`; tabela append-only com índice por pedido/data e chave única para repetição da mesma transição.

As constraints devem exigir valores monetários não negativos, quantidade positiva, `public_id` único, `checkout_key` único por comprador e relações obrigatórias entre pedido e linhas. O `cart_id` não será único, pois uma origem pode aparecer em tentativas distintas com chaves diferentes, sujeitas às regras de negócio e ao histórico.

Se o PostgreSQL exigir um nome reservado para `order`, usar o identificador quoted consistente com as migrations existentes (`"order"`) e mapear explicitamente com `@Table`. A migration deve funcionar em banco limpo e após V1–V17.

## 4. Código, APIs e limites de módulo

Adicionar `usecase.order` com:

- `Order`, `OrderItem`, `OrderStatus`, `OrderStatusHistory`;
- repositories para pedido do comprador, busca por `public_id`/`checkout_key` e histórico;
- `OrderService` transacional para criar snapshot, resolver pedido idempotente, vincular referências e aplicar transições;
- `OrderTransitionValidator`/tabela de transições, exceção de estado inválido e exceção de conflito/idempotência;
- DTOs `OrderWeb`, `OrderItemWeb`, `CheckoutRequest` e resposta de checkout;
- mapper explícito que nunca copie campos de snapshot de volta para entidades de catálogo.

Contrato planejado:

- `POST /orders/checkout` — autenticação obrigatória; exige `Idempotency-Key`, lê o carrinho do usuário, valida itens/ofertas, cria ou recupera o pedido e inicia a reserva do PLAN-004. Retorna `OrderWeb`/referência de checkout; a criação da preferência pode ser delegada ao fluxo atual em uma etapa compatível.
- `GET /orders/{publicId}` — comprador autenticado consulta somente seus pedidos; `ADMIN` pode consultar operação autorizada.
- `GET /orders` — comprador lista seus pedidos paginados, ordenados por `id`/criação; não expõe snapshots de outros compradores.
- operação interna `transition(orderPublicId, target, cause, source, actor, idempotencyKey)` — não exposta como CRUD genérico. Endpoints operacionais dos planos seguintes chamarão esse serviço.

Alterar `CreatePreferenceService`/`CartPreferenceConverter` para consumir `OrderItem` e usar `order.public_id` no `external_reference`. O comportamento público atual de `POST /mercado-pago/preferences` deve permanecer compatível durante a migração: ele passará pelo checkout idempotente e devolverá a preferência associada ao pedido. `PaymentService` deixará de converter `external_reference` para `Cart.id`; PLAN-006 fará a resolução por `Order.public_id` e atualizará o pedido de forma idempotente.

Integrar `InventoryService.reserve` somente depois de persistir `OrderItem`; usar `order_item.id` como referência prevista em PLAN-004 e uma operation key derivada de `order.public_id` + item. Se a reserva falhar, o pedido não poderá avançar para pagamento e a transação deverá deixar um estado recuperável sem preferência criada.

Revisar `SecurityConfig`, `ControllerAdvice` e `schema/api-schema-marketplace.json`. Não abrir endpoints de transição, histórico completo ou dados de instrução para acesso público.

## 5. Sequência de implementação

1. Confirmar o contrato do header `Idempotency-Key`, payload de instruções e campos disponíveis em `User`, `CartItem`, `Offer` e `Product`; registrar a decisão no schema.
2. Criar a migration de pedido, linhas e histórico com constraints, índices e triggers append-only quando aplicável.
3. Implementar entidades/repositories/mapper e testes de persistência contra Flyway real.
4. Implementar criação idempotente do pedido e snapshot transacional a partir do carrinho, incluindo validação de oferta e total.
5. Implementar a matriz de transições, histórico com causa/origem/ator e concorrência otimista.
6. Integrar reservation do PLAN-004 e impedir que preferência seja criada sem pedido/linhas válidos.
7. Migrar preference/payment de `Cart.id` para `Order.public_id`, preservando a rota atual e preparando o contrato do PLAN-006.
8. Expor consulta autenticada de pedidos, atualizar schema/documentação e adicionar testes de autorização.
9. Executar validação Maven, conferir diff/migration e registrar limitações de integração externa.

## 6. Testes e evidências

Adicionar testes versionados; usar PostgreSQL/Testcontainers e Flyway para constraints, locking e JSONB quando o suporte já disponível no `pom.xml` permitir:

- checkout válido cria um `Order` e `OrderItem` com subtotal/total e snapshot completo;
- alterar produto, oferta, preço ou quantidade do carrinho depois do checkout não altera o pedido;
- mesma `Idempotency-Key` concorrente para o mesmo comprador cria exatamente um pedido, linhas, histórico inicial e reservas;
- chave igual para payload incompatível é rejeitada deterministicamente;
- chaves diferentes não são deduplicadas apenas por `Cart.id`;
- carrinho vazio, item sem oferta, oferta inválida, quantidade inválida e reserva indisponível não criam preferência/pedido pagável;
- cada transição válida grava `from`, `to`, causa, origem e ator; repetição idempotente não duplica histórico;
- toda transição inválida é rejeitada e não muda o estado;
- concorrência com `@Version` não permite perder uma transição;
- comprador não consulta pedido de outro usuário; ADMIN segue a política definida;
- `external_reference` da preferência é `Order.public_id`, e payment não tenta interpretá-lo como `Cart.id`;
- pedido mantém referências nullable e preenchíveis de payment/inventory/fulfillment/delivery sem quebrar quando os módulos futuros ainda não existem;
- migration limpa e upgrade V1–V17 preservam carrinhos, pagamentos legados e inventory.

Validação obrigatória ao implementar:

```bash
./mvnw clean test -Dmaven.plugin.validation=BRIEF
./mvnw clean package
git diff --check
git status --short
```

O teste de gateway real fica limitado a credenciais/ambiente de teste do Mercado Pago. Sem PostgreSQL/Testcontainers, não será possível comprovar adequadamente idempotência concorrente, constraints e locking.

## 7. Rastreabilidade dos critérios do PRD-005

| Critério | Implementação planejada | Evidência obrigatória |
|---|---|---|
| Checkout válido cria pedido com snapshot imutável de preço e linhas | `OrderService` cria `Order`/`OrderItem`; linhas não possuem update de snapshot | Teste de integração de criação e teste de alteração posterior do carrinho/catálogo |
| Repetição da confirmação não cria pedido duplicado | `Idempotency-Key`, unique `(buyer_id, checkout_key)` e retorno do pedido existente | Teste concorrente com mesma chave e inspeção de contagem de pedidos/linhas |
| Cada transição informa causa e origem | `OrderStatusHistory` append-only e serviço sem mutação direta de status | Teste de transição válida, inválida e reentrega; histórico contém causa/origem/ator |
| Pedido mantém referências para payment, inventory, fulfillment e entrega | Colunas/referências externas no pedido, preenchidas pelos módulos responsáveis | Teste de persistência/consulta com referências simuladas e contrato entre PLAN-005/006/007/008 |

## 8. Riscos, pressupostos e rollback

- **Gateway entre transações:** preferência pode ser criada depois de uma falha local ou o callback pode chegar antes da resposta HTTP. **Mitigação:** pedido idempotente, referência pública estável, estado `PAYMENT_PENDING` e PLAN-006 tolerante a reentrega/ordem.
- **Código legado de Payment:** registros existentes usam `Cart.id`. **Mitigação:** migration/compatibilidade explícita para dados legados; não reinterpretar silenciosamente referências antigas. Definir estratégia de backfill antes de ativar o novo webhook.
- **Reserva e checkout:** reserva pode expirar antes da aprovação. **Mitigação:** estados intermediários, operation key derivada do pedido e integração com release/expiration do PLAN-004.
- **Dados sensíveis no snapshot:** instruções podem conter credenciais de jogo. **Mitigação:** armazenar somente campos necessários, restringir consulta, não logar JSON bruto e deixar retenção/anonimização para decisão de segurança.
- **Rollback:** migrations são aditivas. Em falha, desabilitar a rota nova/feature flag, manter tabelas e dados para diagnóstico e reverter código em release; não editar nem desfazer migrations aplicadas.

## 9. Fora de escopo

Não entram neste plano: confirmação/autenticação de webhook do Mercado Pago, refund/dispute efetivos, cálculo de impostos/taxas/frete, numeração humana sequencial, múltiplos pedidos por carrinho como regra comercial, fulfillment/entrega, política de risco, reconciliação financeira, notificações, edição de instruções versionadas e remoção dos campos legados de `Cart`/`Product`.
