# BP-014 — Criar e administrar pedido a partir do checkout

**Classificação:** [FALTANTE — ESSENCIAL]

**Objetivo:** criar o registro comercial que representa a intenção de compra e coordena pagamento, fulfillment, entrega e pós-venda.

**Atores:** cliente, plataforma, operador e payment provider.

**Trigger:** confirmação final do checkout.

**Fluxo:** congelar linhas, preço, contexto, dados de entrega e termos; criar pedido; associar carrinho/preference; encaminhar para pagamento e risco; impedir que `Cart` seja a referência definitiva da venda.

**Estados:** `CREATED → PAYMENT_PENDING → PAID/REJECTED → FULFILLING → DELIVERED → COMPLETED`, com `CANCELLED`, `EXPIRED`, `REFUNDED`, `DISPUTED` e `PARTIALLY_DELIVERED` quando aplicável.

**Evidência:** **[EVIDÊNCIA INTERNA]** o fluxo atual termina na preference e usa `Cart.id` como `externalReference`; não existe entidade/processo de pedido.

**PRD relacionado:** [PRD-005](../../prd/PRD-005-pedido-lifecycle.md).
