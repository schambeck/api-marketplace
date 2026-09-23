# PLAN-004 — Reserva, alocação e consumo de inventory

**Prioridade:** MVP reduzido. Controlar o saldo do estoque pertencente diretamente ao seller.

**Issue:** [#6](https://github.com/schambeck/api-marketplace/issues/6)

**PRD:** [`PRD-004`](../prd/PRD-004-inventory.md) · **Dependências:** PRD-001 e PRD-002. O `PLAN-003` só passa a ser dependência quando houver múltiplas fontes.

## Estado atual e alvo

`Product.quantity`/`inventoryStatus` não oferecem atomicidade nem vínculo a pedido. O alvo é um ledger de inventory com estados `AVAILABLE`, `RESERVED`, `ALLOCATED`, `CONSUMED`, `RELEASED` e `EXPIRED`, vinculado a linha de pedido e fonte.

## Abordagem técnica

- Definir granularidade do item (saldo, unidade ou código) e começar com `InventoryBalance` ou quantidade na oferta, conforme o tipo de item.
- Criar serviço transacional mínimo para reservar, consumir e liberar estoque; alocação por fonte fica para o `PLAN-003`.
- Usar locking pessimista ou atualização condicional por versão; selecionar estratégia após teste de concorrência PostgreSQL.
- Implementar expiração da reserva apenas quando o checkout exigir TTL; não depender de alteração manual em `Product`.
- Exigir reserva válida antes da confirmação da venda e consumir somente a quantidade efetivamente entregue.

## Persistência e APIs

Adicionar migrations para saldo/unidades, reservas, alocações, movimentos e chaves idempotentes. APIs administrativas devem ocultar códigos sensíveis; endpoints de operação devem mostrar somente o necessário.

## Sequência e validação

1. Fixar granularidade, oversell e TTL.
2. Implementar reserva com locking e idempotência.
3. Integrar pedido/pagamento/fulfillment.
4. Implementar expiração, release e consumo parcial.

Testes obrigatórios no MVP: duas reservas concorrentes, repetição da mesma operação, saldo insuficiente, cancelamento e liberação. Expiração, entrega parcial e alocação por fonte entram posteriormente.

## Aceite e riscos

Os critérios serão evidenciados por testes transacionais de integração. O risco maior é overselling por locking inadequado; a migration deve incluir constraints e o serviço deve rejeitar saldo insuficiente de forma determinística.
