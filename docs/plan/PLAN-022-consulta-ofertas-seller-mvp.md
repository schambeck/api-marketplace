# PLAN-022 — Consulta de ofertas do seller no MVP

**Tipo:** Plano corretivo sobre o `PLAN-019` já implementado.

**Prioridade:** MVP.

**Objetivo:** substituir a consulta administrativa ampla de ofertas por uma consulta segura e útil ao seller, mantendo no administrador somente a capacidade mínima necessária para moderação.

## Motivação

O `PLAN-019` implementou `GET /admin/offers` e `GET /admin/offers/{id}` para listar qualquer oferta em qualquer estado. Esse fluxo é orientado ao painel administrativo e não resolve a necessidade principal do MVP: o vendedor precisa consultar e acompanhar suas próprias ofertas para publicar, pausar, reativar, retirar e preparar a entrega.

A regra de propriedade do MVP é:

```text
Offer → Product → Product.user = seller autenticado
```

Uma consulta administrativa sem filtro de proprietário não deve ser usada pelo seller nem exposta como substituto de autorização de domínio.

## Estado implementado

Atualmente existem:

- `AdminOfferController` em `/admin/offers`;
- `GET /admin/offers` com filtro opcional de status e busca textual;
- `GET /admin/offers/{id}` para qualquer estado;
- `OfferService.searchAdmin` e `findAdminById`;
- `OfferRepository.searchAdmin`, sem filtro por seller;
- autorização exclusiva por `ROLE_ADMIN`;
- catálogo público separado em `GET /offers`, restrito a ofertas `PUBLISHED`.

## Estado alvo do MVP

O MVP deve expor dois fluxos de consulta distintos:

### Seller

```http
GET /seller/offers?status=DRAFT&page=0&size=25&search=elden
GET /seller/offers/{id}
```

- exige usuário autenticado;
- retorna somente ofertas cujo `Product.user.username` seja o usuário autenticado;
- permite consultar `DRAFT`, `PUBLISHED`, `PAUSED` e `RETIRED` próprias;
- não expõe ofertas de outro seller;
- usa os mesmos campos públicos da oferta, sem custo, supply ou dados financeiros.

### Administrador

- mantém apenas consulta individual de uma oferta para moderação, se necessária pela UI;
- ações de publicar, pausar, reativar, retirar e alterar disponibilidade continuam protegidas por `ADMIN` até que o PLAN-021 defina a autorização final do seller;
- a listagem administrativa ampla por status e busca não faz parte do MVP e deve ser removida ou mantida apenas como evolução futura.

O catálogo público continua sem alteração: só exibe ofertas `PUBLISHED` e disponíveis.

## Alterações previstas

1. Criar `SellerOfferController` com `GET /seller/offers` e `GET /seller/offers/{id}`.
2. Obter o username por `AuthenticationService` e passá-lo explicitamente ao serviço.
3. Criar `OfferService.searchOwned(username, pageable, status, search)` e `findOwnedById(username, id)`.
4. Criar consulta repository com filtro obrigatório por `offer.product.user.username`.
5. Remover `searchAdmin` e a listagem ampla de `AdminOfferController` do fluxo do MVP.
6. Manter, se necessário, `findAdminById` em uma rota de detalhe administrativa separada, sem reutilizá-la para sellers.
7. Retornar `404` para uma oferta inexistente ou que não pertença ao seller, evitando revelar a existência de ofertas de terceiros.
8. Usar a busca textual simplificada do PLAN-020, sem depender de snapshot versionado ou campos de supply.
9. Atualizar schema, OpenAPI e documentação do frontend para a nova rota seller.

## Autorização e isolamento

- seller autenticado só consulta ofertas dos próprios produtos;
- seller não pode usar `sellerId`, `userId` ou parâmetro equivalente para consultar outro proprietário;
- ADMIN pode consultar uma oferta para moderação, mas essa autorização não deve ser usada pelo seller;
- o filtro de proprietário deve estar no repository/service, não apenas no controller;
- o detalhe de uma oferta de outro seller deve responder `404`, não `403`, quando isso evitar enumeração de dados.

## Persistência e contrato

Não é necessária migration. A relação existente `Offer.product.user` é suficiente para o isolamento do MVP.

A consulta deve usar `join fetch` do produto e `countQuery` compatível com paginação. O filtro por status deve ser opcional para seller, mas sempre limitado ao proprietário. A busca deve cobrir somente os campos de produto e contexto mínimo definidos no PLAN-020.

## Integração com o PLAN-021

Este plano fornece a consulta para o seller; o PLAN-021 define quem pode executar as mutações da oferta. A implementação deve evitar duplicar regras de propriedade:

- leitura própria: PLAN-022;
- criação, edição e transições próprias: PLAN-021;
- consulta pública: `OfferController` existente;
- moderação administrativa: endpoints ADMIN existentes, reduzidos ao necessário.

## Testes e aceite

- seller lista somente suas ofertas em todos os estados comerciais;
- seller não enxerga ofertas de outro seller quando busca por status, texto ou ID;
- oferta inexistente e oferta de terceiro retornam `404` no detalhe seller;
- ADMIN consegue consultar o detalhe necessário para moderação;
- usuário anônimo recebe `401` na rota seller;
- catálogo público continua retornando somente ofertas publicadas e disponíveis;
- paginação, ordenação e busca textual permanecem determinísticas;
- não são retornados `availabilitySource`, custos, settlement ou dados de outro seller;
- repetição da consulta não altera estado nem cria histórico.

## Rollback

O rollback pode restaurar temporariamente a listagem `GET /admin/offers` enquanto a UI seller é migrada. A restauração não deve remover o filtro de proprietário das novas rotas seller nem permitir que um seller use a rota administrativa.

Não há alteração de banco. As mudanças são de controller, service, repository, contrato, autorização e testes.
