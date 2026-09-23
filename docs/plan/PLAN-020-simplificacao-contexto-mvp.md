# PLAN-020 — Simplificação do contexto de game para o MVP

**Tipo:** Plano corretivo sobre o `PLAN-001` já implementado.

**Prioridade:** MVP.

**Objetivo:** reduzir o `GameContext` ao mínimo necessário para identificar o item de game anunciado, sem exigir versionamento de domínio, histórico de snapshots ou referência imutável para publicação.

## Motivação

O `PLAN-001` foi implementado com `GameContextHistory`, `GameContextReference`, versionamento, histórico administrativo e regras de retenção. Para o MVP, o seller precisa apenas informar o contexto comercial do item. Essa infraestrutura não é necessária para o fluxo:

```text
seller cadastra oferta → comprador compra → seller entrega → comprador confirma
```

O `GameContext` implementado deve ser tratado como baseline legado; este plano define a simplificação que o código deverá adotar.

## Estado implementado

Atualmente existem:

- entidade, repository, service, mapper e controller de `GameContext`;
- tabela `game_context` criada pela `V11__create_game_context.sql`;
- tabela `game_context_history` e gravação automática de snapshots;
- `GameContextReference` com `id`, `version` e cópia dos atributos;
- validação de status `ACTIVE`, `INACTIVE` e `DISCONTINUED`;
- dependência de `gameContextId` na criação e publicação de `Offer`.

## Estado alvo do MVP

O contexto necessário para a venda deve ficar diretamente na oferta:

- `game` e `platform` obrigatórios;
- `region` e `variant` opcionais;
- `publisher` e `server` fora do contrato do MVP, podendo permanecer em colunas legadas não expostas;
- nenhum `GameContextReference` público;
- nenhuma exigência de `contextVersion` para publicar uma oferta;
- nenhuma consulta de histórico de contexto no fluxo de venda;
- o seller pode criar uma oferta informando seus próprios dados de contexto, sujeitos a validação básica.

A informação histórica que precisar ser preservada pertence ao futuro `OrderItem`, não ao agregado de contexto do catálogo.

## Alterações previstas

1. Remover `gameContextId`, `contextVersion` e `context` dos DTOs e mappers de `Offer`.
2. Substituir o uso de `GameContextService.referenceForOfferPublication` por validação simples dos campos de contexto da oferta.
3. Mover o contrato público de `game`, `platform`, `region` e `variant` para `Offer`.
4. Remover `GameContextHistory`, `GameContextHistoryRepository` e a rota administrativa de histórico do caminho do MVP.
5. Deixar `GameContext` e suas tabelas sem novos consumidores, como legado de migração; não apagar tabelas já aplicadas em Flyway nesta etapa.
6. Retirar `GameContext` das dependências de publicação, checkout e carrinho.
7. Atualizar `schema/api-schema-marketplace.json` e a documentação OpenAPI para o contrato simplificado.

## Persistência e migração

Criar uma nova migration, sem editar `V11`, `V12` ou `V13` já aplicadas:

- adicionar ou consolidar em `offer` as colunas de contexto do MVP;
- preencher os valores a partir de `context_game`, `context_platform`, `context_region` e `context_variant` existentes;
- manter `game_context_id`, `context_version` e tabelas históricas durante a transição, sem usá-los no novo código;
- só remover colunas/tabelas legadas em uma migration posterior, após confirmar que nenhum consumidor externo depende delas.

## Testes e aceite

- criar oferta sem `gameContextId`, informando `game` e `platform`;
- rejeitar oferta sem `game` ou `platform`;
- aceitar `region` e `variant` opcionais;
- publicar oferta sem chamar `GameContextService`;
- confirmar que atualização de contexto não bloqueia checkout por divergência de versão;
- confirmar que endpoints legados de contexto não são necessários para catálogo, carrinho ou checkout;
- validar migração dos dados existentes de `Offer` sem perda de game/plataforma.

## Limites e riscos

Este plano não implementa pedido, retenção financeira, entrega ou repasse. Esses comportamentos pertencem aos planos de pedido, pagamento, fulfillment e pós-venda. O principal risco é haver consumidores externos do endpoint `game-contexts`; por isso a retirada física das tabelas fica separada da remoção do uso no domínio.

## Rollback

A reversão deve reativar temporariamente a leitura das colunas legadas de contexto e restaurar a dependência de `game_context_id` somente se a migração tiver sido interrompida. Nenhuma migration aplicada deve ser editada ou removida.
