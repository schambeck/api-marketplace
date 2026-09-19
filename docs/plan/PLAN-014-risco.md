# PLAN-014 — Risco e revisão manual

**Issue:** [#16](https://github.com/schambeck/api-marketplace/issues/16)

**PRD:** [`PRD-014`](../prd/PRD-014-risco.md) · **Dependências:** PRD-005, PRD-006, PRD-007, PRD-009 e autorização operacional.

## Estado atual e alvo

Não há avaliação de risco. O alvo é avaliar sinais versionados antes do fulfillment, produzir decisão `ALLOW`, `HOLD`, `BLOCK` ou `ESCALATE` e permitir revisão manual segregada e auditável.

## Abordagem técnica

- Criar `RiskPolicy` versionada, `RiskSignal`, `RiskAssessment` e `ManualReview` com snapshot da política aplicada.
- Implementar motor determinístico com adapter para sinais externos; ausência/erro do provedor deve seguir política explícita.
- Bloquear fulfillment enquanto `HOLD`; exigir motivo e papel autorizado para liberar, bloquear ou escalar.
- Persistir evidências minimizadas e impedir reprocessamento de uma mesma decisão sem chave idempotente.
- Emitir somente transições permitidas para order/fulfillment.

## Persistência e testes

Migration para políticas, sinais, avaliações e revisões. Testar hold, decisão manual, segregação, política versionada, repetição e falha de provedor.

## Decisões e riscos

Thresholds, sinais, retenção e bloqueio são políticas de negócio sensíveis. Não hardcodar scores no domínio; versionar configuração e exigir auditoria para alterações.
