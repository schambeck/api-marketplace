# Plano: busca textual de ofertas publicadas

## Objetivo

Permitir busca textual paginada no catálogo público (`GET /catalog/offers?search=...`), mantendo apenas ofertas `PUBLISHED` e preservando as APIs e regras de criação/publicação existentes.

## Estado atual e alvo

- Atualmente os controllers aceitam paginação, mas ignoram `search`.
- O serviço consulta apenas por status, sem filtro textual.
- O repositório já ordena ofertas por `offer.id asc` e faz `join fetch` do produto.
- O alvo é normalizar busca ausente/em branco para ausência de filtro e filtrar produto, snapshot de contexto e status com uma consulta paginada que tenha `countQuery` explícito.

## Escopo técnico

1. Atualizar `CatalogController` e `OfferController` para receber e encaminhar `search`.
2. Adicionar sobrecarga compatível em `OfferService`, normalizando `null`, vazio e espaços.
3. Adicionar consulta JPQL paginada em `OfferRepository` para nome/descrição/categoria do produto e game/publisher/platform/region/server/variant do snapshot.
4. Cobrir a consulta com teste PostgreSQL controlado e o encaminhamento do catálogo com teste unitário.

## Sequência

1. Alterar repository e serviço.
2. Alterar controllers.
3. Adicionar testes dos critérios de busca, status, paginação e controller.
4. Executar testes Maven, diff check e revisar status/diff.

## Critérios e evidências

- Ausência, vazio e espaços: teste de serviço/integração compara total e conteúdo com busca sem filtro.
- Campos textuais, case-insensitive e nenhum resultado: teste PostgreSQL com dados publicados distintos.
- Status: fixture inclui oferta não publicada e verifica que ela não aparece.
- Paginação: teste verifica `totalElements`, página e tamanho.
- Controller: teste unitário verifica que `search` chega ao serviço.
- Regressão: `./mvnw test` e `git diff --check`.

## Riscos e mitigação

- `Page` com `join fetch` pode contar incorretamente; a consulta nova terá `countQuery` sem `fetch`.
- Campos opcionais do snapshot podem ser nulos; a query usará `coalesce`.
- O filtro será aplicado somente em ofertas publicadas; nenhuma regra adicional de disponibilidade será introduzida.

## Rollback

Reverter os arquivos de controller, serviço, repository e testes adicionados; não há migration nem alteração de contrato persistente.
