# BP-028 — Operar fila de fulfillment e escalonamento

**Classificação:** [FALTANTE — IMPORTANTE]

**Objetivo:** coordenar trabalho humano quando a entrega não é totalmente automática.

**Atores:** operador, supervisor, suporte e supplier/seller.

**Trigger:** fulfillment criado, prioridade alterada, timeout, falha, cliente aguardando ou necessidade de evidência.

**Fluxo:** priorizar; atribuir; executar; registrar; reatribuir; escalar; encerrar. O resultado é responsabilidade operacional clara para cada fulfillment aberto.

**Evidência:** **[EVIDÊNCIA INTERNA]** não há work queue, assignment, scheduler ou operador documentado. **[EVIDÊNCIA EXTERNA]** a referência declara equipe global de serviço e delivery support ([About MMOEXP](https://www.mmoexp.com/About.html)).

**PRD relacionado:** [PRD-007](../../prd/PRD-007-fulfillment.md).
