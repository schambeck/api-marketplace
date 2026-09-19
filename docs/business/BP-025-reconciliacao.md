# BP-025 — Reconciliar pagamentos, entregas, inventory e valores devidos

**Classificação:** [FALTANTE — IMPORTANTE]

**Objetivo:** comparar o esperado com o efetivamente confirmado e corrigir divergências.

**Atores:** financeiro, operador, plataforma, payment provider e supplier/seller se aplicável.

**Trigger:** fechamento periódico, callback, refund, chargeback, divergência manual ou falha de integração.

**Fluxos:** orders × gateway; orders × deliveries; inventory × deliveries; refunds × gateway; supplier activity × payable. Registrar diferença, investigar, ajustar e fechar o caso.

**Evidência:** **[EVIDÊNCIA INTERNA]** não há ledger, reconciliation, settlement ou payout. **[A VALIDAR]** se o modelo terá supplier/seller payable e qual nível contábil é necessário.

**PRD relacionado:** [PRD-013](../../prd/PRD-013-reconciliacao.md).
