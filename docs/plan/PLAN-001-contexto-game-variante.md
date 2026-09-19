# PLAN-001 — Contexto de game e variante

**Issue:** [#3](https://github.com/schambeck/api-marketplace/issues/3)

**PRD:** [`PRD-001`](../prd/PRD-001-contexto-game-variante.md) · **Dependências:** nenhuma de negócio; base para PRD-002, PRD-004, PRD-008 e PRD-015.

## Estado atual e alvo

`Product` mistura catálogo, preço, quantidade, status e imagem e não possui game, publisher, plataforma, região, servidor ou variante. O alvo é separar um agregado `GameContext` versionado de uma futura `Offer`, mantendo referências históricas mesmo após descontinuação.

## Abordagem técnica

- Criar `game_context` com atributos normalizados, status (`ACTIVE`, `INACTIVE`, `DISCONTINUED`), versão, auditoria e restrições de compatibilidade.
- Criar `GameContextController`, `GameContextService`, repository, mapper e DTOs `GameContextWeb`; restringir mutações por papel.
- Validar campos obrigatórios e combinações incompatíveis em serviço transacional, com erros de domínio acionáveis.
- Usar referência imutável/versionada quando uma oferta ou pedido consumir o contexto; nunca apagar contexto referenciado.
- Evoluir `Product` apenas para associação ao contexto quando o desenho de `Offer` for aprovado; não duplicar regras em `Product`.

## Persistência e contrato

Adicionar migration Flyway para tabela, índices de busca por game/platform/region/status e tabela de histórico/auditoria, se a auditoria não puder ser atendida pelo log de domínio. Atualizar `schema/api-schema-marketplace.json` somente para entidades e endpoints aprovados.

## Sequência

1. Resolver campos obrigatórios por game e vocabulário de entidades.
2. Implementar entidade, migration, validação e consulta pública.
3. Integrar associação na publicação de oferta.
4. Adicionar histórico e bloqueio de novos usos para contexto descontinuado.

## Validação e aceite

- Testes de unidade cobrem combinações incompletas/incompatíveis e transições de status.
- Testes de integração provam consulta de contexto ativo e retenção de contexto referenciado.
- Teste de contrato comprova que catálogo diferencia produto, contexto e variante.
- AC1/AC2: respostas 4xx com motivo e associação consultável; AC3: publicação bloqueada sem apagar histórico; AC4: payload público contém os três níveis.

## Riscos e decisões

As decisões sobre campos obrigatórios, entidades controladas, publisher terms e preço/inventory próprio da variante bloqueiam o modelo final. Mitigação: manter valores extensíveis, versionar regras e não embutir termos externos em enums rígidos.
