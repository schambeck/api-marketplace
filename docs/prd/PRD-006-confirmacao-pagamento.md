# PRD-006 — Confirmação de pagamento e webhook idempotente

**Rastreabilidade:** BP-007/BP-008/BP-015 · Payment · Essencial.

## Problema e objetivo

Webhook e `Payment` hoje são persistidos sem validação, deduplicação ou avanço de negócio. O produto deve transformar uma notificação confiável do gateway em decisão aplicável ao pedido.

## Requisitos

1. Validar autenticidade, formato, referência e correspondência com o pedido.
2. Deduplicar eventos por identificador e tratar reentrega sem efeitos duplicados.
3. Aceitar eventos fora de ordem somente quando a transição for válida; rejeitar regressões.
4. Atualizar payment e pedido de forma consistente e emitir o próximo evento de negócio.
5. Registrar payload/auditoria sem expor segredos.

## Critérios de aceite

- Webhook duplicado produz uma única mudança de estado.
- Notificação inválida não altera pedido nem inventory.
- Pagamento aprovado dispara a etapa de reserva/risco prevista.
- Status incompatível é enviado para retry ou revisão, com motivo rastreável.

## Decisões pendentes

Assinatura e contrato do Mercado Pago; fonte de verdade entre webhook e consulta; estados aceitos; política de retry e janela de reconciliação.

