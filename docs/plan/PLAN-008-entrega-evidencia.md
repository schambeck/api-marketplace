# PLAN-008 — Instruções e evidência de entrega

**Prioridade:** Condicional. Reduzir para o método de entrega adotado no MVP.

Se o item for entregue automaticamente, basta registrar resultado e referência segura ao conteúdo entregue. Instruções versionadas, evidência detalhada e contestação entram quando houver entrega manual, códigos sensíveis ou disputa operacional.

**Issue:** [#10](https://github.com/schambeck/api-marketplace/issues/10)

**PRD:** [`PRD-008`](../prd/PRD-008-entrega-evidencia.md) · **Dependências:** PRD-001, PRD-004, PRD-007 e PRD-015.

## Estado atual e alvo

Não há instruções por contexto nem tentativas/evidências vinculadas à execução. O alvo é um schema versionado de instruções, validação pré-fulfillment e registro append-only de tentativa, resultado e evidência.

## Abordagem técnica

- Modelar inicialmente apenas resultado da entrega e, se necessário, uma tentativa com referência segura ao conteúdo ou comprovante.
- Definir campos e validações por contexto/método; permitir correção pelo cliente antes de execução, mantendo versões.
- Aplicar minimização e autorização: operador recebe apenas campos necessários; armazenamento usa referências seguras ao object storage.
- Registrar quantidade, horário, método, responsável, resultado (`DELIVERED`, `PARTIALLY_DELIVERED`, `FAILED`, `DISPUTED`) e checksum/metadados da evidência.
- Separar confirmação/contestação do cliente de alteração do registro original.

## Persistência e APIs

Migration para schema/instruções, tentativas, evidências e vínculos. Atualizar controller de object storage para autorização e expiração de acesso; revisar CORS e logs para não vazar dados.

## Validação

Testar instrução ausente/inválida, correção versionada, escopo de operador, evidência obrigatória, parcial e contestação. Teste de contrato garante que pedido conserva a versão aplicada.

## Decisões e riscos

Campos, consentimento, retenção e prova dependem de cada game/método. Não armazenar conteúdo sensível em logs nem permitir que uma URL de storage pública seja a evidência sem controle de acesso.
