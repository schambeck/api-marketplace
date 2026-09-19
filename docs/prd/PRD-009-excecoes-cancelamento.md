# PRD-009 — Falhas, timeout, parcial e cancelamento

**Rastreabilidade:** BP-019/BP-020 · Exceptions · Importante.

## Problema e objetivo

Não há lifecycle para abandono, timeout, falha de entrega, falta de supply ou cancelamento, nem liberação de reserva. O produto deve encerrar ou recuperar fluxos sem deixar dinheiro, inventory ou trabalho em estado ambíguo.

## Requisitos

1. Definir timeout por etapa e registrar a origem da expiração.
2. Permitir retry, reprogramação, troca de source, ação do cliente, cancelamento ou revisão manual.
3. Cancelamento deve liberar inventory não consumido e encaminhar refund quando necessário.
4. Fulfillment parcial deve manter saldo pendente e decisão explícita sobre o restante.
5. Operações repetidas devem ser idempotentes.

## Critérios de aceite

- Carrinho, pedido e reserva expirados têm transição visível e recurso liberado.
- Falha de entrega cria próxima ação com prazo e responsável.
- Cancelamento não apaga evidências e não consome inventory indevidamente.
- Parcial não é marcado como concluído integralmente.

## Decisões pendentes

Prazos por produto; elegibilidade de cancelamento; suporte a entrega parcial; e uso de scheduler/eventos.

