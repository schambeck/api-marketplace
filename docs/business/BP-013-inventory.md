# BP-013 — Reservar, alocar e consumir inventory

**Classificação:** [FALTANTE — ESSENCIAL]

**Objetivo:** impedir overselling e vincular supply disponível a um pedido/fulfillment.

**Atores:** plataforma, operador, inventory service e supplier/seller quando aplicável.

**Trigger:** pedido pago/aprovado ou decisão de iniciar fulfillment.

**Fluxo:** verificar disponibilidade; reservar; alocar uma fonte; expirar ou liberar reserva quando necessário; consumir após entrega confirmada; reconciliar saldo.

**Estados/eventos:** `AVAILABLE → RESERVED → ALLOCATED → CONSUMED`, com `RELEASED` e `EXPIRED`; `InventoryReserved`, `InventoryReleased`, `InventoryConsumed`.

**Evidência:** **[EVIDÊNCIA INTERNA]** `Product.quantity` e `inventoryStatus` são apenas dados editáveis, sem transição, reserva ou baixa. **[EVIDÊNCIA EXTERNA]** a referência descreve stock lock, allocation e decremento em tempo real para contas ([produto Steam](https://www.mmoexp.com/Account/Steam/169425005.html)).

**PRD relacionado:** [PRD-004](../../prd/PRD-004-inventory.md).
