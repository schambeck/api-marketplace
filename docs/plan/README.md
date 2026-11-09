# Planos de implementação dos PRDs

Este diretório contém o plano técnico de cada PRD em [`docs/prd`](../prd/README.md). O PRD é a autoridade sobre comportamento e critérios de aceite; cada plano descreve uma possível implementação incremental no backend atual.

Para o modelo inicial — seller anunciando e entregando o próprio estoque — a ordem recomendada é:

1. MVP de catálogo e venda: PLAN-001 reduzido, PLAN-002, PLAN-004 reduzido, PLAN-005 e PLAN-006.
2. Entrega e exceções: PLAN-007, PLAN-008 e PLAN-009, com escopo mínimo para entrega digital ou manual simples.
3. Evolução operacional: planos em [`future/`](future/), ativados conforme escala, repasses, fraude, suporte ou múltiplas fontes.

O PLAN-003 não é dependência do MVP: a oferta aponta diretamente para o seller e o inventory pertence a essa oferta.

Os planos `PLAN-001` e `PLAN-002` registram a implementação original. Como essa implementação contém versionamento, snapshots, supply e aprovação administrativa além do necessário, os ajustes do MVP devem seguir os planos corretivos abaixo.

## Planos corretivos do MVP

| Plano | Documento | Base |
|---|---|---|
| 020 | [Simplificação do contexto de game](PLAN-020-simplificacao-contexto-mvp.md) | PLAN-001 |
| 021 | [Oferta pertencente ao seller](PLAN-021-oferta-seller-mvp.md) | PLAN-002 |
| 022 | [Consulta de ofertas do seller](PLAN-022-consulta-ofertas-seller-mvp.md) | PLAN-019 |
| 023 | [Migração de MinIO para RustFS](PLAN-023-migracao-rustfs.md) | Infraestrutura de object storage |

Como regra, cada entrega deve introduzir uma migration Flyway nova, manter histórico/auditoria, atualizar os objetos HTTP e o schema JSON quando houver contrato público, e acrescentar testes de concorrência/idempotência nos fluxos que alteram estado. As decisões pendentes de cada PRD precisam ser resolvidas antes de congelar o modelo de dados ou o contrato público correspondente.

| PRD | Plano |
|---|---|
| 001 | [Contexto de game e variante](PLAN-001-contexto-game-variante.md) |
| 002 | [Oferta e disponibilidade](PLAN-002-oferta-disponibilidade.md) |
| 004 | [Inventory](PLAN-004-inventory.md) |
| 005 | [Pedido e lifecycle](PLAN-005-pedido-lifecycle.md) |
| 006 | [Confirmação de pagamento](PLAN-006-confirmacao-pagamento.md) |
| 007 | [Fulfillment](PLAN-007-fulfillment.md) |
| 008 | [Entrega e evidência](PLAN-008-entrega-evidencia.md) |
| 009 | [Exceções e cancelamento](PLAN-009-excecoes-cancelamento.md) |
| 017 | [Migração para Java 21 — concluído](PLAN-017-java-21-migration.md) |
| 018 | [Busca textual de ofertas — concluído](PLAN-018-busca-textual-ofertas.md) |
| 019 | [Consulta administrativa de ofertas — concluído](PLAN-019-admin-ofertas.md) |

## Planos futuros

| Plano | Documento |
|---|---|
| 003 | [Supply e aquisição](future/PLAN-003-supply-aquisicao.md) |
| 010 | [Pós-venda financeiro](future/PLAN-010-pos-venda-financeiro.md) |
| 011 | [Suporte](future/PLAN-011-suporte.md) |
| 012 | [Comunicação](future/PLAN-012-comunicacao.md) |
| 013 | [Reconciliação](future/PLAN-013-reconciliacao.md) |
| 014 | [Risco](future/PLAN-014-risco.md) |
| 015 | [Regras por game/publisher](future/PLAN-015-regras-game-publisher.md) |
| 016 | [Reviews e reputação](future/PLAN-016-reviews-reputacao.md) |
