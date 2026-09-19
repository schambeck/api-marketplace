# PLAN-005 — Pedido e lifecycle

**PRD:** [`PRD-005`](../prd/PRD-005-pedido-lifecycle.md) · **Dependências:** PRD-001, PRD-002 e base para PRD-004/006–014.

## Estado atual e alvo

`CreatePreferenceService` usa `Cart.id` como referência externa e não existe agregado comercial. O alvo é `Order` independente do carrinho, com linhas e snapshots imutáveis, transições auditadas e referências a payment, inventory, fulfillment e delivery.

## Abordagem técnica

- Criar `Order`, `OrderItem`, snapshot de contexto/instruções e `OrderStateTransition`.
- Definir estados e máquina de transição; mudanças passam por serviço único, com causa, ator e origem.
- Implementar checkout idempotente por carrinho/checkout key, congelando preço, quantidade e dados do cliente.
- Migrar preference para referenciar `order.public_id`, mantendo `Cart` apenas como origem descartável.
- Expor consulta do próprio pedido e APIs operacionais com autorização adequada.

## Persistência e contrato

Migration com identificador público único, estado, totais monetários, versionamento otimista, itens e tabelas de histórico. Atualizar Payment/Preference e schema JSON; não permitir edição de snapshot após criação.

## Sequência e testes

Implementar agregado e transições; checkout idempotente; integração com preference/payment; referências futuras; autorização. Testar snapshot, duplicidade, transições inválidas, concorrência e retenção de histórico.

## Aceite e riscos

Cada critério do PRD deve ter teste de integração. Decisões sobre pedido por checkout, taxas, numeração e estados mínimos precisam ser fechadas antes da migration; sem isso, implementar somente um esqueleto interno não exposto.

