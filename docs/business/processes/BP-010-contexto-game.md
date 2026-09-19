# BP-010 — Configurar contexto de game, plataforma, região e variante

**Classificação:** [FALTANTE — ESSENCIAL]

**Objetivo:** determinar o contexto no qual um item ou serviço pode ser vendido e entregue.

**Atores:** administrador de catálogo, operador e cliente; game/publisher como restrição externa.

**Trigger:** onboarding ou alteração de game, plataforma, região, servidor, categoria, tipo de produto ou variante.

**Fluxo e resultado:** cadastrar o contexto válido; associar atributos obrigatórios, métodos de entrega e restrições; publicar somente combinações válidas. O resultado é uma definição de produto/variante que pode ser precificada e ofertada.

**Estados/eventos:** `DRAFT → ACTIVE → INACTIVE`; `GameContextConfigured`.

**Exception paths:** combinação incompatível, contexto descontinuado, regra do publisher não confirmada.

**Evidência:** **[EVIDÊNCIA INTERNA]** `Product` possui campos genéricos, sem modelar game/platform/server ou dados de entrega. **[EVIDÊNCIA EXTERNA]** páginas públicas da MMOExp organizam catálogo por games, produtos, plataformas e tipos de serviço ([home](https://www.mmoexp.com/), [delivery times](https://www.mmoexp.com/About/how-long-will-it-take-to-receive-my-order-after-payment.html)). **[A VALIDAR]** quais games, plataformas e campos serão suportados.

**PRD relacionado:** [PRD-001](../../prd/PRD-001-contexto-game-variante.md).
