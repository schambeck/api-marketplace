# PLAN-022 — Consulta de ofertas do seller no MVP

**Tipo:** Plano corretivo sobre o `PLAN-019` já implementado.

**Status:** IMPLEMENTADO parcialmente no backend; a remoção de `GET /admin/offers` permanece pendente da migração do frontend e a consolidação final dos PLAN-020/021.

**Prioridade:** MVP.

**Objetivo:** substituir a consulta administrativa ampla de ofertas por uma consulta segura e útil ao seller, mantendo no administrador somente a capacidade mínima necessária para moderação.

## Decisão de escopo e pré-condições

Este plano não deve ser implementado isoladamente contra o contrato legado atual. No estado do repositório, `OfferWeb` ainda expõe `gameContextId`, `context` e `availabilitySource`, enquanto os PLAN-020/021 definem a remoção desses campos do contrato do MVP. A implementação deve seguir esta ordem:

1. aplicar ou concluir o contrato de oferta dos PLAN-020/021, incluindo o conjunto final de campos de leitura e a autorização de propriedade;
2. implementar as consultas próprias deste plano sobre esse contrato;
3. migrar o consumidor frontend para `/seller/offers` antes de remover a listagem administrativa ampla.

Se a equipe precisar entregar a consulta seller antes dos PLAN-020/021, deverá ser criada uma decisão explícita de compatibilidade: manter temporariamente o DTO legado ou criar um `SellerOfferWeb` sem os campos internos. Essa exceção não deve ser inferida durante a implementação.

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

Também existem testes unitários e de integração de oferta (`OfferServiceTest`, `AdminOfferControllerTest` e `OfferPostgresIntegrationTest`); eles devem ser estendidos, não substituídos.

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

Até a migração do frontend, a remoção de `GET /admin/offers` é uma mudança incompatível. Portanto, a implementação deve primeiro descontinuar a listagem no consumidor e só então removê-la do controller/repository. O detalhe `GET /admin/offers/{id}` pode permanecer para moderação, desde que não seja usado como caminho de seller.

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

## Arquivos e módulos afetados

- adicionar `src/main/java/com/schambeck/marketplace/usecase/offer/SellerOfferController.java`;
- alterar `OfferService.java` com `searchOwned` e `findOwnedById`;
- alterar `OfferRepository.java` com consultas paginadas que filtrem obrigatoriamente `offer.product.user.username`;
- reutilizar `AuthenticationService` para obter `preferred_username` a partir de `Principal`;
- revisar `AdminOfferController.java` e remover somente a listagem ampla após a migração do consumidor;
- ajustar `OfferWeb`/DTO de leitura conforme o contrato final dos PLAN-020/021;
- atualizar `schema/api-schema-marketplace.json` e a documentação OpenAPI/consumidor;
- estender os testes existentes de serviço, controller e PostgreSQL.

Não alterar entidades, migrations ou regras de transição neste plano. Se o contrato dos PLAN-020/021 ainda exigir migration pendente, essa migration pertence ao plano correspondente e deve ser concluída antes deste.

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

### Evidência por critério

| Critério | Evidência mínima |
|---|---|
| Isolamento na listagem | teste de repository/integrado com dois usuários, ofertas em todos os estados e filtro por username |
| Isolamento no detalhe | teste de serviço/controller em que ID de terceiro e ID inexistente produzem `404` |
| Filtro e busca | teste com status ausente, cada status válido, status inválido (`400`) e texto em campos permitidos |
| Paginação determinística | teste verifica `totalElements`, `page`, `size` e ordem `id asc` |
| Autorização | teste HTTP/security: anônimo `401`, autenticado sem ADMIN consegue seller, rota admin sem role `403` |
| Contrato | teste serializado confirma ausência de `availabilitySource`, custos, settlement e campos legados conforme PLAN-021 |
| Não mutação | teste verifica ausência de chamadas de save/histórico em consultas |
| Regressão pública | testes existentes de catálogo confirmam somente `PUBLISHED` e disponível |

Validação de build obrigatória: `./mvnw clean test -Dmaven.plugin.validation=BRIEF`. Se o PostgreSQL/Testcontainers não estiver disponível, registrar a limitação e não considerar o isolamento de tenant validado apenas por teste unitário.

## Rollback

O rollback pode restaurar temporariamente a listagem `GET /admin/offers` enquanto a UI seller é migrada. A restauração não deve remover o filtro de proprietário das novas rotas seller nem permitir que um seller use a rota administrativa.

Não há alteração de banco. As mudanças são de controller, service, repository, contrato, autorização e testes.

## Riscos, decisões pendentes e mitigação

- **Dependência dos PLAN-020/021:** sem contrato final, a resposta pode vazar campos legados. Mitigação: bloquear a implementação ou usar DTO seller explícito aprovado.
- **Quebra do frontend:** remover `/admin/offers` antes da migração quebra o painel atual. Mitigação: migração em duas etapas e confirmação de consumidores.
- **Enumeração de IDs:** uma consulta sem filtro no detalhe expõe a existência de ofertas. Mitigação: repository/service recebem username e retornam `404` para não pertencentes.
- **Filtro apenas no controller:** não é suficiente para garantir isolamento. Mitigação: username obrigatório na query do repository e teste negativo integrado.
- **`join fetch` com paginação:** pode produzir contagem ou paginação incorreta. Mitigação: `countQuery` sem `fetch` e teste de múltiplas páginas.
