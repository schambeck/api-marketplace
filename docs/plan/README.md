# Planos de implementação dos PRDs

Este diretório contém o plano técnico de cada PRD em [`docs/prd`](../prd/README.md). O PRD é a autoridade sobre comportamento e critérios de aceite; cada plano descreve uma possível implementação incremental no backend atual.

Os planos foram organizados por dependência de domínio, não apenas pela numeração dos documentos:

1. Fundamentos de catálogo: PRD-001, PRD-002 e PRD-015.
2. Supply e disponibilidade operacional: PRD-003 e PRD-004.
3. Transação e pagamento: PRD-005 e PRD-006.
4. Entrega e exceções: PRD-007, PRD-008 e PRD-009.
5. Pós-venda e controles: PRD-010, PRD-011, PRD-012, PRD-013 e PRD-014.
6. Reputação: PRD-016.

Como regra, cada entrega deve introduzir uma migration Flyway nova, manter histórico/auditoria, atualizar os objetos HTTP e o schema JSON quando houver contrato público, e acrescentar testes de concorrência/idempotência nos fluxos que alteram estado. As decisões pendentes de cada PRD precisam ser resolvidas antes de congelar o modelo de dados ou o contrato público correspondente.

| PRD | Plano |
|---|---|
| 001 | [Contexto de game e variante](PLAN-001-contexto-game-variante.md) |
| 002 | [Oferta e disponibilidade](PLAN-002-oferta-disponibilidade.md) |
| 003 | [Supply e aquisição](PLAN-003-supply-aquisicao.md) |
| 004 | [Inventory](PLAN-004-inventory.md) |
| 005 | [Pedido e lifecycle](PLAN-005-pedido-lifecycle.md) |
| 006 | [Confirmação de pagamento](PLAN-006-confirmacao-pagamento.md) |
| 007 | [Fulfillment](PLAN-007-fulfillment.md) |
| 008 | [Entrega e evidência](PLAN-008-entrega-evidencia.md) |
| 009 | [Exceções e cancelamento](PLAN-009-excecoes-cancelamento.md) |
| 010 | [Pós-venda financeiro](PLAN-010-pos-venda-financeiro.md) |
| 011 | [Suporte](PLAN-011-suporte.md) |
| 012 | [Comunicação](PLAN-012-comunicacao.md) |
| 013 | [Reconciliação](PLAN-013-reconciliacao.md) |
| 014 | [Risco](PLAN-014-risco.md) |
| 015 | [Regras por game/publisher](PLAN-015-regras-game-publisher.md) |
| 016 | [Reviews e reputação](PLAN-016-reviews-reputacao.md) |

