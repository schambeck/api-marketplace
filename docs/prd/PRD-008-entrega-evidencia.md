# PRD-008 — Instruções e evidência de entrega

**Rastreabilidade:** BP-017/BP-018 · Delivery · Essencial/importante.

## Problema e objetivo

O backend não coleta dados específicos do jogador nem registra tentativa, evidência ou confirmação. O produto deve obter apenas as instruções necessárias por contexto e provar o resultado da entrega.

## Requisitos

1. Determinar campos por game, plataforma, região, servidor, variante e método.
2. Validar instruções antes de iniciar fulfillment e permitir correção pelo cliente.
3. Proteger dados operacionais e registrar versão usada na execução.
4. Registrar cada tentativa, horário, método, quantidade, responsável, resultado e evidência permitida.
5. Diferenciar entregue, parcialmente entregue, falha e contestação.

## Critérios de aceite

- Checkout não avança quando instrução obrigatória está ausente ou inválida.
- Operador vê somente os dados necessários para executar a entrega.
- Tentativa concluída possui evidência vinculada ao pedido/fulfillment.
- Cliente pode confirmar ou contestar sem apagar o registro original.

## Decisões pendentes

Campos por game; retenção e consentimento; métodos permitidos; definição de prova; e prazo de confirmação.

