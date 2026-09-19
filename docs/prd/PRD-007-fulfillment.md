# PRD-007 — Fulfillment e operação

**Rastreabilidade:** BP-016/BP-028 · Fulfillment · Essencial.

## Problema e objetivo

Não existe processo para transformar pedido pago e alocado em trabalho executável, nem fila, atribuição ou escalonamento. O produto deve coordenar fulfillment humano ou automático com responsabilidade clara.

## Requisitos

1. Criar fulfillment somente após pagamento aprovado, risco liberado e source alocado.
2. Suportar `PENDING`, `ASSIGNED`, `IN_PROGRESS`, `DELIVERED` e `FAILED`.
3. Registrar responsável, prioridade, prazo, tentativas e motivo de falha.
4. Permitir atribuição, reatribuição e escalonamento sem perder histórico.
5. Atualizar pedido apenas por eventos de fulfillment válidos.

## Critérios de aceite

- Pedido elegível entra na fila com contexto e instruções necessários.
- Operador consegue assumir, iniciar, concluir ou falhar um fulfillment.
- Timeout ou falha gera ação definida e não deixa trabalho órfão.
- Duas pessoas não executam a mesma atribuição simultaneamente.

## Decisões pendentes

Operação automática versus humana; SLA e prioridades; canais de trabalho; e permissões de operador/supplier.

