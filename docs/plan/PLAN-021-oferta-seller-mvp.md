# PLAN-021 — Oferta pertencente ao seller no MVP

**Tipo:** plano corretivo sobre o `PLAN-002` já implementado.

**Prioridade:** MVP.

**Status:** planejado; nenhuma implementação deste plano foi iniciada.

## 1. Objetivo e escopo

Adaptar `Offer` para o modelo em que o seller anuncia e entrega o próprio item. No MVP, a propriedade é derivada exclusivamente de:

```text
Offer → Product → Product.user
```

Este plano cobre catálogo, gerenciamento da oferta, autorização, migração do modelo atual e preservação da identidade da oferta no carrinho. Não cria `Seller`, `SupplySource`, reserva atômica, settlement, pedido ou fluxo de entrega.

### Fora de escopo

- entidade ou coluna `seller_id` duplicada em `offer`;
- aprovação de supplier, alocação de estoque externo ou `availabilitySource`;
- débito/reserva atômica de `Product.quantity` — responsabilidade do `PLAN-004`;
- repasse financeiro, pedido, fulfillment e confirmação de entrega;
- remoção física de `GameContext`, `game_context_history` ou das colunas legadas nesta primeira migração;
- edição de migrations `V11`, `V12` ou `V13` já aplicadas.

## 2. Estado atual verificado

O código atual possui:

- `Offer` com `product`, `gameContext`, `contextVersion`, snapshot de contexto, `availabilitySource`, `availabilityStatus`, preço, entrega e status;
- criação, edição, publicação, pausa, reativação, retirada e disponibilidade em `OfferService`;
- todas as mutações de `OfferController` protegidas por `ROLE_ADMIN`;
- consultas de seller apenas em `SellerOfferController` (`GET seller/offers` e `GET seller/offers/{id}`);
- chamada de `GameContextService.referenceForOfferPublication` na publicação, reativação e validação de checkout;
- `Product.quantity` como estoque existente do produto;
- `CartItem.offer`, `CartItem.price` e `CartItem.quantity` já persistidos, com compatibilidade de entrada por `productId`;
- `V12__create_offer.sql` criando as colunas legadas e `V13__backfill_legacy_product_offers.sql` criando ofertas com contexto `LEGACY` e `availability_source` de migração;
- `schema/api-schema-marketplace.json` ainda exigindo `gameContextId`, `contextVersion` e `availabilitySource`.

## 3. Estado alvo e decisões de desenho

### 3.1 Modelo de oferta

`Offer` deverá manter apenas, no domínio ativo:

- `product` obrigatório;
- `contextGame` e `contextPlatform` obrigatórios;
- `contextRegion` e `contextVariant` opcionais;
- `price` positivo;
- `deliveryMethod` obrigatório;
- `availabilityStatus` derivado de `Product.quantity` e usado apenas como estado operacional/histórico compatível durante a transição;
- `status`: `DRAFT`, `PUBLISHED`, `PAUSED`, `RETIRED`;
- auditoria e versionamento já existentes.

`gameContext`, `contextVersion`, `contextPublisher`, `contextServer` e `availabilitySource` deixam de ser usados pelo novo fluxo. Os campos físicos legados podem permanecer nullable durante a transição para permitir leitura e rollback; não devem aparecer nos DTOs nem em novas regras de negócio.

### 3.2 Fonte de disponibilidade

`Product.quantity` é a fonte de verdade do estoque. O novo código deve:

- considerar a oferta disponível somente quando `Product.quantity > 0`;
- rejeitar publicação e reativação quando o produto estiver sem estoque;
- revalidar produto, status e quantidade no checkout;
- filtrar o catálogo público por `status = PUBLISHED` e `product.quantity > 0`;
- atualizar `availabilityStatus` para `AVAILABLE`/`UNAVAILABLE` quando o fluxo já estiver gravando a oferta, sem permitir que esse campo isolado torne uma oferta vendável;
- retirar o endpoint de alteração manual de disponibilidade do fluxo seller; se mantido para compatibilidade administrativa, ele não poderá marcar como disponível produto com quantidade zero.

Essa decisão evita uma segunda fonte de verdade enquanto a reserva atômica do `PLAN-004` não existe.

### 3.3 Autorização

O seller autenticado poderá criar, editar, publicar, pausar, reativar e retirar ofertas vinculadas a produtos cujo `Product.user.username` seja o seu username. `ADMIN` poderá executar as mesmas operações para moderação, inclusive em produto de outro usuário, conforme a política administrativa existente.

A autorização deve ser aplicada no serviço, e não somente em anotações do controller. Isso protege chamadas internas e evita que uma rota futura contorne a regra. O controller deve passar o actor autenticado para o serviço; o serviço deve carregar o produto com seu proprietário e rejeitar divergências com erro de autorização/validação já tratado pela aplicação.

## 4. Contrato HTTP alvo

### Criação e edição

O payload de criação/edição deve conter:

```json
{
  "productId": 123,
  "game": "Nome do jogo",
  "platform": "PC",
  "region": "BR",
  "variant": "Standard",
  "price": 49.90,
  "deliveryMethod": "digital",
  "reason": "ajuste da oferta",
  "source": "seller"
}
```

