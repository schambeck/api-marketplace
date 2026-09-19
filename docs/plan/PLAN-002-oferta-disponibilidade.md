# PLAN-002 — Oferta e disponibilidade

**PRD:** [`PRD-002`](../prd/PRD-002-oferta-disponibilidade.md) · **Dependências:** PRD-001, PRD-003, PRD-004 e PRD-015.

## Estado atual e alvo

`Product.price`, `quantity` e `inventoryStatus` são editáveis no mesmo agregado e o carrinho referencia diretamente o produto. O alvo é introduzir `Offer` como unidade vendável, com preço/contexto/método/fonte/status e transições explícitas, preservando compatibilidade de leitura durante a migração.

## Abordagem técnica

- Criar `offer` e `offer_status_history`; status mínimo `DRAFT`, `PUBLISHED`, `PAUSED`, `RETIRED`.
- Implementar `OfferController`, service, repository, mapper e regras de publicação; publicação exige contexto válido, preço, entrega e disponibilidade.
- Alterar catálogo, `CartItem` e `CartItemService` para referenciar oferta e capturar preço no momento da adição.
- Revalidar oferta no checkout em transação e com locking/versão, evitando divergência entre catálogo e compra.
- Registrar ator, motivo e origem em toda transição; pedidos futuros devem guardar snapshot, não reler oferta.

## Persistência e contrato

Migration para oferta, histórico, índices de status/contexto e foreign keys. Atualizar schema e DTOs públicos; manter endpoint de produto como compatibilidade apenas enquanto o consumidor migrar, sem aceitar produto indisponível como substituto silencioso.

## Sequência

1. Decidir relação Product–Offer, preço e multiplicidade.
2. Criar oferta e publicação com testes de transição.
3. Migrar catálogo/carrinho e adicionar revalidação no checkout.
4. Implementar pausa/retirada e auditoria.

## Validação e aceite

Testar publicação inválida, catálogo publicado, bloqueio de oferta indisponível, histórico após retirada e concorrência entre pausa/checkout. A evidência deve incluir teste de integração transacional mostrando que pedido já criado mantém preço antigo.

## Riscos e decisões

O desenho definitivo depende de PRD-001/003/004 e da política de preço. Não reutilizar `inventoryStatus` como máquina de estados da oferta; qualquer compatibilidade temporária deve ser explicitamente marcada.

