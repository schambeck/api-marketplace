# BP-024 — Comunicar eventos relevantes ao cliente e operador

**Classificação:** [FALTANTE — IMPORTANTE]

**Objetivo:** informar mudanças que exigem ação ou definem expectativa, sem confundir canal de comunicação com processo de negócio.

**Atores:** plataforma, cliente, operador, supplier/seller e suporte.

**Trigger:** criação/pagamento do pedido, ação necessária, início/falha/conclusão de entrega, refund, disputa ou expiração.

**Resultado:** mensagem entregue ou registrada como falha, com preferência/canal e deduplicação suficientes para não gerar instruções conflitantes.

**Evidência:** **[EVIDÊNCIA INTERNA]** não há eventos de domínio nem notificações de lifecycle; o webhook é apenas persistido.

**PRD relacionado:** [PRD-012](../../prd/PRD-012-comunicacao.md).