`game`, `platform`, `price` e `deliveryMethod` são obrigatórios; `region` e `variant` são opcionais. `gameContextId`, `contextVersion`, `context` e `availabilitySource` não são obrigatórios nem usados. Durante a migração, consumidores antigos podem enviar esses campos como deprecated, mas eles devem ser ignorados e não devem ser necessários para uma requisição válida.

### Rotas e permissões

- `POST /offers`: seller autenticado para produto próprio; `ADMIN` também permitido;
- `PUT /offers/{id}`: seller proprietário apenas para oferta `DRAFT`; `ADMIN` pode editar conforme política;
- `POST /offers/{id}/publish`, `/pause`, `/reactivate`, `/retire`: seller proprietário ou `ADMIN`;
- `GET /seller/offers` e `GET /seller/offers/{id}`: seller autenticado, filtrando sempre por proprietário;
- `GET /catalog/offers`, `GET /catalog/offers/{id}` e `GET /offers/**`: públicos somente para ofertas publicadas e com estoque;
- `GET /offers/{id}/history`: seller proprietário ou `ADMIN`;
- `POST /offers/{id}/availability`: remover do contrato seller; manter apenas se necessário para compatibilidade administrativa, com validação contra `Product.quantity`.

As respostas públicas e de seller devem expor `productId`, contexto mínimo, preço, entrega, disponibilidade e status. Não devem expor `gameContextId`, versão de contexto, `availabilitySource` ou dados internos de supply.

## 5. Arquivos e módulos afetados

### Domínio e DTOs

- `src/main/java/com/schambeck/marketplace/usecase/offer/Offer.java`: remover associação ativa com `GameContext`, `contextVersion` e `availabilitySource`; manter colunas legadas somente se a estratégia de migração exigir;
- `OfferWeb.java`, `OfferUpdateRequest.java`, `SellerOfferWeb.java`: adicionar `game`, `platform`, `region`, `variant`; retirar campos legados do contrato;
- `OfferMapper.java`: mapear o contexto diretamente da oferta e não instanciar `GameContextReference`;
- `OfferAvailabilityRequest.java`: remover ou restringir à compatibilidade administrativa conforme a decisão de implementação;
- `OfferStatusHistory.java`: preservar apenas transições/status e alterações de disponibilidade, sem source de supply; `source` pode continuar como origem da ação (`seller`, `admin`, `api`), não como fonte de estoque.

### Serviço e autorização

- `OfferService.java`: retirar `GameContextService`, validar contexto mínimo, carregar o produto proprietário, autorizar actor, validar estoque na publicação/reativação/checkout e registrar histórico;
- `OfferController.java`: permitir autenticação nas mutações seller e encaminhar username; preservar `GET` público;
- `SellerOfferController.java`: manter consultas próprias e, se necessário, adicionar as ações seller ou delegá-las às rotas de oferta;
- `AdminOfferController.java`: preservar moderação administrativa;
- `ProductRepository.java`/`OfferRepository.java`: adicionar consultas com proprietário, produto e quantidade; garantir filtros públicos `PUBLISHED` + `quantity > 0`;
- `exception`/tratamento HTTP: distinguir produto/oferta inexistente de tentativa de operar recurso alheio sem vazar dados.

### Carrinho e checkout

- `CreateShoppingCartItemService.java`: resolver sempre uma oferta, validar status e `Product.quantity`, e manter `offerId`, preço capturado e produto/seller derivados;
- `OfferService.validateForCheckout`: remover validação de `GameContext` e validar estoque do produto;
- `CartItem`/`CartItemMapper`/`CartPreferenceConverter`: confirmar que `offerId` e preço persistido continuam sendo usados, sem voltar ao preço atual do produto;
- fluxo futuro de pedido: documentar que deverá capturar seller, oferta, preço e quantidade a partir do `CartItem`, sem implementar neste plano.

### Persistência e contratos

- criar `V14__simplify_seller_offer_mvp.sql` — usar o próximo número livre após `V13`;
- atualizar `schema/api-schema-marketplace.json` para oferta e seller-offer simplificados;
- revisar OpenAPI gerado pelos controllers, sem criar uma especificação paralela;
- não editar `V11`, `V12` ou `V13`.

## 6. Migração de dados

A migration nova deve ser executada em etapas compatíveis com PostgreSQL:

1. adicionar/garantir `context_game`, `context_platform`, `context_region` e `context_variant` em `offer`, se necessário para a versão do banco;
2. preencher os campos diretos a partir do snapshot existente e, quando ausente, do `game_context` referenciado;
3. normalizar ofertas legadas `LEGACY`, mantendo-as identificáveis para revisão;
4. recalcular `availability_status` a partir de `product.quantity`;
5. marcar como `PAUSED` ofertas legadas sem `game`/`platform`, sem produto, sem preço positivo ou sem estoque disponível; registrar a transição em `offer_status_history` com `source = 'PLAN-021_MIGRATION'`;
6. tornar `availability_source` e a FK `game_context_id` não participantes do novo código, sem removê-los fisicamente nesta etapa;
7. preservar `offer_status_history` e não duplicar histórico equivalente;
8. criar índices necessários para catálogo (`status`, produto/quantidade) apenas se a análise da consulta indicar necessidade.

