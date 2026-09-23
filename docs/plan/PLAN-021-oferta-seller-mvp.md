# PLAN-021 — Oferta pertencente ao seller no MVP

**Tipo:** Plano corretivo sobre o `PLAN-002` já implementado.

**Prioridade:** MVP.

**Objetivo:** adaptar `Offer` para o modelo em que o seller anuncia e entrega o próprio item, removendo conceitos de supply externo e permitindo que o proprietário gerencie sua oferta.

## Motivação

A implementação atual de `Offer` foi desenhada para um marketplace operacional mais amplo. Ela exige `gameContextId`, `availabilitySource`, snapshot versionado, aprovação administrativa e validação de uma fonte de disponibilidade. No MVP, a origem do estoque é simples:

```text
Product.user = seller proprietário
Offer → Product → seller
```

Não deve existir uma segunda fonte de verdade para supplier, source ou seller enquanto o seller for o dono direto do produto.

## Estado implementado

Atualmente `Offer` possui:

- relação obrigatória com `Product` e `GameContext`;
- `contextVersion` e várias colunas de snapshot;
- `deliveryMethod` e `availabilitySource` obrigatórios;
- `availabilityStatus` e ciclo `DRAFT/PUBLISHED/PAUSED/RETIRED`;
- histórico de transições;
- criação, edição e transições protegidas por `ROLE_ADMIN`;
- carrinho que aceita `offerId`, mantendo compatibilidade com `productId`.

## Estado alvo do MVP

A oferta deve conter somente o necessário para vender o item do seller:

- referência ao `Product`, cujo `user` identifica o seller proprietário;
- `game`, `platform`, `region` e `variant` conforme o PLAN-020;
- preço positivo;
- método de entrega informado pelo seller;
- disponibilidade baseada no estoque do próprio produto;
- ciclo comercial `DRAFT`, `PUBLISHED`, `PAUSED` e `RETIRED`;
- histórico simples das transições relevantes;
- nenhuma `availabilitySource` separada;
- nenhuma aprovação ou alocação de supplier no fluxo de publicação.

`Product.user` é a fonte de autorização e propriedade no MVP. Não criar uma entidade `Seller` nem duplicar `seller_id` em `Offer` nesta etapa.

## Alterações previstas

1. Remover `gameContextId`, `contextVersion`, `context` e `availabilitySource` de `OfferWeb`, requests, mapper e service.
2. Adicionar ao contrato de oferta os campos de contexto mínimos definidos no PLAN-020.
3. Validar que o usuário autenticado é o proprietário de `Product.user` ao criar, alterar, publicar, pausar, reativar ou retirar sua oferta.
4. Permitir que o seller gerencie suas próprias ofertas; manter `ADMIN` para moderação, pausa forçada e retirada administrativa.
5. Retirar a chamada a `GameContextService.referenceForOfferPublication` e a validação de compatibilidade de contexto.
6. Manter o histórico de oferta apenas para status e disponibilidade, sem registrar source de supply.
7. Usar `Product.quantity` como estoque do MVP até a implementação da reserva atômica do PLAN-004.
8. Impedir publicação quando o produto não pertencer ao seller autenticado, estiver sem preço válido, sem método de entrega ou sem estoque disponível.
9. Garantir que o carrinho e o checkout preservem `offerId`, preço e seller da oferta adicionada.

## Persistência e migração

Criar nova migration, sem alterar `V12` ou `V13`:

- consolidar em `offer` os campos de contexto do PLAN-020;
- tornar `availability_source` obsoleto para o novo código e removê-lo somente após a transição dos consumidores;
- preservar `offer_status_history`;
- manter ofertas existentes publicadas somente quando tiverem produto válido, preço positivo e estoque disponível;
- marcar ou pausar ofertas legadas que dependam exclusivamente do contexto `LEGACY`, para revisão do seller/admin;
- não criar tabela de `SupplySource`, seller ou settlement neste plano.

## Contrato e autorização

O contrato de criação/edição deve deixar de exigir `gameContextId` e `availabilitySource`. O contrato deve retornar o seller derivado do produto, ou apenas os dados necessários para o comprador, sem expor informações internas de custo ou supply.

A autorização deve seguir estas regras:

- seller autenticado pode criar e editar ofertas de seus próprios produtos;
- seller não pode alterar oferta ou produto de outro usuário;
- ADMIN pode moderar e alterar status conforme política administrativa;
- catálogo público consulta somente ofertas publicadas e disponíveis.

## Integração com o fluxo de pagamento e entrega

Este plano não libera dinheiro ao seller. Ele apenas garante que a oferta pertence a um seller identificável e que o método de entrega está definido. Os planos seguintes devem usar essa relação:

- o pedido captura seller, oferta, preço e quantidade;
- o pagamento aprovado coloca o pedido em `AWAITING_DELIVERY`;
- o seller informa a entrega;
- o comprador confirma a entrega;
- somente então o módulo de pagamento/settlement libera o valor.

## Testes e aceite

- seller cria oferta para produto próprio;
- seller não consegue criar ou editar oferta de produto alheio;
- ADMIN consegue moderar uma oferta;
- oferta não exige `gameContextId` nem `availabilitySource`;
- oferta sem estoque não pode ser publicada;
- publicação e pausa registram histórico sem source de supply;
- catálogo público não exibe oferta `DRAFT`, `PAUSED`, `RETIRED` ou indisponível;
- carrinho mantém preço, `offerId` e seller corretos;
- oferta migrada do modelo anterior mantém preço, contexto mínimo e status revisável.

## Riscos e rollback

A mudança altera o contrato de criação e edição de ofertas e pode exigir migração do frontend. Durante a transição, aceitar os campos antigos apenas como entrada deprecated, sem usá-los como regra de negócio, ou coordenar a migração dos consumidores antes de remover o contrato.

Nenhuma migration existente deve ser editada. O rollback deve reativar temporariamente a leitura de `availability_source` e dos campos legados de contexto, sem restaurar supply externo ou aprovação de supplier.
