# PLAN-016 — Reviews e reputação

**Issue:** [#18](https://github.com/schambeck/api-marketplace/issues/18)

**PRD:** [`PRD-016`](../prd/PRD-016-reviews-reputacao.md) · **Dependências:** PRD-005, PRD-008 e decisão de entrada no MVP.

## Estado atual e alvo

`Product.rating` é um campo editável sem review ou elegibilidade. O alvo é `Review` ligada a pedido concluído e oferta correta, com janela, moderação, contestação, auditoria e rating derivado determinístico.

## Abordagem técnica

- Criar review, versão de conteúdo, moderation case e audit event; restringir uma review por transação/janela.
- Emitir convite após elegibilidade, mas permitir publicação autenticada com validação de pedido/oferta.
- Separar estados `PENDING`, `PUBLISHED`, `HIDDEN`, `REJECTED` e `CONTESTED`; ocultação não remove histórico.
- Calcular rating a partir apenas de avaliações elegíveis/publicadas, com job/rebuild idempotente e precisão definida.
- Retirar ou tornar derivado `Product.rating`, evitando dupla fonte de verdade.

## Persistência, APIs e testes

Migration para reviews, versões/moderação e agregados. Criar endpoints de cliente, consulta pública e moderação com autorização. Testar inelegibilidade, duplicidade, vínculo correto, edição versionada, ocultação, contestação e recálculo repetível.

## Decisões e riscos

MVP, objetos avaliáveis, janela, anonimização e moderação devem ser aprovados. Não expor comentários ocultos nem usar reputação como decisão operacional antes de política explícita.
