# PRD-014 — Risco e revisão manual

**Rastreabilidade:** BP-026 · Risk · Importante; políticas a validar.

## Problema e objetivo

Não há decisão de risco para pedidos suspeitos, inconsistência de conta, abuso de refund ou chargeback. O produto deve permitir sinalizar, revisar e decidir sem perder contexto.

## Requisitos

1. Avaliar sinais configuráveis antes de fulfillment e, quando necessário, antes de confirmação final.
2. Suportar `ALLOW`, `HOLD`, `BLOCK` e `ESCALATE`, com motivo e evidências.
3. Permitir revisão manual com segregação de função e histórico de decisão.
4. Decisão deve ser idempotente e disparar apenas as transições permitidas.

## Critérios de aceite

- Pedido sinalizado não é entregue enquanto estiver em `HOLD`.
- Revisor consegue liberar, bloquear ou escalar com motivo obrigatório.
- O sistema preserva sinais e evidências usados na decisão.
- Alteração de política é versionada e aplicável de forma determinística.

## Decisões pendentes

Thresholds, sinais, provedores, revisão manual, retenção de dados e critérios de bloqueio.

