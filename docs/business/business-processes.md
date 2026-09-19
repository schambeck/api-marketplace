# Processos de Negócio — Modelo Atual e Análise de Completude

## 1. Sumário executivo

Este documento preserva o baseline observável do backend e acrescenta uma análise de completude do modelo de negócio. O backend cobre cadastro automático de usuários autenticados, publicação e consulta de produtos, imagens, carrinho e preparação/registro do pagamento via Mercado Pago. O fluxo implementado termina na criação de uma preferência de pagamento e no armazenamento de dados recebidos; não há, no código analisado, pedido, reserva/baixa de estoque, fulfillment, entrega, estorno ou fechamento financeiro.

A evidência mostra um modelo predominantemente CRUD, com algumas regras concentradas no carrinho e na integração de pagamento. Não foram encontrados testes versionados em `src/test`, consumidores de fila, schedulers ou eventos de domínio publicados pela aplicação.

## 2. Escopo e método

Foram cruzados `AGENTS.md`, estrutura do projeto, controllers, services, entidades, repositories, migrations, configuração de segurança, integrações e arquivos de configuração. A classificação usada neste documento é:

- **Implementado**: comportamento diretamente observável no código executável.
- **Inferido**: interpretação de negócio sustentada por mais de uma evidência, mas sem regra formal.
- **Ausente**: esperado em um marketplace, porém não localizado no projeto.
- **A validar**: depende de contrato externo, configuração ou comportamento que não possui teste.

As referências abaixo usam o caminho do arquivo e a linha inicial do trecho relevante.

## 3. Visão geral do sistema

```text
Identidade JWT/Keycloak
        ↓ autenticação bem-sucedida
Cadastro local de User
        ↓
Catálogo público ← Product ← imagens em GCS/MinIO
        ↓ seleção
Carrinho único por usuário ← Cart + CartItem
        ↓ checkout
Preferência Mercado Pago
        ↓ retorno/webhook/registro manual
Payment e Webhook persistidos
```

O acesso é protegido por JWT; catálogo GET, download de objetos, GET de OAuth e POST de webhooks são públicos, enquanto as demais rotas exigem autenticação (`SecurityConfig.java:31-46`).

## 4. Domínios identificados

| Domínio | Responsabilidade | Entidades principais | Operações de negócio |
|---|---|---|---|
| Identidade e conta | Reconhecer o usuário do provedor JWT e manter seu cadastro local | `User` | Criar no primeiro login, consultar, atualizar dados e chave pública |
| Catálogo e oferta | Publicar e consultar produtos por vendedor | `Product` | Criar, editar, excluir, listar, pesquisar e listar por usuário |
| Mídia e object storage | Associar imagens a produtos e transportar arquivos | `Image`, objeto externo | Upload/download, criar/editar/excluir vínculo de imagem |
| Carrinho | Manter uma cesta por usuário e seu total calculado | `Cart`, `CartItem` | Adicionar, impedir duplicidade opcional, alterar quantidade, remover, contar |
| Pagamento | Preparar checkout Mercado Pago e registrar retornos | `Payment`, `PaymentDetail` | Criar preferência, relacionar pagamento ao carrinho, atualizar/consultar |
| Integração Mercado Pago | Autorizar o marketplace a operar em nome do usuário | OAuth request/code/token | Iniciar autorização, trocar code por token e guardar public key |
| Webhook | Receber e persistir payloads de integração | `Webhook` | Gravar payload estruturado; endpoint `location` apenas acusa recebimento |

## 5. Atores e entradas

| Ator | Entrada | Processo iniciado |
|---|---|---|
| Usuário autenticado | Login JWT | Provisionamento local da conta |
| Usuário/cliente | `GET /catalog/**` | Consulta da oferta |
| Usuário/vendedor autenticado | CRUD de `products`, `images` e arquivos | Gestão de catálogo e mídia |
| Usuário autenticado | `POST/PUT/DELETE /shopping-cart/**` | Composição do carrinho |
| Usuário autenticado | `POST /mercado-pago/preferences` | Preparação do checkout |
| Mercado Pago/cliente autorizado | `POST /webhooks` | Registro de notificação |
| Usuário autenticado + Mercado Pago | `GET /oauth/start-flow`, callback `GET /oauth?code=...` | Vinculação da conta Mercado Pago |

