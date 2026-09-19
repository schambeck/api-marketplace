# PLAN-013 — Reconciliação e settlement

**PRD:** [`PRD-013`](../prd/PRD-013-reconciliacao.md) · **Dependências:** PRD-005, PRD-006, PRD-008, PRD-010 e decisão de supplier/seller.

## Estado atual e alvo

Não há ledger nem execução de reconciliação. O alvo é uma execução versionada por período que compara pares de fontes, gera itens conciliados/divergentes/não verificáveis e permite fechamento com aceite explícito.

## Abordagem técnica

- Definir moeda, timezone, granularidade e fontes de verdade para order-gateway, delivery, inventory-delivery e refund-gateway.
- Criar `ReconciliationRun`, `ReconciliationItem`, severidade, owner, evidence, adjustment e close decision.
- Usar chave determinística de período/escopo para reprocessamento idempotente; ajustes são lançamentos novos, nunca edição destrutiva.
- Se marketplace/híbrido for aprovado, adicionar cálculo de payable/settlement por supplier em módulo separado.
- Expor operação de execução, investigação e fechamento com segregação de função.

## Persistência e testes

Migration para runs/items/adjustments e índices de origem. Testar execução, divergência, não verificável, reprocessamento, vínculo de evidência e bloqueio de fechamento por item crítico.

## Decisões e riscos

Sem política contábil e regras de taxa não se deve prometer settlement. O plano inicial pode entregar reconciliação operacional, deixando lançamentos financeiros formais atrás de decisão explícita.

