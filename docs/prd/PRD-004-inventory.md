# PRD-004 — Reserva, alocação e consumo de inventory

**Rastreabilidade:** BP-013 · Inventory · Essencial.

## Problema e objetivo

`Product.quantity` e `inventoryStatus` são apenas dados editáveis. O produto precisa impedir overselling e vincular quantidade disponível a um pedido e a uma fonte de fulfillment.

## Requisitos

1. Inventory deve suportar os estados `AVAILABLE`, `RESERVED`, `ALLOCATED`, `CONSUMED`, `RELEASED` e `EXPIRED`.
2. Reserva deve ser atômica, possuir expiração e referenciar pedido/linha.
3. Allocation deve apontar para a fonte responsável; consumo só ocorre após confirmação de entrega.
4. Falha, cancelamento ou timeout deve liberar o saldo não consumido.
5. Toda mudança deve ser idempotente e auditável.

## Critérios de aceite

- Duas compras concorrentes não reservam a mesma unidade.
- Reserva expirada é liberada automaticamente ou por processo operacional seguro.
- Pedido pago não avança ao fulfillment sem reserva/alocação válida.
- Entrega parcial consome somente a quantidade confirmada.

## Decisões pendentes

Granularidade do inventory (unidade, saldo, código); estratégia de locking; política de oversell; e prazo de reserva.

