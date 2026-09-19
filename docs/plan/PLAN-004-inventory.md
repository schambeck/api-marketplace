# PLAN-004 — Reserva, alocação e consumo de inventory

**Issue:** [#6](https://github.com/schambeck/api-marketplace/issues/6)

**PRD:** [`PRD-004`](../prd/PRD-004-inventory.md) · **Dependências:** PRD-001, PRD-002 e PRD-003.

## Estado atual e alvo

`Product.quantity`/`inventoryStatus` não oferecem atomicidade nem vínculo a pedido. O alvo é um ledger de inventory com estados `AVAILABLE`, `RESERVED`, `ALLOCATED`, `CONSUMED`, `RELEASED` e `EXPIRED`, vinculado a linha de pedido e fonte.

## Abordagem técnica

- Definir granularidade (saldo, unidade ou código) e criar `InventoryItem`/`InventoryBalance` conforme a decisão.
- Criar tabela de movimentos idempotentes e serviço transacional para reservar, alocar, consumir e liberar.
- Usar locking pessimista ou atualização condicional por versão; selecionar estratégia após teste de concorrência PostgreSQL.
- Tornar expiração observável via scheduler/job seguro, sem depender de alteração manual em `Product`.
- Exigir reserva/alocação válida antes de criar fulfillment e consumir somente quantidade entregue.

## Persistência e APIs

Adicionar migrations para saldo/unidades, reservas, alocações, movimentos e chaves idempotentes. APIs administrativas devem ocultar códigos sensíveis; endpoints de operação devem mostrar somente o necessário.

## Sequência e validação

1. Fixar granularidade, oversell e TTL.
2. Implementar reserva com locking e idempotência.
3. Integrar pedido/pagamento/fulfillment.
4. Implementar expiração, release e consumo parcial.

Testes obrigatórios: duas reservas concorrentes, repetição da mesma operação, expiração, pedido pago sem reserva, falha/cancelamento e entrega parcial.

## Aceite e riscos

Os critérios serão evidenciados por testes transacionais de integração. O risco maior é overselling por locking inadequado; a migration deve incluir constraints e o serviço deve rejeitar saldo insuficiente de forma determinística.
