# PLAN-002 — Oferta e disponibilidade

**Status:** Implementado no baseline; substituído para o MVP pelo plano corretivo [`PLAN-021`](PLAN-021-oferta-seller-mvp.md), em conjunto com [`PLAN-020`](PLAN-020-simplificacao-contexto-mvp.md).

**Prioridade original:** MVP. A oferta é a unidade vendável do seller.

**Issue:** [#4](https://github.com/schambeck/api-marketplace/issues/4)

**PRD:** [`PRD-002`](../prd/PRD-002-oferta-disponibilidade.md) · **Dependências:** PRD-001 e estoque/reserva mínima do PRD-004. O `PLAN-001` fornece o agregado `GameContext`; supply externo e regras por publisher não bloqueiam o MVP.

## Estado atual e alvo

`Product.price`, `quantity` e `inventoryStatus` são editáveis no mesmo agregado e o carrinho referencia diretamente o produto. O alvo é introduzir `Offer` como unidade vendável, com preço/contexto/método/fonte/status e transições explícitas, preservando compatibilidade de leitura durante a migração.

## Abordagem técnica

- Criar `offer` vinculada a um `Product` e diretamente ao seller/proprietário; status mínimo `DRAFT`, `PUBLISHED`, `PAUSED`, `RETIRED`.
- Implementar `OfferController`, service, repository, mapper e regras de publicação; publicação exige contexto válido e `ACTIVE`, preço, entrega e disponibilidade.
- Associar cada oferta a um `GameContext` ativo. A referência e o snapshot histórico completo podem ser simplificados no MVP e ampliados quando houver necessidade de preservar mudanças de catálogo.
- Alterar catálogo, `CartItem` e `CartItemService` para referenciar oferta e capturar preço no momento da adição.
- Revalidar oferta no checkout em transação e com locking/versão, evitando divergência entre catálogo e compra.
- Registrar o ator e o motivo das transições principais; o pedido deve guardar preço e dados essenciais, sem depender da oferta atualizada.

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

O `PLAN-003` não é pré-requisito para este plano no modelo inicial: a própria relação seller–oferta representa a origem do estoque.

## Decisões de implementação desta etapa

- `Product` permanece como item de catálogo e uma `Offer` pertence a exatamente um produto; um produto pode ter várias ofertas.
- A oferta guarda a referência ao `GameContext` e um snapshot dos seus atributos/versionamento. A publicação sempre revalida o contexto atual como `ACTIVE`.
- O preço da oferta é imutável para uma oferta publicada; novas condições devem ser representadas por outra oferta ou por alteração enquanto `DRAFT`.
- A disponibilidade usa `AVAILABLE`/`UNAVAILABLE`, enquanto o ciclo comercial usa `DRAFT`/`PUBLISHED`/`PAUSED`/`RETIRED`; `inventoryStatus` do produto não participa da decisão de checkout.
- A API de carrinho aceita `offerId`. `productId` permanece somente como compatibilidade de entrada e resolve uma única oferta publicada disponível; não há fallback silencioso para produto sem oferta.
- `CartItem.price` continua sendo o snapshot do preço no momento da inclusão. A criação da preferência revalida status, disponibilidade e contexto com lock pessimista.
- Mutação de oferta exige a role `ADMIN`; consulta de ofertas publicadas e catálogo permanece pública. Toda transição e alteração de disponibilidade registra ator, motivo e origem.

## Evidência esperada

- `./mvnw clean test -Dmaven.plugin.validation=BRIEF` comprova compilação e testes de regras de publicação/transição.
- As migrations `V12__create_offer.sql` e `V13__backfill_legacy_product_offers.sql` devem aplicar sobre as 11 migrations existentes; a segunda cria ofertas legadas idempotentes e mantém cart items legados sem oferta para migração controlada.
- Testes manuais/API devem cobrir publicação sem contexto ativo, indisponibilidade no carrinho, pausa/retirada, preço congelado e revalidação durante checkout.

## Limite de integração com pedidos

Este repositório ainda não possui `Order`, linhas de pedido ou uma transação de criação de pedido; o fluxo existente encerra na criação da preferência do Mercado Pago. O snapshot de contexto/preço dentro de pedido e o teste de pedido já criado permanecem como handoff explícito para o `PLAN-005`, enquanto esta etapa congela o preço em `CartItem` e revalida a oferta antes do checkout.

## Migração operacional e concorrência

`V13__backfill_legacy_product_offers.sql` cria um contexto `LEGACY` explícito, preserva o preço dos produtos, converte quantidade/status legados em disponibilidade e registra cada oferta criada no histórico. A migration é segura para reexecução lógica porque só insere o contexto se ausente e só cria oferta/histórico quando ainda não existem.

`OfferPostgresIntegrationTest` executa duas alterações concorrentes de disponibilidade sobre a mesma oferta PostgreSQL e verifica que ambas são serializadas pelo lock pessimista e deixam duas entradas auditáveis, sem perder uma das decisões.