Não há entradas automáticas identificadas: nenhuma classe com scheduler, job, consumer de fila, retry de negócio ou listener de webhook que avance o pagamento foi encontrada. O único listener de aplicação é o de sucesso de autenticação.

## 6. Cadeia de valor

1. Usuário autentica no provedor e passa a existir localmente.
2. Vendedor mantém produtos e suas imagens.
3. Cliente consulta o catálogo e compõe seu carrinho.
4. Cliente solicita uma preferência com os itens do carrinho.
5. Mercado Pago recebe a preferência e conduz o checkout externo.
6. O backend pode registrar `Payment` e `Webhook`, mas não transforma o pagamento em pedido concluído.

## 7. Catálogo de processos

| ID | Processo | Situação |
|---|---|---|
| BP-001 | [Provisionar usuário no primeiro login](processes/BP-001-provisionar-usuario.md) | Implementado |
| BP-002 | [Consultar catálogo](processes/BP-002-consultar-catalogo.md) | Implementado |
| BP-003 | [Publicar e manter produto](processes/BP-003-publicar-produto.md) | Implementado |
| BP-004 | [Armazenar e associar imagem](processes/BP-004-imagem.md) | Implementado parcialmente |
| BP-005 | [Montar e manter carrinho](processes/BP-005-carrinho.md) | Implementado |
| BP-006 | [Criar preferência de pagamento](processes/BP-006-preferencia-pagamento.md) | Implementado |
| BP-007 | [Registrar pagamento recebido](processes/BP-007-pagamento.md) | Implementado como persistência |
| BP-008 | [Registrar webhook](processes/BP-008-webhook.md) | Implementado como persistência |
| BP-009 | [Autorizar Mercado Pago para o usuário](processes/BP-009-oauth-mercado-pago.md) | Implementado com lacunas de segurança/validade |
| BP-010 | [Configurar contexto de game, plataforma, região e variante](processes/BP-010-contexto-game.md) | Ausente — essencial |
| BP-011 | [Publicar e administrar disponibilidade da oferta](processes/BP-011-oferta-disponibilidade.md) | Parcialmente modelado |
| BP-012 | [Definir modelo de supply e adquirir/register supply](processes/BP-012-supply.md) | Ausente — essencial; modelo a validar |
| BP-013 | [Reservar, alocar e consumir inventory](processes/BP-013-inventory.md) | Ausente — essencial |
| BP-014 | [Criar e administrar pedido a partir do checkout](processes/BP-014-pedido.md) | Ausente — essencial |
| BP-015 | [Confirmar pagamento e processar webhook idempotentemente](processes/BP-015-confirmacao-pagamento.md) | Parcialmente modelado |
| BP-016 | [Criar e executar fulfillment](processes/BP-016-fulfillment.md) | Ausente — essencial |
| BP-017 | [Coletar e validar instruções de entrega](processes/BP-017-instrucoes-entrega.md) | Ausente — essencial |
| BP-018 | [Registrar tentativa, evidência e confirmação de entrega](processes/BP-018-evidencia-entrega.md) | Ausente — importante |
| BP-019 | [Tratar falha, timeout e fulfillment parcial](processes/BP-019-falhas-fulfillment.md) | Ausente — importante |
| BP-020 | [Expirar e cancelar carrinho, pedido ou reserva](processes/BP-020-cancelamento.md) | Ausente — importante |
| BP-021 | [Solicitar, decidir e executar refund](processes/BP-021-refund.md) | Ausente — importante |
| BP-022 | [Tratar disputa e chargeback](processes/BP-022-disputa-chargeback.md) | Ausente — importante |
| BP-023 | [Operar suporte pré-venda, venda e pós-venda](processes/BP-023-suporte.md) | Ausente — importante |
| BP-024 | [Comunicar eventos relevantes ao cliente e operador](processes/BP-024-comunicacao.md) | Ausente — importante |
| BP-025 | [Reconciliar pagamentos, entregas, inventory e valores devidos](processes/BP-025-reconciliacao.md) | Ausente — importante |
| BP-026 | [Avaliar risco e executar revisão manual](processes/BP-026-risco.md) | Ausente — importante; políticas a validar |
| BP-027 | [Administrar regras por game/publisher e restrições](processes/BP-027-regras-game-publisher.md) | Ausente — importante; validação externa |
| BP-028 | [Operar fila de fulfillment e escalonamento](processes/BP-028-fila-fulfillment.md) | Ausente — importante |
| BP-029 | [Coletar reviews e administrar reputação](processes/BP-029-reviews-reputacao.md) | Ausente — evolutivo; a validar |

