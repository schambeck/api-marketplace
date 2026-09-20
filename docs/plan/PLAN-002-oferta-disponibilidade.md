# PLAN-002 — Oferta e disponibilidade

**Issue:** [#4](https://github.com/schambeck/api-marketplace/issues/4)

**PRD:** [`PRD-002`](../prd/PRD-002-oferta-disponibilidade.md) · **Dependências:** PRD-001, PRD-003, PRD-004 e PRD-015. O `PLAN-001` fornece o agregado `GameContext`, seus estados e seu histórico.

## Estado atual e alvo

`Product.price`, `quantity` e `inventoryStatus` são editáveis no mesmo agregado e o carrinho referencia diretamente o produto. O alvo é introduzir `Offer` como unidade vendável, com preço/contexto/método/fonte/status e transições explícitas, preservando compatibilidade de leitura durante a migração.

## Abordagem técnica

- Criar `offer` e `offer_status_history`; status mínimo `DRAFT`, `PUBLISHED`, `PAUSED`, `RETIRED`.
- Implementar `OfferController`, service, repository, mapper e regras de publicação; publicação exige contexto válido e `ACTIVE`, preço, entrega e disponibilidade.
- Associar cada oferta a um `GameContext` por referência obrigatória e versionada; preservar snapshot da referência quando a oferta ou pedido precisar manter o contexto histórico.
- Alterar catálogo, `CartItem` e `CartItemService` para referenciar oferta e capturar preço no momento da adição.
- Revalidar oferta no checkout em transação e com locking/versão, evitando divergência entre catálogo e compra.
- Registrar ator, motivo e origem em toda transição; pedidos futuros devem guardar snapshot, não reler oferta.

## Persistência e contrato

Migration para oferta, histórico, índices de status/contexto e foreign keys. Atualizar schema e DTOs públicos; manter endpoint de produto como compatibilidade apenas enquanto o consumidor migrar, sem aceitar produto indisponível como substituto silencioso.

## Sequência

1. Decidir relação Product–Offer, preço, multiplicidade e forma da referência versionada ao `GameContext`.
2. Criar oferta e publicação com validação do contexto ativo e testes de transição.
3. Migrar catálogo/carrinho e adicionar revalidação no checkout.
4. Implementar pausa/retirada, auditoria e preservação do snapshot histórico.

## Validação e aceite

Testar publicação inválida, catálogo publicado, bloqueio de oferta indisponível, histórico após retirada e concorrência entre pausa/checkout. A evidência deve incluir teste de integração transacional mostrando que pedido já criado mantém preço antigo.

Também testar que uma oferta não pode ser publicada sem `GameContext`, nem com contexto `INACTIVE` ou `DISCONTINUED`, e que pedidos existentes continuam consultando o snapshot do contexto usado na compra.

## Riscos e decisões

O desenho definitivo depende de PRD-001/003/004 e da política de preço. Não reutilizar `inventoryStatus` como máquina de estados da oferta; qualquer compatibilidade temporária deve ser explicitamente marcada.
