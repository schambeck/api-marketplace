# PLAN-003 — Supply e aquisição

**Issue:** [#5](https://github.com/schambeck/api-marketplace/issues/5)

**PRD:** [`PRD-003`](../prd/PRD-003-supply-aquisicao.md) · **Dependências:** PRD-001 e decisão B2C/marketplace/híbrido.

## Estado atual e alvo

Não há entidade de fonte de supply; `User` não deve representar implicitamente seller ou supplier. O alvo é um agregado de fonte aprovado, com capacidade, custo, condições, validade, restrições e histórico operacional.

## Abordagem técnica

- Definir `SupplySource` e, se necessário, separar `Supplier`/`Seller` de `User`.
- Implementar onboarding/status (`PENDING`, `APPROVED`, `SUSPENDED`, `CLOSED`), vínculo de contexto/oferta e registro de divergência.
- Exigir aprovação e compatibilidade em publicação e alocação; suspensão bloqueia somente novas alocações.
- Criar `SupplySourceController` para operação administrativa e APIs de consulta limitadas; dados de custo não devem vazar para catálogo.
- Preparar campos de SLA, comissão e settlement apenas se o modelo marketplace/híbrido for escolhido.

## Persistência e validação

Migration com tabelas de fonte, capacidade/condições versionadas, vínculo oferta-fonte e `supply_exception`. Índices por status/validade e restrições de unicidade devem impedir duas fontes primárias incompatíveis.

## Sequência e testes

Resolver atores legais; implementar modelo e aprovação; integrar publicação; integrar alocação/fulfillment; adicionar auditoria. Testar fonte não aprovada, suspensão, histórico, divergência e concorrência de aprovação/alocação.

## Aceite, riscos e decisões

Os quatro critérios do PRD serão demonstrados por testes de serviço/API e rastreio de exceção até uma decisão. O risco principal é construir payout/settlement sem decisão de negócio; manter essa parte atrás de uma interface até PRD-013.
