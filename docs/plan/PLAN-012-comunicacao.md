# PLAN-012 — Comunicação de eventos

**Issue:** [#14](https://github.com/schambeck/api-marketplace/issues/14)

**PRD:** [`PRD-012`](../prd/PRD-012-comunicacao.md) · **Dependências:** PRD-005, PRD-006, PRD-007, PRD-009 e PRD-010.

## Estado atual e alvo

Não existem notificações de lifecycle. O alvo é uma outbox de eventos e `NotificationDelivery` por destinatário/canal/template/version, com deduplicação e retry isolado da transação de negócio.

## Abordagem técnica

- Definir eventos elegíveis: pedido, pagamento, ação necessária, entrega, refund, disputa e expiração.
- Persistir evento/outbox e entrega com unique key por evento/canal/template; consumir com lease e tentativas.
- Criar adapter de canal e catálogo de templates versionados, com dados mínimos e preferências/opt-out.
- Expor consulta de comunicações do próprio pedido e operação de reenvio seguro.
- Não chamar provedor externo dentro da transação principal; marcar falha e observar.

## Persistência e validação

Migration para outbox, templates, preferências e entregas. Testar no máximo uma entrega lógica por canal/versão, retry, reenvio, falha sem rollback de negócio e autorização da consulta.

## Decisões e riscos

Canais, idioma, provedor e política de retry devem ser definidos. O principal risco é duplicidade em reprocessamento; constraints e chave idempotente são obrigatórias.
