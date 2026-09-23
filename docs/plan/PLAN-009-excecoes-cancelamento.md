# PLAN-009 — Falhas, timeout, parcial e cancelamento

**Prioridade:** MVP reduzido. Cobrir cancelamento, falha e liberação do estoque.

**Issue:** [#11](https://github.com/schambeck/api-marketplace/issues/11)

**PRD:** [`PRD-009`](../prd/PRD-009-excecoes-cancelamento.md) · **Dependências:** PRD-004, PRD-005, PRD-006, PRD-007 e PRD-008.

## Estado atual e alvo

Não há lifecycle de exceção nem liberação coordenada de reserva. O alvo é um `ExceptionCase`/workflow idempotente que transforma timeout, falha, parcial e cancelamento em próxima ação com prazo, responsável e efeitos financeiros/inventory explícitos.

## Abordagem técnica

- Definir motivos básicos e ações `RETRY`, `CANCEL` e `CUSTOMER_ACTION`.
- Centralizar cancelamento para liberar saldo não consumido e solicitar refund quando aplicável.
- Adiar entrega parcial, troca de fonte, revisão manual e deadlines por etapa até haver operação que exija esses fluxos.
- Usar idempotência para retries e expirações; scheduler/outbox completos ficam para evolução posterior.
- Preservar evidências e histórico de pedido/fulfillment.

## Persistência e testes

Migration para casos, deadlines, ações e idempotency keys. Testar expiração de carrinho/pedido/reserva, falha com responsável, cancelamento, parcial, repetição e falha no refund.

## Aceite e decisões

Os critérios serão comprovados em testes de integração com clock controlado. Prazos, elegibilidade, parcial e mecanismo scheduler devem ser aprovados antes de expor ações irreversíveis.