## 8. Fluxos detalhados

Os fluxos detalhados de cada processo foram separados em documentos individuais para facilitar manutenção e consulta:

- [BP-001 — Provisionar usuário no primeiro login](processes/BP-001-provisionar-usuario.md)
- [BP-002 — Consultar catálogo](processes/BP-002-consultar-catalogo.md)
- [BP-003 — Publicar e manter produto](processes/BP-003-publicar-produto.md)
- [BP-004 — Armazenar e associar imagem](processes/BP-004-imagem.md)
- [BP-005 — Montar e manter carrinho](processes/BP-005-carrinho.md)
- [BP-006 — Criar preferência de pagamento](processes/BP-006-preferencia-pagamento.md)
- [BP-007 — Registrar pagamento recebido](processes/BP-007-pagamento.md)
- [BP-008 — Registrar webhook](processes/BP-008-webhook.md)
- [BP-009 — Autorizar Mercado Pago para o usuário](processes/BP-009-oauth-mercado-pago.md)

Os processos BP-010 a BP-029 também possuem documentos individuais em [`docs/business/processes/`](processes/README.md); suas descrições de análise e lacunas permanecem nas seções posteriores deste documento.

## 9. Regras de negócio catalogadas

| ID | Regra | Condição | Resultado | Evidência |
|---|---|---|---|---|
| BR-001 | Usuário local único por username | Login bem-sucedido | Cria apenas se username não existir | `AuthenticationEvents.java:22-33`; `V1__create_user.sql` |
| BR-002 | Um carrinho por usuário | Inclusão/consulta de item | Reutiliza carrinho existente; índice único impede duplicidade | `CreateShoppingCartItemService.java:60-66`; `V3__create_cart.sql` |
| BR-003 | Subtotal monetário | Inclusão ou alteração | `price × quantity`, escala 2, arredondamento HALF_UP | `CreateShoppingCartItemService.java:69-70`; `UpdateShoppingCartItemService.java:32-34` |
| BR-004 | Total acompanha itens | Inclusão, alteração ou remoção | Soma, substituição ou subtração de subtotais | serviços de shopping cart |
| BR-005 | Inclusão condicional | Produto já está no carrinho | Não cria segundo item e informa `created=false` | `CreateShoppingCartItemService.java:40-46` |
| BR-006 | Referência do pagamento é o carrinho | Criação de Payment | `externalReference` deve ser conversível para id de Cart existente | `PaymentService.java:17-21` |
| BR-007 | Checkout exige public key | Criação de preferência | Sem chave pública, falha antes da chamada externa | `CreatePreferenceService.java:51-52` |
| BR-008 | Preferência carrega itens do carrinho | Checkout | Itens viram linhas Mercado Pago em BRL | `CreatePreferenceService.java:53-57`; `CartPreferenceConverter.java` |
| BR-009 | Webhook é público | Recepção externa | POST pode ser chamado sem JWT e é persistido | `SecurityConfig.java:36`; `WebhookController.java:21-24` |
| BR-010 | Produto inexistente interrompe operações relacionadas | Consulta/associação | Repository lança exceção de não encontrado | repositories de `Product`, `Cart`, `CartItem` e `User` |

