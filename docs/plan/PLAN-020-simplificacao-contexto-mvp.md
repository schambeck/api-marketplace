# PLAN-020 — Simplificação do contexto de game para o MVP

**Tipo:** plano corretivo sobre o `PLAN-001` já implementado.

**Prioridade:** MVP.

**Status:** planejado; o repositório contém parte da implementação técnica, mas ainda não há evidência completa de migração, contrato e testes de aceite.

**Dependências:** `PLAN-001` (baseline legado), `PLAN-002`/`PLAN-021` (oferta) e `PLAN-022` (consulta de ofertas do seller).

## 1. Objetivo

Reduzir o contexto comercial necessário para vender um item ao conjunto mínimo armazenado diretamente em `Offer`:

- `game` e `platform`: obrigatórios;
- `region` e `variant`: opcionais;
- `publisher` e `server`: fora do contrato do MVP;
- nenhuma referência pública a `GameContext`, `GameContextReference` ou `contextVersion`;
- nenhuma validação de publicação, carrinho ou checkout baseada em versão/histórico de contexto.

O fluxo do MVP deve ser:

```text
seller informa o contexto na oferta → oferta é publicada → comprador adiciona ao carrinho → checkout valida a oferta
```

A preservação histórica de dados de venda será responsabilidade futura de `OrderItem`, não do agregado de catálogo `GameContext`.

## 2. Limites de escopo

Este plano trata exclusivamente do desacoplamento do contexto versionado. Não implementa:

- ownership/autorização do seller, que pertence ao `PLAN-021`;
- reserva atômica ou baixa de estoque, que pertence ao `PLAN-004`;
- pedido, pagamento, entrega, repasse ou confirmação;
- remoção física de `game_context`, `game_context_history` ou colunas legadas;
- migração de consumidores externos do endpoint `/game-contexts`.

O `GameContext` pode continuar disponível como legado administrativo e de compatibilidade, desde que não seja requisito para criar, publicar, consultar, adicionar ao carrinho ou iniciar checkout de uma oferta.

## 3. Baseline verificado

O baseline atual contém:

- `GameContext`, `GameContextService`, `GameContextReference`, `GameContextHistory` e rota `GET /game-contexts/{id}/history`;
- migrations `V11__create_game_context.sql`, `V12__create_offer.sql`, `V13__backfill_legacy_product_offers.sql` e `V14__simplify_seller_offer_mvp.sql`;
- `Offer` já expõe no domínio ativo `contextGame`, `contextPlatform`, `contextRegion` e `contextVariant`, sem associação Java ativa com `GameContext`;
- `OfferWeb`, `SellerOfferWeb` e `OfferUpdateRequest` já usam `game`, `platform`, `region` e `variant` diretamente;
- `OfferRepository` já busca o catálogo por `status = PUBLISHED` e `Product.quantity > 0`;
- `OfferService` já publica e valida checkout sem chamar `GameContextService`;
- o schema JSON já descreve o contrato simplificado.

As evidências ainda pendentes são a execução da `V14` em banco limpo e banco migrado, testes automatizados do desacoplamento e verificação de todas as referências no fluxo de carrinho/checkout. O plano deve ser executado como validação e fechamento da migração, não como justificativa para duplicar mudanças já presentes.

## 4. Estado alvo

### Domínio e persistência ativa

`Offer` deve ter como contexto ativo apenas:

```text
product, game, platform, region?, variant?, price, deliveryMethod,
availabilityStatus, status e auditoria/versionamento da oferta
```

As colunas físicas legadas `game_context_id`, `context_version`, `context_publisher`, `context_server` e `availability_source` podem permanecer nullable para rollback e compatibilidade. Elas não podem ser lidas ou alteradas por novas regras de negócio.

### Contrato HTTP

Criação e edição devem aceitar, no mínimo:

```json
{
  "productId": 123,
  "game": "Nome do jogo",
  "platform": "PC",
  "region": "BR",
  "variant": "Standard",
  "price": 49.90,
  "deliveryMethod": "digital"
}
```

`game`, `platform`, `price` e `deliveryMethod` são obrigatórios. `region` e `variant` são opcionais. `gameContextId`, `contextVersion`, `context` e `availabilitySource` não são necessários, não aparecem nas respostas e não influenciam a publicação.

### Fluxos consumidores

- publicação/reativação validam somente os campos da oferta e o estoque operacional definido pelo plano de oferta;
- carrinho resolve a oferta e não consulta `GameContext`;
- checkout revalida oferta, status e estoque sem comparar `contextVersion`;
- catálogo e consultas seller retornam somente o contexto mínimo;
- alterações posteriores em `GameContext` não alteram nem bloqueiam uma oferta existente.

## 5. Arquivos e áreas afetadas

Verificar e, quando ainda necessário, ajustar:

