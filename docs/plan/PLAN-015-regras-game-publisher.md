# PLAN-015 — Regras por game e publisher

**Issue:** [#17](https://github.com/schambeck/api-marketplace/issues/17)

**PRD:** [`PRD-015`](../prd/PRD-015-regras-game-publisher.md) · **Dependências:** PRD-001, PRD-002, PRD-007 e PRD-008.

## Estado atual e alvo

O catálogo não configura regras específicas de contexto. O alvo é um `RuleSet` versionado por game/publisher/plataforma/região/servidor/produto/variante, aplicável em publicação, checkout e fulfillment.

## Abordagem técnica

- Modelar regras de métodos, campos, limites, janelas e restrições em formato validável e versionado.
- Criar engine de avaliação com resultado `ALLOW`, `BLOCK` ou `REVIEW`, incluindo motivos e versão.
- Integrar os três pontos de decisão sem duplicar lógica; salvar snapshot da versão no pedido/fulfillment.
- Validar expiração/conflito e fornecer tela/API operacional para aprovação/publicação de regras.
- Manter fonte oficial e processo de atualização fora do código, com adapter se houver provedor externo.

## Persistência e testes

Migration para rulesets, versões, escopo e auditoria. Testar publicação incompatível, checkout inválido, regra ausente/expirada/conflitante, mudança afetando somente novas vendas e explicação ao operador.

## Decisões e riscos

Fonte oficial, frequência, autoridade e precedência de conflitos precisam de decisão. Regra aplicada deve ser imutável no pedido, mesmo que o ruleset atual mude.
