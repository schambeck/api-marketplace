# PRD-012 — Comunicação de eventos

**Rastreabilidade:** BP-024 · Communication · Importante.

## Problema e objetivo

Não existem notificações de lifecycle. O produto deve comunicar eventos que exigem ação ou definem expectativa, sem enviar instruções duplicadas ou conflitantes.

## Requisitos

1. Notificar criação/pagamento do pedido, ação necessária, início/falha/conclusão de entrega, refund, disputa e expiração.
2. Registrar destinatário, canal, template/versionamento, status e tentativa.
3. Deduplicar eventos e respeitar preferências e dados mínimos necessários.
4. Falha de comunicação não pode desfazer a transação de negócio; deve gerar retry/observabilidade.

## Critérios de aceite

- Cada evento elegível gera no máximo uma comunicação por canal e versão.
- Mensagem informa estado, ação e referência do pedido sem expor segredo.
- Falha fica visível para operação e pode ser reenviada com segurança.
- Cliente pode consultar comunicações relevantes do próprio pedido.

## Decisões pendentes

Canais, opt-in/opt-out, templates, idioma, provedor e política de retry.