- `src/main/java/com/schambeck/marketplace/usecase/offer/Offer.java`;
- `OfferWeb.java`, `SellerOfferWeb.java`, `OfferUpdateRequest.java` e `OfferMapper.java`;
- `OfferService.java`, `OfferRepository.java` e controllers de oferta/catálogo;
- `src/main/java/com/schambeck/marketplace/usecase/cartitem/` e `shoppingcart/`;
- `src/main/java/com/schambeck/marketplace/usecase/mercadopago/preference/`;
- `src/main/java/com/schambeck/marketplace/usecase/gamecontext/`: manter isolado como legado e retirar apenas consumidores do fluxo MVP;
- `src/main/resources/db/migration/V14__simplify_seller_offer_mvp.sql`: revisar/validar, sem editar migrations aplicadas;
- `schema/api-schema-marketplace.json` e documentação OpenAPI derivada dos controllers;
- planos `PLAN-021` e `PLAN-022`, apenas para manter dependências e contratos coerentes.

Não remover classes ou tabelas de contexto nesta etapa sem inventário explícito de consumidores externos.

## 6. Migração de dados

Usar uma migration nova após `V13`; no estado atual, a `V14` já existe e deve ser tratada como a migration desta mudança.

Validar que ela:

1. torna `game_context_id`, `context_version` e `availability_source` não obrigatórios para o novo fluxo sem apagar as colunas;
2. preenche `context_game`, `context_platform`, `context_region` e `context_variant` usando o snapshot da oferta e, quando necessário, o `game_context` referenciado;
3. recalcula `availability_status` conforme a regra vigente de estoque;
4. não deixa ofertas sem `game`/`platform` publicadas;
5. registra pausas corretivas em `offer_status_history` sem duplicar histórico;
6. preserva `offer.id`, preço, produto e os campos mínimos migrados;
7. funciona tanto em banco limpo quanto em banco já migrado por `V11`–`V13`.

A remoção física das colunas e tabelas deve ser uma migration futura, condicionada à confirmação de que nenhum consumidor externo depende delas.

## 7. Sequência de execução

1. Inventariar referências a `GameContextService`, `GameContextReference`, `gameContextId`, `contextVersion` e colunas legadas no código ativo de oferta, carrinho e checkout.
2. Confirmar que o contrato final do MVP é o contexto direto da oferta e que `PLAN-021` permanece responsável por ownership/autorização.
3. Revisar a `V14` contra os cenários de dados válidos, `LEGACY`, sem estoque e sem contexto.
4. Executar a migration em PostgreSQL limpo e em cópia representativa do banco legado; registrar contagens antes/depois e ids pausados.
5. Completar eventuais ajustes residuais em serviço, repository, mapper, controllers e consumidores de checkout.
6. Atualizar schema JSON, OpenAPI e planos dependentes para remover campos obsoletos do contrato.
7. Adicionar testes de unidade/integração para o desacoplamento e os cenários de migração.
8. Fazer revisão final por `git diff`, `git status` e busca de referências proibidas no fluxo ativo.

## 8. Testes e evidência de aceite

| Critério | Evidência exigida |
|---|---|
| Criar oferta sem contexto versionado | teste/chamada autenticada cria oferta com `game` e `platform`, sem `gameContextId` |
| Campos mínimos | ausência ou branco em `game`/`platform` retorna erro de domínio antes da persistência |
| Opcionais | `region` e `variant` ausentes são aceitos e persistidos como nulos |
| Publicação desacoplada | teste verifica publicação sem chamada a `GameContextService` |
| Checkout desacoplado | atualização de `GameContext` não bloqueia checkout por divergência de versão |
| Carrinho/catalogo | inclusão e consulta não exigem endpoint nem registro novo de `GameContext` |
| Contrato | serialização de `OfferWeb`/`SellerOfferWeb` não contém campos legados |
| Migração | `V14` preserva os campos mínimos, não perde ofertas válidas e pausa dados inválidos |
| Legado isolado | busca de referências confirma que `game_context` só é usado no módulo legado/migration |

Comandos mínimos:

```bash
./mvnw clean test -Dmaven.plugin.validation=BRIEF
./mvnw clean package -Dmaven.plugin.validation=BRIEF
```

Também executar a migration contra PostgreSQL limpo e contra uma cópia com `V11`–`V13`. A validação manual deve cobrir seller autenticado, admin, usuário sem vínculo, catálogo, carrinho e início de checkout, sem credenciais reais.

## 9. Riscos, rollback e compatibilidade

- **Consumidores externos de `/game-contexts`:** manter endpoint e tabelas até confirmar dependências; remover apenas o consumo interno da venda.
- **Dados `LEGACY`:** não reativar automaticamente ofertas pausadas; gerar relatório de ids para correção explícita.
- **Campos antigos enviados pelo frontend:** durante a janela de transição, podem ser ignorados/deprecated, mas não podem ser necessários para uma requisição válida.
- **Rollback:** reverter o código para leitura das colunas legadas e restaurar a dependência de contexto somente se a migração for interrompida; nunca editar ou remover migration aplicada.
- **Integridade histórica:** o snapshot comercial definitivo deverá ser capturado em `OrderItem` quando o fluxo de pedido for implementado.

## 10. Critério de conclusão

O PLAN-020 será considerado concluído quando:

1. nenhuma operação de oferta, carrinho ou checkout exigir `GameContext`;
2. o contrato e o schema público expuserem somente o contexto mínimo;
3. a migração tiver evidência em banco limpo e legado;
4. os testes de aceite passarem;
5. os planos `PLAN-021` e `PLAN-022` não contradizerem o contrato simplificado;
6. permanecer documentado que a remoção física do legado é uma etapa futura.
