# PLAN-010 — Refund, disputa e chargeback

**Issue:** [#12](https://github.com/schambeck/api-marketplace/issues/12)

**PRD:** [`PRD-010`](../prd/PRD-010-pos-venda-financeiro.md) · **Dependências:** PRD-005, PRD-006, PRD-008, PRD-009 e contrato do gateway.

## Estado atual e alvo

Não há pós-venda financeiro. O alvo é `RefundRequest` e `DisputeCase` auditáveis, com elegibilidade, aprovação, execução idempotente no gateway e atualização dos agregados somente após confirmação externa.

## Abordagem técnica

- Definir política de elegibilidade por estado, motivo, entrega e valor; rejeitar antes de chamar gateway quando inelegível.
- Persistir solicitação, decisão, valor integral/parcial, external id, tentativas e resposta sanitizada.
- Congelar ações conflitantes durante disputa/chargeback e permitir anexar evidências com controle de acesso.
- Criar adapters de gateway com retry seguro; nunca declarar sucesso com base apenas no envio.
- Emitir eventos para order/payment/reconciliation após confirmação idempotente.

## Persistência e validação

Migration com refunds, refund attempts, disputes, evidence links e constraints contra duplicidade. Testes cobrem inelegibilidade, integral/parcial, resposta duplicada, falha gateway e disputa congelando ações.

## Decisões e riscos

Política local, prazos, papéis e contrato gateway são bloqueadores de regra. Valores devem usar moeda/escala definida e ser reconciliáveis; nenhum ajuste deve apagar o fato financeiro original.
