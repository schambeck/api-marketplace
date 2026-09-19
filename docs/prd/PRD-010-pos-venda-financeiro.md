# PRD-010 — Refund, disputa e chargeback

**Rastreabilidade:** BP-021/BP-022 · After-sales · Importante.

## Problema e objetivo

Não há fluxo para devolver valores, investigar contestação ou responder chargeback. O produto deve ligar pedido, payment, entrega e evidências a uma decisão pós-venda auditável.

## Requisitos

1. Receber solicitação e determinar elegibilidade por motivo, estado e evidência.
2. Suportar aprovação, rejeição, refund integral/parcial e execução no gateway.
3. Abrir disputa/chargeback com prazo, responsáveis, evidências e decisão.
4. Atualizar pedido/payment somente após resposta idempotente do gateway.
5. Impedir refund duplicado e preservar impacto financeiro para reconciliação.

## Critérios de aceite

- Solicitação inelegível recebe motivo e não chama o gateway.
- Refund executado atualiza os agregados relacionados e registra o identificador externo.
- Chargeback congela ações conflitantes e permite anexar evidências.
- Falhas de gateway ficam pendentes para retry/revisão, sem falso sucesso.

## Decisões pendentes

Política local, prazos, refund parcial, responsabilidades, direitos do cliente e contrato do gateway.

