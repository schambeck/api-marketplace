# PRD-013 — Reconciliação e settlement

**Rastreabilidade:** BP-025 · Finance · Importante.

## Problema e objetivo

Não há ledger ou processo para comparar pagamentos, pedidos, entregas, inventory, refunds e valores devidos. O produto deve detectar divergências e permitir fechamento financeiro confiável.

## Requisitos

1. Comparar periodicamente order × gateway, order × delivery, inventory × delivery e refund × gateway.
2. Se houver supplier/seller, comparar atividade confirmada × payable/settlement.
3. Registrar diferença, severidade, responsável, evidência, ajuste e encerramento.
4. Ajustes devem ser auditáveis e não apagar o fato original.

## Critérios de aceite

- Uma execução produz itens conciliados, divergentes e não verificáveis.
- Divergência tem vínculo aos registros de origem e responsável.
- Reprocessar o mesmo período não duplica ajustes.
- Fechamento só ocorre quando itens críticos estão resolvidos ou explicitamente aceitos.

## Decisões pendentes

Nível contábil, periodicidade, timezone, moeda, regras de taxa e existência de supplier/seller payable.

