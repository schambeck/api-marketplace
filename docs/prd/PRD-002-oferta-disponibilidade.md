# PRD-002 — Oferta e disponibilidade

**Rastreabilidade:** BP-003/BP-011 · Catálogo · Essencial
**Origem:** matriz de completude; `Product` hoje mistura catálogo e disponibilidade.

## Problema e objetivo

O backend armazena produto e `inventoryStatus`, mas não define o que está vendável agora, nem impede a compra de uma oferta indisponível. Deve existir uma oferta publicável com preço, contexto, disponibilidade e método de entrega vigentes.

## Escopo

- separar produto catalogado de oferta vendável;
- publicar, pausar, reativar e retirar oferta;
- exibir disponibilidade consistente no catálogo e checkout;
- registrar motivo e origem das alterações.

## Requisitos e regras

1. Oferta só pode ser publicada com contexto válido, preço, método de entrega e fonte de disponibilidade.
2. Oferta pausada, retirada ou sem disponibilidade não pode iniciar checkout.
3. Mudança de preço/disponibilidade deve aplicar-se a novas compras sem alterar pedidos já criados.
4. O status não deve ser um campo livre sem transições definidas.

## Critérios de aceite

- Uma oferta publicada aparece no catálogo com preço, contexto e disponibilidade.
- Oferta indisponível não é adicionada ao checkout e informa a causa.
- Pausar/retirar uma oferta a remove das novas compras sem apagar histórico.
- Alterações concorrentes não deixam o catálogo e o checkout com decisões incompatíveis.

## Decisões pendentes

Se `Product` será mantido como entidade de catálogo; política de preço; múltiplas ofertas por produto; e quem pode alterar disponibilidade.
