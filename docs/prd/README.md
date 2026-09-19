# PRDs dos gaps de negócio

Este diretório transforma a **Matriz de completude e rastreabilidade de gaps** de [`docs/business/business-processes.md`](../business/business-processes.md) em requisitos de produto rastreáveis.

Os PRDs abaixo descrevem capacidades esperadas; não afirmam que elas já existam no backend. Cada documento mantém separadas evidência interna, decisões ainda necessárias e critérios de aceite.

| ID | Macroprocesso | Processo(s) | Relevância | PRD |
|---|---|---|---|---|
| PRD-001 | Catálogo | BP-010 | Essencial | [Contexto de game e variante](PRD-001-contexto-game-variante.md) |
| PRD-002 | Catálogo | BP-003/BP-011 | Essencial | [Oferta e disponibilidade](PRD-002-oferta-disponibilidade.md) |
| PRD-003 | Supply | BP-012 | Essencial | [Supply e aquisição](PRD-003-supply-aquisicao.md) |
| PRD-004 | Inventory | BP-013 | Essencial | [Reserva e consumo de inventory](PRD-004-inventory.md) |
| PRD-005 | Transaction | BP-014 | Essencial | [Pedido e lifecycle](PRD-005-pedido-lifecycle.md) |
| PRD-006 | Payment | BP-007/BP-008/BP-015 | Essencial | [Confirmação de pagamento](PRD-006-confirmacao-pagamento.md) |
| PRD-007 | Fulfillment | BP-016/BP-028 | Essencial | [Fulfillment e operação](PRD-007-fulfillment.md) |
| PRD-008 | Delivery | BP-017/BP-018 | Essencial/importante | [Instruções e evidência de entrega](PRD-008-entrega-evidencia.md) |
| PRD-009 | Exceptions | BP-019/BP-020 | Importante | [Falhas, timeout e cancelamento](PRD-009-excecoes-cancelamento.md) |
| PRD-010 | After-sales | BP-021/BP-022 | Importante | [Refund, disputa e chargeback](PRD-010-pos-venda-financeiro.md) |
| PRD-011 | Support | BP-023 | Importante | [Suporte e casos](PRD-011-suporte.md) |
| PRD-012 | Communication | BP-024 | Importante | [Comunicação de eventos](PRD-012-comunicacao.md) |
| PRD-013 | Finance | BP-025 | Importante | [Reconciliação e settlement](PRD-013-reconciliacao.md) |
| PRD-014 | Risk | BP-026 | Importante | [Risco e revisão manual](PRD-014-risco.md) |
| PRD-015 | Rules | BP-027 | Importante | [Regras por game/publisher](PRD-015-regras-game-publisher.md) |
| PRD-016 | Reputation | BP-029 | Evolutivo | [Reviews e reputação](PRD-016-reviews-reputacao.md) |

O baseline de identidade (BP-001) permanece fora desta lista porque a matriz o classifica como adequadamente coberto. O processo BP-009, OAuth Mercado Pago, também não é um gap da matriz; suas lacunas de segurança continuam registradas na documentação de negócio.

