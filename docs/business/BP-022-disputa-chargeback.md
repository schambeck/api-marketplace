# BP-022 — Tratar disputa e chargeback

```mermaid
flowchart TD
    A[Dispute / chargeback] --> B[Abrir caso e congelar decisões]
    B --> C[Coletar evidências de order, payment e delivery]
    C --> D{Decisão}
    D -- contestar --> E[Responder ao gateway]
    D -- refund --> F[Executar refund]
    D -- escalar --> G[Revisão especializada]
    E --> H[Reconciliar impacto]
    F --> H
    G --> H
```

**Classificação:** [FALTANTE — IMPORTANTE]

**Objetivo:** investigar reclamações e reversões iniciadas pelo cliente ou pelo provedor financeiro, preservando evidência e impacto financeiro.

**Atores:** cliente, suporte, risco, financeiro, payment provider e supplier/seller.

**Trigger:** dispute, chargeback, contestação de entrega ou alerta do gateway.

**Fluxo:** abrir caso; congelar decisões conflitantes; coletar order/payment/delivery evidence; decidir reject/retry/partial refund/full refund/escalation; responder ao gateway; reconciliar impacto e risco do cliente/source.

**Evidência:** **[EVIDÊNCIA INTERNA]** não há dispute, chargeback, evidência ou case management. **[A VALIDAR]** prazos, direitos, responsabilidade e política de risco.

**PRD relacionado:** [PRD-010](../../prd/PRD-010-pos-venda-financeiro.md).
