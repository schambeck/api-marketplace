# BP-019 — Tratar falha, timeout e fulfillment parcial

```mermaid
flowchart TD
    A[Falha, timeout ou entrega parcial] --> B{Próxima ação}
    B -- retry --> C[Reexecutar]
    B -- reschedule --> D[Agendar nova tentativa]
    B -- trocar source --> E[Reatribuir fulfillment]
    B -- cancel/refund --> F[Cancelar e reconciliar]
    B -- revisão --> G[Escalar para operador]
```

**Classificação:** [FALTANTE — IMPORTANTE]; **[A VALIDAR]** se quantidades parciais serão permitidas.

**Objetivo:** decidir o próximo passo quando a entrega não ocorre, demora ou ocorre apenas parcialmente.

**Atores:** operador, cliente, supplier/seller, suporte e plataforma.

**Trigger:** erro de entrega, timeout, falta de estoque, manutenção do game, informação incorreta ou entrega parcial.

**Decisões:** retry, reschedule, pedir ação do cliente, trocar source/método, completar saldo remanescente, cancelar, refund ou revisão manual.

**Evidência:** **[EVIDÊNCIA EXTERNA]** a política pública da MMOExp trata atraso por informação incorreta, falta de stock e refund; seus tempos também variam por produto ([refund policy](https://www.mmoexp.com/Refund.html), [delivery times](https://www.mmoexp.com/About/how-long-will-it-take-to-receive-my-order-after-payment.html)). Não se importam os prazos ou políticas da referência.

**PRD relacionado:** [PRD-009](../../prd/PRD-009-excecoes-cancelamento.md).
