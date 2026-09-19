# PRD-011 — Suporte e casos

**Rastreabilidade:** BP-023 · Support · Importante.

## Problema e objetivo

Não há ticket, fila ou SLA para dúvidas, atraso, falha de pagamento, entrega, refund ou conta. O produto deve dar rastreabilidade ao atendimento pré-venda, venda e pós-venda.

## Requisitos

1. Criar caso com cliente, pedido quando aplicável, categoria, prioridade e canal.
2. Suportar `OPEN`, `CLASSIFIED`, `ASSIGNED`, `WAITING`, `IN_PROGRESS`, `RESOLVED` e `CLOSED`, com reabertura.
3. Registrar mensagens, decisões, responsáveis, SLA e histórico imutável.
4. Restringir dados sensíveis por papel e contexto operacional.

## Critérios de aceite

- Uma solicitação vira caso rastreável e recebe classificação/prioridade.
- Caso atribuído tem responsável e prazo visíveis.
- Resolução registra causa, ação e vínculos com refund/dispute/fulfillment.
- Cliente e operador veem apenas o conteúdo permitido por seu papel.

## Decisões pendentes

Canais, SLA, horário de atendimento, integração externa e papéis de suporte.

