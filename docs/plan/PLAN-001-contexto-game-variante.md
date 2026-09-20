# PLAN-001 — Contexto de game e variante

**Issue:** [#3](https://github.com/schambeck/api-marketplace/issues/3)

**PRD:** [`PRD-001`](../prd/PRD-001-contexto-game-variante.md) · **Dependências:** nenhuma de negócio; base para PRD-002, PRD-004, PRD-008 e PRD-015.

## Estado atual e alvo

`Product` mistura catálogo, preço, quantidade, status e imagem e não possui game, publisher, plataforma, região, servidor ou variante. O alvo é separar um agregado `GameContext` versionado de uma futura `Offer`, mantendo referências históricas mesmo após descontinuação.

## Abordagem técnica

- Criar `game_context` com atributos normalizados, status (`ACTIVE`, `INACTIVE`, `DISCONTINUED`), versão, auditoria e restrições de compatibilidade.
- Criar `GameContextController`, `GameContextService`, repository, mapper e DTOs `GameContextWeb`; restringir mutações por papel.
- Validar campos obrigatórios e combinações incompatíveis em serviço transacional, com erros de domínio acionáveis.
- Definir o contexto como referência estável para ofertas e pedidos, sem apagar registros que possam ser referenciados historicamente.
- Manter `Product` sem regras duplicadas de contexto; a associação concreta com `Offer` será tratada no `PLAN-002`.

## Persistência e contrato

Adicionar migration Flyway para tabela, índices de busca por game/platform/region/status e tabela de histórico/auditoria, se a auditoria não puder ser atendida pelo log de domínio. Atualizar `schema/api-schema-marketplace.json` somente para entidades e endpoints aprovados.

## Sequência

1. Resolver campos obrigatórios por game e vocabulário de entidades.
2. Implementar entidade, migration, validação e consulta pública.
3. Entregar ao `PLAN-002` o contrato de referência, status e validação de publicação do contexto.
4. Adicionar histórico e bloqueio de novos usos para contexto descontinuado.

## Validação e aceite

- Testes de unidade cobrem combinações incompletas/incompatíveis e transições de status.
- Testes de integração provam consulta de contexto ativo e retenção de contexto referenciado.
- Teste de contrato comprova que catálogo diferencia produto, contexto e variante.
- AC1/AC2: respostas 4xx com motivo e associação consultável; AC3: publicação bloqueada sem apagar histórico; AC4: payload público contém os três níveis.

## Riscos e decisões

As decisões sobre campos obrigatórios, entidades controladas, publisher terms e preço/inventory próprio da variante bloqueiam o modelo final. Mitigação: manter valores extensíveis, versionar regras e não embutir termos externos em enums rígidos.

## Decisões de implementação desta etapa

- `game` e `platform` são obrigatórios; `publisher`, `region`, `server` e `variant` permanecem extensíveis como texto até a definição dos vocabulários controlados.
- `server` exige `region`; contextos iniciam como `ACTIVE` quando o status não é informado.
- `DISCONTINUED` é terminal e não permite alterações; contextos inativos/descontinuados permanecem armazenados para referências históricas.
- Toda criação/alteração grava um snapshot em `game_context_history`, incluindo o usuário autenticado quando disponível.
- `GameContextService.referenceForOfferPublication` devolve uma referência imutável com `id`, `version` e snapshot dos atributos para consumidores de oferta/pedido.
- `GameContextService.history` permite consultar os snapshots auditáveis sem apagar o contexto original.
- `GET /game-contexts` é público e retorna somente contextos `ACTIVE`; mutações exigem a role JWT `ADMIN`.
- A associação concreta com `Product`/`Offer`, a validação durante publicação e o snapshot consumido por pedidos pertencem ao `PLAN-002`.

## Handoff para PLAN-002

O `PLAN-002` deve consumir o `GameContext` por uma referência obrigatória e versionada, validar que o contexto está `ACTIVE` e preservar a referência/snapshot quando uma oferta ou pedido for criado. O `PLAN-001` não implementa `Offer`, catálogo, carrinho ou checkout.

## Evidência de validação

- Testes unitários cobrem criação ativa com auditoria, rejeição de servidor sem região, transição de status, bloqueio de contextos inativos/descontinuados e referência versionada para publicação.
- `GameContextPostgresIntegrationTest` usa Testcontainers PostgreSQL 17.3, executa as 11 migrations Flyway e prova a retenção do contexto descontinuado e dos snapshots em `game_context_history`.
- `mvn clean test -Dmaven.plugin.validation=BRIEF` valida compilação, testes unitários e integração; o teste requer Docker disponível.
- `mvn package -DskipTests` valida a geração do artefato; a aplicação de `V11__create_game_context.sql` requer PostgreSQL disponível.
