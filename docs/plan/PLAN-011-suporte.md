# PLAN-011 — Suporte e casos

**PRD:** [`PRD-011`](../prd/PRD-011-suporte.md) · **Dependências:** PRD-005, PRD-007, PRD-010 e autorização existente.

## Estado atual e alvo

Não há ticket ou SLA. O alvo é `SupportCase` com cliente, pedido opcional, categoria, prioridade, canal, estado e histórico imutável de mensagens/decisões.

## Abordagem técnica

- Criar estados `OPEN`, `CLASSIFIED`, `ASSIGNED`, `WAITING`, `IN_PROGRESS`, `RESOLVED`, `CLOSED` e reabertura controlada.
- Persistir `CaseMessage`, assignment, SLA/deadline e links para refund/dispute/fulfillment.
- Implementar APIs separadas para cliente e operador, filtrando dados por papel e contexto.
- Registrar causa, ação e ator em cada transição; suportar classificação e prioridade sem editar o histórico.

## Persistência e testes

Migration para casos, mensagens, atribuições, deadlines e vínculos. Testar criação, classificação, SLA, reabertura, resolução vinculada e isolamento de conteúdo sensível.

## Aceite e decisões

Os critérios serão cobertos por contrato/API e testes de autorização. Canais, horário, integração e papéis precisam ser definidos antes de acoplar um provedor externo; o núcleo deve permanecer independente.

