# PRD-015 — Regras por game e publisher

**Rastreabilidade:** BP-027 · Rules · Importante; termos externos a validar.

## Problema e objetivo

As regras de entrega, campos de conta, região, quantidade e manutenção variam por contexto, mas o catálogo não possui configuração nem validação. O produto deve impedir que uma oferta incompatível seja publicada ou executada.

## Requisitos

1. Manter regras versionadas por game, publisher, plataforma, região, servidor e produto/variante.
2. Representar métodos permitidos, campos obrigatórios, limites, janelas e restrições.
3. Validar regras na publicação, checkout e fulfillment.
4. Bloquear ou encaminhar para revisão quando a regra estiver ausente, expirada ou conflitante.
5. Registrar a versão de regras aplicada ao pedido.

## Critérios de aceite

- Oferta que viola regra não é publicada.
- Checkout rejeita instrução incompatível com o contexto.
- Alteração de regra afeta novas vendas sem reescrever pedidos históricos.
- Operador vê a regra e versão que justificaram a decisão.

## Decisões pendentes

Fontes oficiais, processo de aprovação, frequência de atualização, autoridade para interpretar termos e política para conflito.