Regras que **não** podem ser atribuídas ao sistema por falta de evidência: impedir compra sem estoque, recalcular `inventoryStatus`, reservar estoque, criar pedido, validar titularidade do item, confirmar pagamento, deduplicar webhook ou atualizar pagamento a partir de notificação.

## 10. Ciclos de vida e estados

### Produto

```text
não persistido → persistido/publicado → atualizado → removido
```

`inventoryStatus` é um atributo editável, não um ciclo implementado. Valores iniciais sugerem `INSTOCK`, `LOWSTOCK` e `OUTOFSTOCK`, mas não há transições automáticas.

### Carrinho

```text
inexistente → criado ao adicionar item → com itens/total calculado
                                      ↓
                         itens alterados ou removidos
                                      ↓
                         inexistente ao remover o último item
```

### Pagamento

```text
sem registro → Payment persistido com status recebido → status editável
```

Não existe vocabulário controlado de status nem fluxo de aprovação/recusa.

### OAuth Mercado Pago

```text
sem autorização → request/state criado → code salvo (used=false)
                                      → token salvo + public key no User
```

As transições de expiração, uso único e renovação não estão implementadas.

## 11. Eventos, integrações e transações

| Tipo | Evidência | Efeito |
|---|---|---|
| Evento de autenticação | `AuthenticationSuccessEvent` | Provisiona usuário local se ausente |
| Evento de domínio | Nenhum encontrado | Não há publicação |
| Fila/consumer | Nenhum encontrado | Não há processamento assíncrono |
| Mercado Pago OAuth | `MercadoPagoClient`/Feign | Troca code por tokens e public key |
| Mercado Pago Preference API | SDK `PreferenceClient` | Cria checkout externo |
| GCS | `GoogleStorageService` | Bucket, upload e download |
| MinIO | `MinioStorageService` | Bucket, upload e download |
| Keycloak/JWT | Resource server | Autentica chamadas e fornece claims |
| Webhook externo | `POST /webhooks` | Persiste JSON; não propaga mudança |

As operações do carrinho e CRUDs usam transações locais Spring, mas a criação de preferência e a persistência de Payment/Webhook não formam transação distribuída. Não há outbox, saga, compensação ou retry de negócio.

## 12. Happy path ponta a ponta

```text
Usuário autentica
  → User local é criado (se necessário)
  → consulta catálogo
  → adiciona produtos ao carrinho
  → altera quantidades conforme necessário
  → inicia/vincula Mercado Pago, se public key ausente
  → solicita preferência
  → Mercado Pago recebe itens e externalReference do carrinho
  → cliente conclui o checkout no provedor
```

O último passo que o backend implementa é a criação da preferência. A conclusão do pagamento no provedor não possui, neste repositório, uma continuação ponta a ponta até pedido ou estoque.

## 13. Exception paths e lacunas

| Situação | Comportamento observado |
|---|---|
| Usuário/produto/carrinho/item inexistente | Repository lança exceção de entidade não encontrada; tratamento HTTP geral retorna 404 para `EntityNotFoundException` |
| Produto não encontrado ao adicionar item | Inclusão falha antes de alterar carrinho |
| Produto já no carrinho em `createIfAbsent` | Retorno normal com `created=false` |
| Carrinho ausente no checkout | Falha de busca; não há preferência |
| Public key ausente | `RuntimeException`; checkout não é enviado |
| Erro Mercado Pago | Log e `RuntimeException`; não há retry/compensação |
| Falha object storage | `RuntimeException`; metadado `Image` não é criado automaticamente |
| Webhook inválido/duplicado | Não há validação, assinatura ou deduplicação visível |

Lacunas prioritárias para validação do negócio:

1. Definir se `Product.quantity` e `inventoryStatus` devem impedir inclusão/checkout e como são atualizados.
2. Criar ou confirmar o conceito de pedido; hoje `Cart` é usado como referência externa de pagamento.
3. Implementar o consumidor do webhook que atualiza Payment e o resultado do checkout, se isso fizer parte do escopo.
4. Definir a máquina de estados e transições de Payment.
5. Validar titularidade/autorização nas operações por id de carrinho, item, produto e imagem.
6. Corrigir o ciclo de vida OAuth: state por sessão, validação no callback, code usado uma vez e expiração real.
7. Adicionar testes de comportamento para total, duplicidade, remoção do último item, erro de integração e webhook.

## 14. Inconsistências e riscos de interpretação

- `Payment` parece representar um retorno do Mercado Pago, mas sua criação é uma rota CRUD autenticada e não está conectada ao webhook.
- O nome `OAuthAuthorizationCode.expiresAt` é preenchido com `LocalDateTime.now()`, sem usar a expiração do provedor; isso não comprova uma validade funcional.
- A existência de `PaymentWebhook.java` não representa um fluxo ativo: o arquivo está comentado.
- `CreatePreferenceService` verifica `publicKey`, mas a configuração de access token aparece comentada; a autenticação efetiva do SDK depende da configuração/runtime não demonstrada neste código.
- Os dados iniciais usam `OUTOFSTOCK` em produtos com quantidades não uniformes e `INSTOCK` em pelo menos um produto com quantidade zero; não se deve inferir que o status seja calculado pela quantidade.
- Há referências de rota e comportamento de object storage que devem ser conferidas com consumidores externos; o código analisado expõe `/object-storage/download/{objectId}`.

## 15. Matriz de rastreabilidade

| Processo | Entrada | Regra/decisão | Estado/dado alterado | Integração/evento | Saída |
|---|---|---|---|---|---|
| BP-001 | AuthenticationSuccessEvent | Criar se username ausente | `User` criado | Keycloak/JWT | Conta local |
| BP-002 | GET catálogo | Busca opcional e paginação | Nenhum | Nenhuma | Produtos |
| BP-003 | CRUD products | Resolver usuário no create associado | `Product` | Nenhuma | Oferta |
| BP-004 | Upload/CRUD images | Bucket/objeto e vínculo separados | Bucket + `Image` | GCS/MinIO | Metadados/arquivo |
| BP-005 | Shopping cart | Carrinho único, total calculado | `Cart`, `CartItem` | Nenhuma | Carrinho atualizado |
| BP-006 | POST preference | Exigir public key; enviar itens | Nenhuma local | Mercado Pago Preference API | Preference |
| BP-007 | CRUD payment | externalReference deve ser Cart id | `Payment` | Nenhuma automática | Registro |
| BP-008 | POST webhook | Persistir JSON | `Webhook` | Provedor externo → API | Auditoria |
| BP-009 | OAuth start/callback | state e code | OAuth request/code/token + User public key | Mercado Pago OAuth | Credencial vinculada |

## 16. Glossário

- **User:** conta local vinculada ao subject/external id do JWT.
- **Product:** oferta publicada por um usuário, contendo preço e atributos de estoque informados.
- **Cart:** cesta única do usuário, com total agregado.
- **CartItem:** linha da cesta com preço capturado, quantidade e subtotal.
- **Preference:** sessão/solicitação de checkout criada no Mercado Pago.
- **Payment:** registro local com dados de pagamento e status recebidos/administrados.
- **Webhook:** payload de notificação persistido, sem processamento de negócio demonstrado.
- **Public key:** chave do usuário Mercado Pago usada como pré-condição do checkout.

## 17. Conclusão

O processo de negócio efetivamente coberto é: **autenticar usuário → publicar/consultar oferta → montar carrinho → gerar preferência de pagamento → persistir registros de integração**. O backend oferece os componentes de uma jornada de marketplace, mas não implementa a etapa que transforma checkout aprovado em pedido, baixa de estoque, fulfillment ou encerramento da venda. Essa fronteira deve ser tratada como a principal lacuna funcional, e não como comportamento implícito.

## 18. Modelo operacional e cadeia de valor esperada

### 18.1 Classificação do modelo atual

