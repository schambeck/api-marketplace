# PLAN-007 — Fulfillment e operação

**Prioridade:** Escopo variável conforme o tipo de item.

Para itens digitais entregues automaticamente, o MVP pode usar uma operação simples vinculada ao pedido. A fila, lease, reatribuição e SLA completos só são necessários para entrega manual, física ou em volume operacional relevante.

**Issue:** [#9](https://github.com/schambeck/api-marketplace/issues/9)

**PRD:** [`PRD-007`](../prd/PRD-007-fulfillment.md) · **Dependências:** PRD-004, PRD-005, PRD-006, PRD-014 e PRD-008.

## Estado atual e alvo

Não existe fila ou trabalho executável. O alvo é `FulfillmentJob` criado somente para pedido pago, risco liberado e source alocado, com estados `PENDING`, `ASSIGNED`, `IN_PROGRESS`, `DELIVERED` e `FAILED`.

## Abordagem técnica

- Criar registro simples de entrega e status. `FulfillmentJob`, assignment, SLA e histórico operacional completo ficam para a segunda etapa.
- Usar idempotência para impedir duas entregas do mesmo item; claim/lease fica condicionado à existência de operadores concorrentes.
- Separar comando operacional de atualização do pedido; somente transições válidas de fulfillment emitem eventos de domínio.
- Suportar reatribuição e escalonamento preservando histórico e autorização por papel.
- Adicionar processo de timeout que marca ação pendente, sem apagar o job.

## Persistência, APIs e testes

Migration para jobs, atribuições e eventos, com índices de fila e lease. Criar controllers para consulta/ações de operador, sem expor instruções além do necessário. Testar elegibilidade, claim concorrente, reatribuição, conclusão, falha e timeout.

## Aceite e decisões

Testes de integração evidenciam fila com contexto, operações do operador, ausência de órfãos e exclusão mútua. SLA, canal e permissões de supplier/operador são decisões de produto; deixar adapters para execução automática/humana.
