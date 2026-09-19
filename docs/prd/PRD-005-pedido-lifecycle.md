# PRD-005 — Pedido e lifecycle

**Rastreabilidade:** BP-014 · Transaction · Essencial.

## Problema e objetivo

Hoje a preference usa `Cart.id` como referência externa e não existe agregado comercial para congelar a compra. O produto deve criar um pedido independente do carrinho, coordenando pagamento, risco, inventory, fulfillment e pós-venda.

## Requisitos

1. O pedido deve congelar linhas, preço, contexto, quantidade, cliente e instruções aplicáveis no momento da criação.
2. O carrinho pode originar o pedido, mas não é o registro definitivo da venda.
3. O lifecycle deve suportar `CREATED`, `PAYMENT_PENDING`, `PAID`, `REJECTED`, `FULFILLING`, `DELIVERED`, `COMPLETED`, `CANCELLED`, `EXPIRED`, `REFUNDED`, `DISPUTED` e `PARTIALLY_DELIVERED`, conforme aplicável.
4. Transições inválidas devem ser rejeitadas e registradas.

## Critérios de aceite

- Checkout válido cria um pedido com snapshot imutável de preço e linhas.
- Repetição da confirmação não cria pedido duplicado.
- Cada transição de estado informa causa e origem.
- O pedido mantém referências para payment, inventory, fulfillment e entrega.

## Decisões pendentes

Pedido único por checkout; impostos/taxas; numeração pública; retenção de snapshot; e estados mínimos da primeira versão.

