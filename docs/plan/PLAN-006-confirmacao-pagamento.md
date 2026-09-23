# PLAN-006 — Confirmação de pagamento e webhook idempotente

**Prioridade:** MVP. Garantir que a confirmação do gateway não duplique nem altere pedidos incorretamente.

**Issue:** [#8](https://github.com/schambeck/api-marketplace/issues/8)

**PRD:** [`PRD-006`](../prd/PRD-006-confirmacao-pagamento.md) · **Dependências:** PRD-005 e contrato do Mercado Pago.

## Estado atual e alvo

`Webhook` e `Payment` são persistidos sem deduplicação, validação ou transição de pedido. O alvo é uma entrada autenticada, idempotente e auditável que atualiza payment/order atomicamente e publica o próximo evento.

## Abordagem técnica

- Modelar `PaymentEvent`/inbox com identificador externo único, hash/payload sanitizado, recebimento, processamento e erro.
- Validar assinatura, formato, referência e correspondência do valor/status antes de tocar no domínio.
- Criar estados mínimos de payment e adaptador do gateway; eventos fora de ordem devem ser ignorados ou encaminhados para revisão.
- Processar evento, payment e order na mesma transação. Outbox e publicação de eventos ficam para o plano de comunicação.
- Nunca logar tokens, assinaturas completas ou payloads sensíveis.

## Persistência e contrato

Migration com unique key do evento, estado de processamento, tentativas e vínculo payment/order. Preservar endpoint público `POST /webhooks/**`, retornando status compatível com retry do gateway.

## Validação

Testes de assinatura inválida, referência divergente, duplicata, reentrega concorrente, evento fora de ordem, aprovado e falha/retry. Teste de integração confirma que notificação inválida não altera order/inventory e aprovada dispara a etapa prevista.

## Decisões e riscos

Assinatura, fonte de verdade, estados e retry dependem da documentação/credenciais de teste do Mercado Pago. A operação deve ser tolerante a reentrega e preparada para consulta de reconciliação.
