# PRD-016 — Reviews e reputação

**Rastreabilidade:** BP-029 · Reputation · Evolutivo; a validar.

## Problema e objetivo

`Product.rating` existe, mas não há review, elegibilidade, moderação ou vínculo com transação concluída. Se validada como capacidade do produto, a reputação deve refletir experiências reais e moderadas.

## Escopo

- convidar avaliação após pedido elegível;
- receber nota e comentário;
- moderar, publicar, ocultar e contestar;
- calcular e exibir reputação com origem rastreável.

## Requisitos

1. Apenas cliente com pedido concluído pode avaliar a oferta/serviço correspondente, salvo decisão explícita em contrário.
2. Uma avaliação por transação e janela configurável; edição deve preservar histórico.
3. Conteúdo moderado não pode ser usado no cálculo público enquanto estiver oculto.
4. Rating deve derivar de avaliações elegíveis, não ser um campo arbitrariamente editável.

## Critérios de aceite

- Cliente inelegível não consegue publicar avaliação.
- Avaliação publicada aparece vinculada à transação e à oferta correta.
- Denúncia/moderação altera visibilidade sem apagar auditoria.
- Recalcular rating é determinístico e repetível.

## Decisões pendentes

Se reviews entram no MVP; objetos avaliáveis; janela; anonimização; critérios de moderação e uso operacional da reputação.