**[EVIDÊNCIA INTERNA]** O documento existente chama o sistema de marketplace e associa `Product` a um usuário, mas não documenta onboarding de sellers independentes, ofertas separadas do produto, supply externo, comissões ou payouts.

**[INFERIDO]** O modelo atual é um catálogo transacional com usuários que podem publicar produtos; ainda não há evidência suficiente para classificá-lo como marketplace multi-seller. A ausência de supply e seller settlement torna prematuro assumir um marketplace completo.

**[A VALIDAR — MODELO OPERACIONAL]** Decidir entre:

1. **B2C com inventory/supply controlado pela plataforma**: exige aquisição, inventory, reserva, fulfillment e suporte operados pela plataforma.
2. **Marketplace**: exige seller onboarding, aprovação, seller offer, SLA, disputa, comissão e settlement.
3. **Híbrido**: exige separar claramente inventory próprio, supplier e seller, inclusive para responsabilidade de entrega e pagamento.

Como referência, a MMOExp declara publicamente uma operação B2C com supply e inventory controlados e canais próprios de fulfillment, não um marketplace C2C genérico ([About MMOEXP](https://www.mmoexp.com/About.html)). Isso é evidência externa sobre uma referência, não decisão para este produto.

### 18.2 Cadeia de valor mínima para validar

```text
Game context and rules
        ↓
Supply / sourcing
        ↓
Inventory and availability
        ↓
Catalog and offer publication
        ↓
Discovery and cart
        ↓
Checkout and order creation
        ↓
Payment confirmation and risk decision
        ↓
Reservation / allocation
        ↓
Fulfillment and delivery
        ↓
Evidence and customer confirmation
        ↓
Support, refund and dispute
        ↓
Reconciliation and settlement
```

**[EVIDÊNCIA INTERNA]** A documentação atual cobre apenas identidade, catálogo, carrinho e criação/persistência de artefatos de pagamento.

**[GAP]** As transições entre pagamento confirmado, pedido, allocation, entrega, pós-venda e reconciliação não possuem processo correspondente.

## 19. Processos adicionados e expandidos

Os processos adicionados e expandidos foram separados em documentos individuais para facilitar manutenção e consulta:

- [BP-010 — Contexto de game, plataforma, região e variante](processes/BP-010-contexto-game.md)
- [BP-011 — Oferta e disponibilidade](processes/BP-011-oferta-disponibilidade.md)
- [BP-012 — Supply e aquisição](processes/BP-012-supply.md)
- [BP-013 — Reserva, alocação e consumo de inventory](processes/BP-013-inventory.md)
- [BP-014 — Pedido e lifecycle](processes/BP-014-pedido.md)
- [BP-015 — Confirmação de pagamento](processes/BP-015-confirmacao-pagamento.md)
- [BP-016 — Fulfillment](processes/BP-016-fulfillment.md)
- [BP-017 — Instruções de entrega](processes/BP-017-instrucoes-entrega.md)
- [BP-018 — Evidência e confirmação de entrega](processes/BP-018-evidencia-entrega.md)
- [BP-019 — Falhas, timeout e fulfillment parcial](processes/BP-019-falhas-fulfillment.md)
- [BP-020 — Expiração e cancelamento](processes/BP-020-cancelamento.md)
- [BP-021 — Refund](processes/BP-021-refund.md)
- [BP-022 — Disputa e chargeback](processes/BP-022-disputa-chargeback.md)
- [BP-023 — Suporte](processes/BP-023-suporte.md)
- [BP-024 — Comunicação de eventos](processes/BP-024-comunicacao.md)
- [BP-025 — Reconciliação](processes/BP-025-reconciliacao.md)
- [BP-026 — Risco e revisão manual](processes/BP-026-risco.md)
- [BP-027 — Regras por game/publisher](processes/BP-027-regras-game-publisher.md)
- [BP-028 — Operação da fila de fulfillment](processes/BP-028-fila-fulfillment.md)
- [BP-029 — Reviews e reputação](processes/BP-029-reviews-reputacao.md)

Os critérios de cobertura, lifecycle, eventos esperados e decisões pendentes permanecem nas seções seguintes deste documento.

## 20. Matriz de completude e rastreabilidade de gaps

| Macroprocesso | Processo esperado | Documentado antes | Cobertura após análise | Relevância | Ação |
|---|---|---:|---|---|---|
| Identidade | Provisionar conta | Sim | Adequada para o baseline | Essencial | Manter; expandir recuperação/perfil se necessário |
| Catálogo | Contexto de game e variante | Não | Ausente | Essencial | Adicionar BP-010 |
| Catálogo | Oferta e disponibilidade | Parcial | Parcial | Essencial | Expandir BP-003/BP-011 |
| Supply | Source, supplier/seller e aquisição | Não | Ausente | Essencial | Adicionar BP-012; validar modelo |
| Inventory | Reserva, allocation, consumo e release | Não | Ausente | Essencial | Adicionar BP-013 |
| Transaction | Pedido e lifecycle | Não | Ausente | Essencial | Adicionar BP-014 |
| Payment | Confirmação, idempotência e transição | Parcial | Parcial | Essencial | Expandir BP-007/BP-008/BP-015 |
| Fulfillment | Execução operacional | Não | Ausente | Essencial | Adicionar BP-016/BP-028 |
| Delivery | Instruções, tentativa, evidência e confirmação | Não | Ausente | Essencial/importante | Adicionar BP-017/BP-018 |
| Exceptions | Falha, timeout, parcial e cancelamento | Não | Ausente | Importante | Adicionar BP-019/BP-020 |
| After-sales | Refund, dispute e chargeback | Não | Ausente | Importante | Adicionar BP-021/BP-022 |
| Support | Case lifecycle | Não | Ausente | Importante | Adicionar BP-023 |
| Communication | Notificações de eventos | Não | Ausente | Importante | Adicionar BP-024 |
| Finance | Reconciliation e settlement | Não | Ausente | Importante | Adicionar BP-025 |
| Risk | Review e decisões de risco | Não | Ausente | Importante | Adicionar BP-026 |
| Rules | Regras por game/publisher | Não | Ausente | Importante | Adicionar BP-027 |
| Reputation | Reviews e moderação | Parcial (`rating`) | Ausente como processo | Evolutivo | Adicionar BP-029 se validado |

## 21. Lifecycle e eventos esperados

### 21.1 Pedido, pagamento e fulfillment

```text
Cart
  → Checkout validated
  → Order created
  → Payment pending
  → Payment confirmed / rejected / expired
  → Risk cleared / held / blocked
  → Supply reserved and allocated
  → Fulfillment assigned and started
  → Delivery attempted
  → Delivered / partially delivered / failed
  → Completed, refunded or disputed
```

### 21.2 Eventos que precisam de processo anterior e posterior

| Evento | Antes | Depois |
|---|---|---|
| `OrderCreated` | Checkout validado e preço congelado | Payment/risk |
| `PaymentConfirmed` | Webhook/consulta idempotente | Order paid, reserve e fulfillment |
| `InventoryReserved` | Disponibilidade e regra de reserva | Allocation/assignment |
| `FulfillmentStarted` | Pedido pago, risco liberado e source atribuído | Tentativa/evidência |
| `DeliveryFailed` | Tentativa registrada com motivo | Retry, ação do cliente, cancelamento ou refund |
| `DeliveryCompleted` | Evidência e quantidade confirmada | Completion, review, reconciliation |
| `RefundCompleted` | Elegibilidade e decisão | Order/payment update e reconciliação |
| `DisputeOpened` | Reclamação e contexto preservados | Investigação, decisão e comunicação |

**[GAP]** Nenhum desses eventos, exceto autenticação, possui produtor/consumidor de negócio demonstrado no baseline.

## 22. Business Process Gap Analysis

### Added

- **BP-010** contexto de game, plataforma, região e variante.
- **BP-012** supply e aquisição/register de supply.
- **BP-013** reservation, allocation e consumo de inventory.
- **BP-014** criação e lifecycle de pedido.
- **BP-016** fulfillment.
- **BP-017** instruções de entrega.
- **BP-018** evidência e confirmação.
- **BP-019** falha, timeout e parcial.
- **BP-020** expiração e cancelamento.
- **BP-021** refund.
- **BP-022** disputa e chargeback.
- **BP-023** suporte.
- **BP-024** comunicação de eventos.
- **BP-025** reconciliação.
- **BP-026** risco e revisão manual.
- **BP-027** regras por game/publisher.
- **BP-028** operação de fulfillment.
- **BP-029** reviews, como capacidade evolutiva.

### Expanded

- **BP-003/BP-002:** distinguir catálogo, produto e oferta; exigir contexto e disponibilidade antes de venda.
- **BP-006:** tratar preference como etapa de checkout, não como pedido ou confirmação de pagamento.
- **BP-007/BP-008:** separar persistência de Payment/Webhook do processo de confirmação, idempotência e avanço do pedido.
- **BP-009:** manter como integração de autorização, mas validar state, expiração, uso único, refresh e revogação.

### Validate

1. A plataforma é B2C, marketplace ou híbrida?
2. Existe inventory próprio, supplier, seller, aquisição on-demand ou combinação?
3. Quais games, plataformas, regiões, servidores, variantes e métodos de entrega serão suportados?
4. Fulfillment pode ser parcial? Há delivery windows e timeouts por produto?
5. Quais dados de jogador são necessários, por quanto tempo e quem pode acessá-los?
6. Quais critérios de risco, refund, dispute, chargeback, review e retenção são desejados?
7. Haverá settlement para supplier/seller ou somente reconciliação com o gateway?
8. Quais termos de publisher tornam uma oferta permitida, restrita ou proibida?

### Not Applicable to the current documented model

- **Seller onboarding, seller payout, seller commission e seller performance**: não devem ser tratados como existentes; só entram se o modelo for marketplace ou híbrido.
- **Supplier settlement**: depende de supplier externo e não pode ser assumido no modelo atual.
- **Reviews publicadas**: o campo `rating` é evidência insuficiente; tratar como evolutivo até decisão de produto.
- **Delivery method específico (mail, trade, auction house, redeem code, boosting ou top-up)**: não assumir como permitido; cada combinação requer **[A VALIDAR — GAME/PUBLISHER TERMS]**.

## 23. Relatório de alterações

**Arquivo atualizado:** `docs/business/business-processes.md`.

**Processos existentes analisados:** 9 (`BP-001` a `BP-009`).

**Processos adicionados à análise/documentação:** `BP-010`, `BP-012` a `BP-014` e `BP-016` a `BP-029`. `BP-011` e `BP-015` foram tratados como expansões de capacidades já parcialmente presentes.

**Novos macroprocessos reconhecidos:** contexto/catalog, supply, inventory, transaction/order, fulfillment/delivery, after-sales/support, risk, financial operations e reputation.

**Principais lacunas:** ausência de pedido como agregado comercial; confirmação de pagamento desconectada do webhook; ausência de reservation/allocation/consumption; ausência de fulfillment, instruções, evidência e falha de entrega; ausência de refund/dispute/support; ausência de reconciliation e decisões de risco; ambiguidade sobre B2C versus marketplace/híbrido.

**Fontes externas utilizadas:** [MMOExp About](https://www.mmoexp.com/About.html), [home/catalog](https://www.mmoexp.com/), [delivery times](https://www.mmoexp.com/About/how-long-will-it-take-to-receive-my-order-after-payment.html), [regular questions](https://www.mmoexp.com/About/regular-questions-about-using.html), [refund policy](https://www.mmoexp.com/Refund.html), [contact/support](https://www.mmoexp.com/Contact.html), [help center](https://www.mmoexp.com/About/help.html), [reviews](https://www.mmoexp.com/reviews.html) e [exemplo de fluxo de allocation/delivery](https://www.mmoexp.com/Account/Steam/169425005.html).
