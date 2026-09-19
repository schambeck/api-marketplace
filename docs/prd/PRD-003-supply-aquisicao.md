# PRD-003 — Supply e aquisição

**Rastreabilidade:** BP-012 · Supply · Essencial · modelo operacional a validar.

## Problema e objetivo

Não há registro da origem do item, capacidade, custo ou responsabilidade de entrega. A plataforma precisa representar como o supply é adquirido e associado a uma oferta, sem assumir indevidamente se o negócio é B2C, marketplace ou híbrido.

## Escopo

- cadastrar source, supplier ou seller conforme o modelo escolhido;
- aprovar, suspender e encerrar uma fonte;
- registrar capacidade, custo, restrições e disponibilidade;
- associar supply aprovado a uma oferta.

## Requisitos

1. Nenhum supply pode ser usado sem status aprovado e contexto compatível.
2. O sistema deve preservar origem, custo/condições e período de validade.
3. Divergências de quantidade, qualidade ou custo devem abrir exceção operacional.
4. Se houver seller, onboarding, SLA, comissão e settlement devem ser explícitos; não podem ser inferidos de `User`.

## Critérios de aceite

- Fonte não aprovada não pode abastecer uma oferta publicada.
- Uma fonte aprovada pode ser vinculada a uma oferta e consultada no fulfillment.
- Suspender uma fonte impede novas alocações e preserva pedidos históricos.
- Divergência registrada fica rastreável até a decisão operacional.

## Decisões pendentes

Escolher B2C, marketplace ou híbrido; definir atores legais, custos, SLA, compliance e necessidade de payout.

