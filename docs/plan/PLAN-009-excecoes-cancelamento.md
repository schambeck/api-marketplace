# PLAN-009 — Falhas, timeout, parcial e cancelamento

**PRD:** [`PRD-009`](../prd/PRD-009-excecoes-cancelamento.md) · **Dependências:** PRD-004, PRD-005, PRD-006, PRD-007 e PRD-008.

## Estado atual e alvo

Não há lifecycle de exceção nem liberação coordenada de reserva. O alvo é um `ExceptionCase`/workflow idempotente que transforma timeout, falha, parcial e cancelamento em próxima ação com prazo, responsável e efeitos financeiros/inventory explícitos.

## Abordagem técnica

- Definir deadlines por etapa, motivo/origem e estados de ação (`RETRY`, `RESCHEDULE`, `CHANGE_SOURCE`, `CUSTOMER_ACTION`, `CANCEL`, `MANUAL_REVIEW`).
- Centralizar cancelamento para liberar saldo não consumido e solicitar refund quando aplicável.
- Modelar entrega parcial com quantidade pendente e decisão explícita; impedir transição `COMPLETED` indevida.
- Usar scheduler/outbox com chave idempotente para expirações e retries; registrar cada tentativa.
- Preservar evidências e histórico de pedido/fulfillment.

## Persistência e testes

Migration para casos, deadlines, ações e idempotency keys. Testar expiração de carrinho/pedido/reserva, falha com responsável, cancelamento, parcial, repetição e falha no refund.

## Aceite e decisões

Os critérios serão comprovados em testes de integração com clock controlado. Prazos, elegibilidade, parcial e mecanismo scheduler devem ser aprovados antes de expor ações irreversíveis.