A migration deve ser testada em banco limpo e em cópia com dados de `V11`–`V13`. Ofertas inválidas não devem permanecer publicadas por efeito do backfill.

## 7. Sequência de implementação

1. Confirmar contrato final com o frontend: campos novos, rotas seller e política para campos legados deprecated.
2. Implementar a migration `V14` e validar o backfill com oferta válida, oferta `LEGACY` com estoque, oferta sem estoque e oferta sem contexto.
3. Alterar entidade, DTOs e mapper para o contexto direto da oferta.
4. Refatorar repository e `OfferService`, incluindo autorização por `Product.user`, validações e filtros de estoque.
5. Alterar controllers e permissões, preservando acesso público somente às consultas de catálogo.
6. Atualizar carrinho/checkout para revalidar a oferta e o estoque sem `GameContextService`.
7. Atualizar schema JSON e documentação OpenAPI/endpoint, removendo campos obsoletos do contrato.
8. Remover imports e referências de `GameContextService.referenceForOfferPublication` no fluxo de oferta/carrinho; manter `GameContext` isolado como legado.
9. Executar testes automatizados, migração em PostgreSQL e validação manual dos endpoints com JWT seller/admin e usuário sem permissão.
10. Só em plano posterior, após confirmar os consumidores, remover fisicamente colunas legadas e tabelas de contexto não utilizadas.

## 8. Testes e evidências de aceite

Embora o repositório não possua testes versionados atualmente, a implementação deve adicionar testes de serviço/repository/controller ou, no mínimo, testes de integração focados no fluxo alterado.

| Critério | Evidência obrigatória |
|---|---|
| Seller cria oferta própria | teste/chamada autenticada retorna sucesso e persiste `Product.user` como proprietário indireto |
| Seller não opera oferta alheia | tentativa de criar/editar/publicar retorna não autorizado ou erro de domínio sem alterar dados |
| ADMIN modera oferta | teste autenticado com `ROLE_ADMIN` altera status de produto de outro seller |
| Contexto mínimo | payload sem `gameContextId`/`availabilitySource`, com `game`/`platform`, cria e edita com sucesso |
| Campos obrigatórios | ausência de `game`, `platform`, preço ou entrega, ou preço não positivo, falha antes da persistência |
| Estoque | produto com `quantity <= 0` não publica, não reativa, não entra no catálogo e não inicia checkout |
| Histórico | publicação, pausa, reativação, retirada e mudanças relevantes geram histórico sem source de supply |
| Catálogo | `DRAFT`, `PAUSED`, `RETIRED` e ofertas sem estoque não aparecem em lista nem detalhe público |
| Carrinho | `CartItem` preserva `offerId`, preço no momento da inclusão, produto e seller indireto corretos |
| Migração | V14 preserva oferta válida; pausa/revisa as inválidas; não perde contexto mínimo nem duplica histórico |
| Desacoplamento | verificação não encontra chamada a `referenceForOfferPublication` no fluxo de oferta/carrinho |

Comandos mínimos de validação:

```bash
./mvnw clean test -Dmaven.plugin.validation=BRIEF
./mvnw clean package -Dmaven.plugin.validation=BRIEF
```

Também executar a migration contra PostgreSQL limpo e contra cópia com `V11`–`V13`, verificar o schema resultante e testar manualmente rotas com tokens de seller, admin e usuário sem vínculo com o produto. Não usar credenciais reais.

## 9. Riscos, compatibilidade e rollback

### Riscos e mitigações

- frontend pode continuar enviando campos de `GameContext`: aceitar entrada deprecated durante a janela de transição, sem retorná-los;
- `availability_status` pode divergir de `Product.quantity`: centralizar a decisão em `Product.quantity` e cobrir catálogo, publicação e checkout;
- mudança de `ADMIN` para seller pode expor rota: exigir checagem no serviço para toda mutação;
- dados `LEGACY` podem não ter contexto suficiente: pausar ofertas ambíguas na migration e gerar relatório de ids;
- sem reserva atômica, duas inclusões ainda podem disputar estoque: não prometer consistência de reserva antes do `PLAN-004`.

O rollback de aplicação deve voltar a ler os campos físicos legados sem editar ou reverter migrations. Ofertas pausadas pela V14 não devem ser reativadas automaticamente sem reavaliação de produto, preço e estoque. A remoção física das colunas só poderá ocorrer em migration futura após confirmar que nenhum consumidor depende delas.

## 10. Decisões pendentes antes da implementação

- confirmar se as ações seller permanecerão em `/offers/{id}/...` ou serão duplicadas sob `/seller/offers/{id}/...`; a recomendação é manter as rotas atuais e aplicar autorização por proprietário no serviço;
- confirmar se `source` no histórico será visível para seller; ele deve representar origem da ação, nunca supply;
- confirmar a janela de compatibilidade do frontend para campos legados deprecated;
- definir o formato do relatório operacional de ofertas pausadas pela V14.
