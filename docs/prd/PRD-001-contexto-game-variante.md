# PRD-001 — Contexto de game e variante

**Rastreabilidade:** BP-010 · Catálogo · Essencial
**Origem:** matriz de completude; seção 19 de `business-processes.md`.

## Problema e objetivo

O catálogo atual possui atributos genéricos e não determina em qual game, plataforma, região, servidor ou variante uma oferta é válida. O produto deve modelar esse contexto para evitar ofertas ambíguas e permitir regras de entrega específicas.

## Escopo

- cadastrar e versionar contextos válidos;
- associar produto/oferta a uma combinação de contexto;
- exigir os atributos necessários antes da publicação;
- permitir ativar, inativar ou descontinuar um contexto.

Fora do escopo: decidir quais games, publishers, plataformas e termos serão suportados.

## Requisitos

1. Um contexto deve identificar, quando aplicável, game, publisher, plataforma, região, servidor e variante.
2. Combinações incompatíveis não podem ser publicadas.
3. Contextos inativos não podem originar novas ofertas, mas devem permanecer referenciáveis em pedidos históricos.
4. Alterações que impactem uma oferta publicada devem ser auditáveis.

## Critérios de aceite

- Dado um contexto incompleto ou incompatível, a publicação é rejeitada com motivo acionável.
- Dado um contexto ativo, uma oferta pode ser associada e consultada com seus atributos.
- Dado um contexto descontinuado, novas vendas são impedidas sem apagar o histórico.
- O catálogo diferencia produto, contexto e variante na consulta pública.

## Decisões pendentes

Quais campos são obrigatórios por game; se servidor/região são entidades controladas; como publisher terms serão validados; e se variantes têm preço e inventory próprios.
