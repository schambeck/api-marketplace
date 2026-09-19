# Processos de Negócio — Modelo Atual e Análise de Completude

## 1. Sumário executivo

Este documento preserva o baseline observável do backend e acrescenta uma análise de completude do modelo de negócio. O backend cobre cadastro automático de usuários autenticados, publicação e consulta de produtos, imagens, carrinho e preparação/registro do pagamento via Mercado Pago. O fluxo implementado termina na criação de uma preferência de pagamento e no armazenamento de dados recebidos; não há, no código analisado, pedido, reserva/baixa de estoque, fulfillment, entrega, estorno ou fechamento financeiro.

A evidência mostra um modelo predominantemente CRUD, com algumas regras concentradas no carrinho e na integração de pagamento. Não foram encontrados testes versionados em `src/test`, consumidores de fila, schedulers ou eventos de domínio publicados pela aplicação.

## 2. Escopo e método

Foram cruzados `AGENTS.md`, estrutura do projeto, controllers, services, entidades, repositories, migrations, configuração de segurança, integrações e arquivos de configuração. A classificação usada neste documento é:

- **Implementado**: comportamento diretamente observável no código executável.
- **Inferido**: interpretação de negócio sustentada por mais de uma evidência, mas sem regra formal.
- **Ausente**: esperado em um marketplace, porém não localizado no projeto.
- **A validar**: depende de contrato externo, configuração ou comportamento que não possui teste.

As referências abaixo usam o caminho do arquivo e a linha inicial do trecho relevante.

## 3. Visão geral do sistema

```text
Identidade JWT/Keycloak
        ↓ autenticação bem-sucedida
Cadastro local de User
        ↓
Catálogo público ← Product ← imagens em GCS/MinIO
        ↓ seleção
Carrinho único por usuário ← Cart + CartItem
        ↓ checkout
Preferência Mercado Pago
        ↓ retorno/webhook/registro manual
Payment e Webhook persistidos
```

O acesso é protegido por JWT; catálogo GET, download de objetos, GET de OAuth e POST de webhooks são públicos, enquanto as demais rotas exigem autenticação (`SecurityConfig.java:31-46`).

## 4. Domínios identificados

| Domínio | Responsabilidade | Entidades principais | Operações de negócio |
|---|---|---|---|
| Identidade e conta | Reconhecer o usuário do provedor JWT e manter seu cadastro local | `User` | Criar no primeiro login, consultar, atualizar dados e chave pública |
| Catálogo e oferta | Publicar e consultar produtos por vendedor | `Product` | Criar, editar, excluir, listar, pesquisar e listar por usuário |
| Mídia e object storage | Associar imagens a produtos e transportar arquivos | `Image`, objeto externo | Upload/download, criar/editar/excluir vínculo de imagem |
| Carrinho | Manter uma cesta por usuário e seu total calculado | `Cart`, `CartItem` | Adicionar, impedir duplicidade opcional, alterar quantidade, remover, contar |
| Pagamento | Preparar checkout Mercado Pago e registrar retornos | `Payment`, `PaymentDetail` | Criar preferência, relacionar pagamento ao carrinho, atualizar/consultar |
| Integração Mercado Pago | Autorizar o marketplace a operar em nome do usuário | OAuth request/code/token | Iniciar autorização, trocar code por token e guardar public key |
| Webhook | Receber e persistir payloads de integração | `Webhook` | Gravar payload estruturado; endpoint `location` apenas acusa recebimento |

## 5. Atores e entradas

| Ator | Entrada | Processo iniciado |
|---|---|---|
| Usuário autenticado | Login JWT | Provisionamento local da conta |
| Usuário/cliente | `GET /catalog/**` | Consulta da oferta |
| Usuário/vendedor autenticado | CRUD de `products`, `images` e arquivos | Gestão de catálogo e mídia |
| Usuário autenticado | `POST/PUT/DELETE /shopping-cart/**` | Composição do carrinho |
| Usuário autenticado | `POST /mercado-pago/preferences` | Preparação do checkout |
| Mercado Pago/cliente autorizado | `POST /webhooks` | Registro de notificação |
| Usuário autenticado + Mercado Pago | `GET /oauth/start-flow`, callback `GET /oauth?code=...` | Vinculação da conta Mercado Pago |

Não há entradas automáticas identificadas: nenhuma classe com scheduler, job, consumer de fila, retry de negócio ou listener de webhook que avance o pagamento foi encontrada. O único listener de aplicação é o de sucesso de autenticação.

## 6. Cadeia de valor

1. Usuário autentica no provedor e passa a existir localmente.
2. Vendedor mantém produtos e suas imagens.
3. Cliente consulta o catálogo e compõe seu carrinho.
4. Cliente solicita uma preferência com os itens do carrinho.
5. Mercado Pago recebe a preferência e conduz o checkout externo.
6. O backend pode registrar `Payment` e `Webhook`, mas não transforma o pagamento em pedido concluído.

## 7. Catálogo de processos

| ID | Processo | Situação |
|---|---|---|
| BP-001 | Provisionar usuário no primeiro login | Implementado |
| BP-002 | Consultar catálogo | Implementado |
| BP-003 | Publicar e manter produto | Implementado |
| BP-004 | Armazenar e associar imagem | Implementado parcialmente |
| BP-005 | Montar e manter carrinho | Implementado |
| BP-006 | Criar preferência de pagamento | Implementado |
| BP-007 | Registrar pagamento recebido | Implementado como persistência |
| BP-008 | Registrar webhook | Implementado como persistência |
| BP-009 | Autorizar Mercado Pago para o usuário | Implementado com lacunas de segurança/validade |
| BP-010 | Configurar contexto de game, plataforma, região e variante | Ausente — essencial |
| BP-011 | Publicar e administrar disponibilidade da oferta | Parcialmente modelado |
| BP-012 | Definir modelo de supply e adquirir/register supply | Ausente — essencial; modelo a validar |
| BP-013 | Reservar, alocar e consumir inventory | Ausente — essencial |
| BP-014 | Criar e administrar pedido a partir do checkout | Ausente — essencial |
| BP-015 | Confirmar pagamento e processar webhook idempotentemente | Parcialmente modelado |
| BP-016 | Criar e executar fulfillment | Ausente — essencial |
| BP-017 | Coletar e validar instruções de entrega | Ausente — essencial |
| BP-018 | Registrar tentativa, evidência e confirmação de entrega | Ausente — importante |
| BP-019 | Tratar falha, timeout e fulfillment parcial | Ausente — importante |
| BP-020 | Expirar e cancelar carrinho, pedido ou reserva | Ausente — importante |
| BP-021 | Solicitar, decidir e executar refund | Ausente — importante |
| BP-022 | Tratar disputa e chargeback | Ausente — importante |
| BP-023 | Operar suporte pré-venda, venda e pós-venda | Ausente — importante |
| BP-024 | Comunicar eventos relevantes ao cliente e operador | Ausente — importante |
| BP-025 | Reconciliar pagamentos, entregas, inventory e valores devidos | Ausente — importante |
| BP-026 | Avaliar risco e executar revisão manual | Ausente — importante; políticas a validar |
| BP-027 | Administrar regras por game/publisher e restrições | Ausente — importante; validação externa |
| BP-028 | Operar fila de fulfillment e escalonamento | Ausente — importante |
| BP-029 | Coletar reviews e administrar reputação | Ausente — evolutivo; a validar |

## 8. Fluxos detalhados

### BP-001 — Provisionar usuário no primeiro login

**Objetivo:** criar a representação local necessária para relacionar produtos, carrinho e credenciais ao usuário autenticado.

**Gatilho:** evento `AuthenticationSuccessEvent` após autenticação JWT.

**Fluxo:** o sistema extrai username, e-mail, nome e identificador externo; verifica se já existe username; se não existir, grava `User` com esses dados (`AuthenticationEvents.java:19-33`). Se já existir, não atualiza automaticamente o cadastro.

**Regras e decisões:** username é a chave de existência usada pelo listener; o identificador externo vem de `authentication.getName()`; criação duplicada é evitada apenas pela verificação de existência, apoiada pelo índice único de username (`V1__create_user.sql`).

**Estado alterado:** `User` passa de inexistente para persistido. Não há estados de bloqueio, ativação ou desativação.

**Resultado:** conta local disponível para os demais processos. Falhas de claim obrigatório podem interromper a criação (`AuthenticationService.java:25-31`).

### BP-002 — Consultar catálogo

**Objetivo:** permitir que qualquer visitante consulte a oferta publicada.

**Gatilho:** `GET /catalog` ou `GET /catalog/{id}`.

**Fluxo:** recebe paginação e busca opcional; sem texto, lista produtos por id ascendente; com texto, filtra por nome; por id, exige produto existente. A rota é pública (`CatalogController.java:14-28`, `ProductService.java:42-59`, `SecurityConfig.java:33`).

**Regras:** a pesquisa é case-insensitive; a paginação é preservada; produto inexistente gera erro de entidade não encontrada. O processo não valida `inventoryStatus` e não impede a visualização de produtos marcados como `OUTOFSTOCK`.

**Resultado:** representação `ProductWeb`; nenhuma mudança de estado.

### BP-003 — Publicar e manter produto

**Objetivo:** permitir ao usuário criar e administrar uma oferta.

**Gatilho:** CRUD autenticado de `/products`.

**Fluxo principal:** recebe dados do produto; no caminho associado ao usuário, resolve o username autenticado e grava a relação `Product.user`; para atualização, carrega a entidade existente, copia campos e salva; para remoção, exclui pelo id. Há consultas gerais e consultas filtradas por vendedor (`ProductService.java:17-59`).

**Dados de negócio:** nome, descrição, preço, quantidade, categoria, `inventoryStatus`, rating, imagem e `objectId` (`Product.java:20-65`). A migration exige esses campos como não nulos (`V2__create_product.sql`).

**Estados observados:** `inventoryStatus` contém valores como `INSTOCK`, `LOWSTOCK` e `OUTOFSTOCK` nos dados iniciais, mas não existe enum nem transição calculada no serviço. Portanto, o backend armazena/exibe o status; não implementa a política de estoque.

**Resultado:** produto persistido, atualizado, consultado ou removido. Não há reserva, decremento de quantidade, avaliação ou autorização explícita por proprietário no serviço analisado.

### BP-004 — Armazenar e associar imagem

**Objetivo:** transportar um arquivo para GCS ou MinIO e associar seus metadados a um produto.

**Fluxo de arquivo:** upload simples ou múltiplo em `/object-storage`; o serviço cria o bucket se necessário, copia o arquivo para temporário, gera `UUID + extensão`, envia ao bucket e devolve id, nome original, tipo e tamanho (`StorageService.java:44-77`). Download recebe o id, baixa para arquivo temporário e devolve o stream (`StorageService.java:83-92`). GET de object storage é público (`SecurityConfig.java:35`).

**Fluxo de vínculo:** `/images` cria/edita/remove `Image`; o mapper resolve o produto pelo id (`ImageMapper.java:15-27`) e a entidade guarda `objectId`, nome, tipo e tamanho (`Image.java:20-60`).

**Limites:** upload e vínculo são processos separados; não há transação compensatória caso o upload ocorra e o vínculo falhe, nem remoção automática do objeto quando a imagem é excluída. A seleção GCS/MinIO depende de configuração.

### BP-005 — Montar e manter carrinho

**Objetivo:** manter os itens selecionados pelo usuário e o total monetário atual.

**Adicionar item:** recebe produto e quantidade, carrega o produto, calcula `preço × quantidade` com escala 2 e `HALF_UP`, cria ou reutiliza o carrinho único do usuário, soma o total e grava `CartItem` com preço congelado no momento da inclusão (`CreateShoppingCartItemService.java:30-37`, `60-70`). A unicidade de carrinho por usuário é reforçada pelo índice `idx_cart_user_id` (`V3__create_cart.sql`).

**Adicionar se ausente:** verifica se já existe item do mesmo produto para aquele usuário; se existir, retorna `created=false` sem alterar quantidade; caso contrário, executa a inclusão (`CreateShoppingCartItemService.java:40-46`).

**Alterar quantidade:** recarrega o item, recalcula seu total usando o preço armazenado, substitui no total do carrinho e salva (`UpdateShoppingCartItemService.java:21-34`).

**Remover item:** exclui o item; se restarem itens, subtrai seu total do carrinho; se não restar nenhum, exclui o carrinho (`RemoveShoppingCartItemService.java:18-29`).

**Consultar:** lista itens do usuário e conta itens; consultas administrativas também existem em `/cart-items`, `/carts` e `/shopping-cart`.

**Regras explícitas:** um carrinho por usuário; total é soma dos subtotais; subtotal é preço do item multiplicado pela quantidade; inclusão condicional não duplica produto. Não foram encontradas validações de quantidade positiva, estoque disponível, preço vigente, proprietário do item na atualização/remoção ou congelamento transacional do catálogo.

**Ciclo de vida:** `inexistente → aberto/com itens → aberto/com itens alterados → inexistente` quando o último item é removido. Não há estado `checked-out`, `paid`, `cancelled` ou `expired` para `Cart`.

### BP-006 — Criar preferência de pagamento

**Objetivo:** transformar o carrinho atual em uma sessão de checkout Mercado Pago.

**Pré-condições:** usuário autenticado, carrinho existente, chave pública do usuário preenchida e itens localizados. A ausência do carrinho ou da chave interrompe o processo (`CreatePreferenceService.java:51-54`).

**Fluxo:** carrega carrinho por username; converte cada item para título, descrição, imagem, categoria, quantidade, moeda BRL e preço unitário; cria `PreferenceRequest` com `externalReference = cart.id`, taxa de marketplace de `0.03`, retorno automático e a mesma URL para sucesso, falha e pendência; envia à API Mercado Pago com chave de idempotência aleatória (`CreatePreferenceService.java:51-83`).

**Integrações:** SDK `PreferenceClient` do Mercado Pago. Exceções da API são registradas e convertidas em `RuntimeException` (`CreatePreferenceService.java:55-60`).

**Resultado:** preferência externa devolvida ao cliente. O carrinho não muda de estado, não é esvaziado e não é marcado como pago.

### BP-007 — Registrar pagamento recebido

**Objetivo:** persistir dados de pagamento associados a um carrinho.

**Gatilho atual:** chamada autenticada ao CRUD `/mercado-pago/payments`; não há evidência de que o webhook invoque este serviço.

**Fluxo:** ao criar, converte `externalReference` para inteiro, localiza o carrinho pelo id e salva `Payment`; atualização copia os campos para o pagamento existente; consulta permite filtrar por texto no status (`PaymentService.java:17-45`). A tabela guarda ids do pagamento, status, referência externa, merchant order e detalhe JSON (`Payment.java:20-62`, `V5__create_payment.sql`).

**Estados:** `status` é texto livre. Não há enum, máquina de estados, validação de transição, idempotência por `paymentId` ou regra que vincule status a um avanço do carrinho.

**Resultado:** registro persistido. O processo não confirma pedido, não baixa estoque, não dispara evento e não liquida/reembolsa.

### BP-008 — Registrar webhook

**Objetivo:** aceitar e armazenar notificações externas para posterior consulta/auditoria.

**Fluxo:** POST `/webhooks` é público, mapeia o payload para `Webhook` e salva action, versão, data, id externo, modo live, tipo, usuário e JSON bruto (`WebhookController.java:21-24`, `Webhook.java:20-72`). POST `/webhooks/location` apenas registra no log e responde 204 (`WebhookController.java:27-30`).

**Conclusão:** existe recepção/persistência de webhook, mas não processamento de negócio. Não há consumidor que atualize `Payment`, `Cart` ou `Product` após o webhook.

### BP-009 — Autorizar Mercado Pago para o usuário

**Objetivo:** obter credenciais Mercado Pago e a public key necessárias para o checkout.

**Fluxo principal:** usuário autenticado chama `/oauth/start-flow`; o sistema localiza o usuário por external id, grava request com client id, redirect URI e state UUID, guarda o state em campo da instância do controller e devolve URL de autorização. No callback com `code`, recupera o request pelo state, grava authorization code, chama Mercado Pago com `authorization_code`, salva access/refresh token, public key e escopo e copia a public key para `User`; depois redireciona ao frontend (`OAuthController.java:38-58`, `OAuthService.java:40-95`).

**Estados persistidos:** request OAuth; authorization code com `used=false`; token com `expiresAt` preenchido com o instante atual. Não há consumo/invalidação do code, checagem de expiração, refresh, revogação ou uso de `expiresIn` no cálculo de validade (`OAuthService.java:57-86`).

**Risco/lacuna:** o state é mantido em atributo singleton do controller, não por sessão/usuário, e o callback recebe somente `code`; a correlação concorrente e a validação de state/redirect URI devem ser revisadas antes de considerar o processo robusto.

## 9. Regras de negócio catalogadas

| ID | Regra | Condição | Resultado | Evidência |
|---|---|---|---|---|
| BR-001 | Usuário local único por username | Login bem-sucedido | Cria apenas se username não existir | `AuthenticationEvents.java:22-33`; `V1__create_user.sql` |
| BR-002 | Um carrinho por usuário | Inclusão/consulta de item | Reutiliza carrinho existente; índice único impede duplicidade | `CreateShoppingCartItemService.java:60-66`; `V3__create_cart.sql` |
| BR-003 | Subtotal monetário | Inclusão ou alteração | `price × quantity`, escala 2, arredondamento HALF_UP | `CreateShoppingCartItemService.java:69-70`; `UpdateShoppingCartItemService.java:32-34` |
| BR-004 | Total acompanha itens | Inclusão, alteração ou remoção | Soma, substituição ou subtração de subtotais | serviços de shopping cart |
| BR-005 | Inclusão condicional | Produto já está no carrinho | Não cria segundo item e informa `created=false` | `CreateShoppingCartItemService.java:40-46` |
| BR-006 | Referência do pagamento é o carrinho | Criação de Payment | `externalReference` deve ser conversível para id de Cart existente | `PaymentService.java:17-21` |
| BR-007 | Checkout exige public key | Criação de preferência | Sem chave pública, falha antes da chamada externa | `CreatePreferenceService.java:51-52` |
| BR-008 | Preferência carrega itens do carrinho | Checkout | Itens viram linhas Mercado Pago em BRL | `CreatePreferenceService.java:53-57`; `CartPreferenceConverter.java` |
| BR-009 | Webhook é público | Recepção externa | POST pode ser chamado sem JWT e é persistido | `SecurityConfig.java:36`; `WebhookController.java:21-24` |
| BR-010 | Produto inexistente interrompe operações relacionadas | Consulta/associação | Repository lança exceção de não encontrado | repositories de `Product`, `Cart`, `CartItem` e `User` |

Regras que **não** podem ser atribuídas ao sistema por falta de evidência: impedir compra sem estoque, recalcular `inventoryStatus`, reservar estoque, criar pedido, validar titularidade do item, confirmar pagamento, deduplicar webhook ou atualizar pagamento a partir de notificação.

## 10. Ciclos de vida e estados

### Produto

```text
não persistido → persistido/publicado → atualizado → removido
```

`inventoryStatus` é um atributo editável, não um ciclo implementado. Valores iniciais sugerem `INSTOCK`, `LOWSTOCK` e `OUTOFSTOCK`, mas não há transições automáticas.

### Carrinho

```text
inexistente → criado ao adicionar item → com itens/total calculado
                                      ↓
                         itens alterados ou removidos
                                      ↓
                         inexistente ao remover o último item
```

### Pagamento

```text
sem registro → Payment persistido com status recebido → status editável
```

Não existe vocabulário controlado de status nem fluxo de aprovação/recusa.

### OAuth Mercado Pago

```text
sem autorização → request/state criado → code salvo (used=false)
                                      → token salvo + public key no User
```

As transições de expiração, uso único e renovação não estão implementadas.

## 11. Eventos, integrações e transações

| Tipo | Evidência | Efeito |
|---|---|---|
| Evento de autenticação | `AuthenticationSuccessEvent` | Provisiona usuário local se ausente |
| Evento de domínio | Nenhum encontrado | Não há publicação |
| Fila/consumer | Nenhum encontrado | Não há processamento assíncrono |
| Mercado Pago OAuth | `MercadoPagoClient`/Feign | Troca code por tokens e public key |
| Mercado Pago Preference API | SDK `PreferenceClient` | Cria checkout externo |
| GCS | `GoogleStorageService` | Bucket, upload e download |
| MinIO | `MinioStorageService` | Bucket, upload e download |
| Keycloak/JWT | Resource server | Autentica chamadas e fornece claims |
| Webhook externo | `POST /webhooks` | Persiste JSON; não propaga mudança |

As operações do carrinho e CRUDs usam transações locais Spring, mas a criação de preferência e a persistência de Payment/Webhook não formam transação distribuída. Não há outbox, saga, compensação ou retry de negócio.

## 12. Happy path ponta a ponta

```text
Usuário autentica
  → User local é criado (se necessário)
  → consulta catálogo
  → adiciona produtos ao carrinho
  → altera quantidades conforme necessário
  → inicia/vincula Mercado Pago, se public key ausente
  → solicita preferência
  → Mercado Pago recebe itens e externalReference do carrinho
  → cliente conclui o checkout no provedor
```

O último passo que o backend implementa é a criação da preferência. A conclusão do pagamento no provedor não possui, neste repositório, uma continuação ponta a ponta até pedido ou estoque.

## 13. Exception paths e lacunas

| Situação | Comportamento observado |
|---|---|
| Usuário/produto/carrinho/item inexistente | Repository lança exceção de entidade não encontrada; tratamento HTTP geral retorna 404 para `EntityNotFoundException` |
| Produto não encontrado ao adicionar item | Inclusão falha antes de alterar carrinho |
| Produto já no carrinho em `createIfAbsent` | Retorno normal com `created=false` |
| Carrinho ausente no checkout | Falha de busca; não há preferência |
| Public key ausente | `RuntimeException`; checkout não é enviado |
| Erro Mercado Pago | Log e `RuntimeException`; não há retry/compensação |
| Falha object storage | `RuntimeException`; metadado `Image` não é criado automaticamente |
| Webhook inválido/duplicado | Não há validação, assinatura ou deduplicação visível |

Lacunas prioritárias para validação do negócio:

1. Definir se `Product.quantity` e `inventoryStatus` devem impedir inclusão/checkout e como são atualizados.
2. Criar ou confirmar o conceito de pedido; hoje `Cart` é usado como referência externa de pagamento.
3. Implementar o consumidor do webhook que atualiza Payment e o resultado do checkout, se isso fizer parte do escopo.
4. Definir a máquina de estados e transições de Payment.
5. Validar titularidade/autorização nas operações por id de carrinho, item, produto e imagem.
6. Corrigir o ciclo de vida OAuth: state por sessão, validação no callback, code usado uma vez e expiração real.
7. Adicionar testes de comportamento para total, duplicidade, remoção do último item, erro de integração e webhook.

## 14. Inconsistências e riscos de interpretação

- `Payment` parece representar um retorno do Mercado Pago, mas sua criação é uma rota CRUD autenticada e não está conectada ao webhook.
- O nome `OAuthAuthorizationCode.expiresAt` é preenchido com `LocalDateTime.now()`, sem usar a expiração do provedor; isso não comprova uma validade funcional.
- A existência de `PaymentWebhook.java` não representa um fluxo ativo: o arquivo está comentado.
- `CreatePreferenceService` verifica `publicKey`, mas a configuração de access token aparece comentada; a autenticação efetiva do SDK depende da configuração/runtime não demonstrada neste código.
- Os dados iniciais usam `OUTOFSTOCK` em produtos com quantidades não uniformes e `INSTOCK` em pelo menos um produto com quantidade zero; não se deve inferir que o status seja calculado pela quantidade.
- Há referências de rota e comportamento de object storage que devem ser conferidas com consumidores externos; o código analisado expõe `/object-storage/download/{objectId}`.

## 15. Matriz de rastreabilidade

| Processo | Entrada | Regra/decisão | Estado/dado alterado | Integração/evento | Saída |
|---|---|---|---|---|---|
| BP-001 | AuthenticationSuccessEvent | Criar se username ausente | `User` criado | Keycloak/JWT | Conta local |
| BP-002 | GET catálogo | Busca opcional e paginação | Nenhum | Nenhuma | Produtos |
| BP-003 | CRUD products | Resolver usuário no create associado | `Product` | Nenhuma | Oferta |
| BP-004 | Upload/CRUD images | Bucket/objeto e vínculo separados | Bucket + `Image` | GCS/MinIO | Metadados/arquivo |
| BP-005 | Shopping cart | Carrinho único, total calculado | `Cart`, `CartItem` | Nenhuma | Carrinho atualizado |
| BP-006 | POST preference | Exigir public key; enviar itens | Nenhuma local | Mercado Pago Preference API | Preference |
| BP-007 | CRUD payment | externalReference deve ser Cart id | `Payment` | Nenhuma automática | Registro |
| BP-008 | POST webhook | Persistir JSON | `Webhook` | Provedor externo → API | Auditoria |
| BP-009 | OAuth start/callback | state e code | OAuth request/code/token + User public key | Mercado Pago OAuth | Credencial vinculada |

## 16. Glossário

- **User:** conta local vinculada ao subject/external id do JWT.
- **Product:** oferta publicada por um usuário, contendo preço e atributos de estoque informados.
- **Cart:** cesta única do usuário, com total agregado.
- **CartItem:** linha da cesta com preço capturado, quantidade e subtotal.
- **Preference:** sessão/solicitação de checkout criada no Mercado Pago.
- **Payment:** registro local com dados de pagamento e status recebidos/administrados.
- **Webhook:** payload de notificação persistido, sem processamento de negócio demonstrado.
- **Public key:** chave do usuário Mercado Pago usada como pré-condição do checkout.

## 17. Conclusão

O processo de negócio efetivamente coberto é: **autenticar usuário → publicar/consultar oferta → montar carrinho → gerar preferência de pagamento → persistir registros de integração**. O backend oferece os componentes de uma jornada de marketplace, mas não implementa a etapa que transforma checkout aprovado em pedido, baixa de estoque, fulfillment ou encerramento da venda. Essa fronteira deve ser tratada como a principal lacuna funcional, e não como comportamento implícito.

## 18. Modelo operacional e cadeia de valor esperada

### 18.1 Classificação do modelo atual

**[EVIDÊNCIA INTERNA]** O documento existente chama o sistema de marketplace e associa `Product` a um usuário, mas não documenta onboarding de sellers independentes, ofertas separadas do produto, supply externo, comissões ou payouts.

**[INFERIDO]** O modelo atual é um catálogo transacional com usuários que podem publicar produtos; ainda não há evidência suficiente para classificá-lo como marketplace multi-seller. A ausência de supply e seller settlement torna prematuro assumir um marketplace completo.

**[A VALIDAR — MODELO OPERACIONAL]** Decidir entre:

1. **B2C com inventory/supply controlado pela plataforma**: exige aquisição, inventory, reserva, fulfillment e suporte operados pela plataforma.
2. **Marketplace**: exige seller onboarding, aprovação, seller offer, SLA, disputa, comissão e settlement.
3. **Híbrido**: exige separar claramente inventory próprio, supplier e seller, inclusive para responsabilidade de entrega e pagamento.

Como referência, a MMOExp declara publicamente uma operação B2C com supply e inventory controlados e canais próprios de fulfillment, não um marketplace C2C genérico ([About MMOEXP](https://www.mmoexp.com/About.html)). Isso é evidência externa sobre uma referência, não decisão para este produto.

### 18.2 Cadeia de valor mínima para validar

```text
Game context and rules
        ↓
Supply / sourcing
        ↓
Inventory and availability
        ↓
Catalog and offer publication
        ↓
Discovery and cart
        ↓
Checkout and order creation
        ↓
Payment confirmation and risk decision
        ↓
Reservation / allocation
        ↓
Fulfillment and delivery
        ↓
Evidence and customer confirmation
        ↓
Support, refund and dispute
        ↓
Reconciliation and settlement
```

**[EVIDÊNCIA INTERNA]** A documentação atual cobre apenas identidade, catálogo, carrinho e criação/persistência de artefatos de pagamento.

**[GAP]** As transições entre pagamento confirmado, pedido, allocation, entrega, pós-venda e reconciliação não possuem processo correspondente.

## 19. Processos adicionados e expandidos

Os processos abaixo são capacidades de negócio, não afirmações de implementação. Cada um tem objetivo, ator, gatilho, resultado e evidência suficiente para ser incorporado ao modelo esperado.

### BP-010 — Configurar contexto de game, plataforma, região e variante

**Classificação:** [FALTANTE — ESSENCIAL]

**Objetivo:** determinar o contexto no qual um item ou serviço pode ser vendido e entregue.

**Atores:** administrador de catálogo, operador e cliente; game/publisher como restrição externa.

**Trigger:** onboarding ou alteração de game, plataforma, região, servidor, categoria, tipo de produto ou variante.

**Fluxo e resultado:** cadastrar o contexto válido; associar atributos obrigatórios, métodos de entrega e restrições; publicar somente combinações válidas. O resultado é uma definição de produto/variante que pode ser precificada e ofertada.

**Estados/eventos:** `DRAFT → ACTIVE → INACTIVE`; `GameContextConfigured`.

**Exception paths:** combinação incompatível, contexto descontinuado, regra do publisher não confirmada.

**Evidência:** **[EVIDÊNCIA INTERNA]** `Product` possui campos genéricos, sem modelar game/platform/server ou dados de entrega. **[EVIDÊNCIA EXTERNA]** páginas públicas da MMOExp organizam catálogo por games, produtos, plataformas e tipos de serviço ([home](https://www.mmoexp.com/), [delivery times](https://www.mmoexp.com/About/how-long-will-it-take-to-receive-my-order-after-payment.html)). **[A VALIDAR]** quais games, plataformas e campos serão suportados.

### BP-011 — Publicar e administrar disponibilidade da oferta

**Classificação:** [PARCIALMENTE MODELADO]

**Objetivo:** transformar um produto catalogado em uma oferta vendável, com preço, quantidade, contexto, disponibilidade e método de entrega vigentes.

**Atores:** administrador, operador, fornecedor/seller se aplicável.

**Trigger:** criação, alteração, indisponibilidade ou reposição de supply.

**Fluxo e resultado:** validar produto e contexto; determinar preço e disponibilidade; publicar, ocultar, pausar ou reativar a oferta. A oferta não deve ser confundida com o `Product` genérico.

**Estados/eventos:** `DRAFT → PUBLISHED → PAUSED/OUT_OF_STOCK → PUBLISHED/RETIRED`; `OfferPublished`, `AvailabilityChanged`.

**Evidência:** **[EVIDÊNCIA INTERNA]** BP-003 publica produto e armazena `inventoryStatus`, mas não calcula disponibilidade nem impede venda. **[INFERIDO]** o domínio precisa separar “o que existe no catálogo” de “o que pode ser comprado agora”.

### BP-012 — Definir modelo de supply e adquirir/register supply

**Classificação:** [FALTANTE — ESSENCIAL]; **[A VALIDAR — MODELO OPERACIONAL]**

**Objetivo:** garantir origem, disponibilidade e responsabilidade pelo item/serviço vendido.

**Atores:** administrador, operador, supplier, seller ou parceiro, conforme modelo escolhido.

**Trigger:** necessidade de reposição, novo produto, contratação de supplier ou venda com aquisição on-demand.

**Fluxo e resultado:** aprovar fonte; registrar custo, capacidade, restrições e status; confirmar recebimento ou disponibilidade; associar a uma oferta. No modelo marketplace, incluir onboarding, aprovação, SLA, suspensão e settlement de seller.

**Exception paths:** source indisponível, qualidade insuficiente, supplier não aprovado, divergência de quantidade ou custo.

**Evidência:** **[EVIDÊNCIA INTERNA]** não há supplier, seller, sourcing, custo ou settlement. **[EVIDÊNCIA EXTERNA]** a MMOExp declara inventory management, supply control e direct-sales fulfillment ([About MMOEXP](https://www.mmoexp.com/About.html)).

### BP-013 — Reservar, alocar e consumir inventory

**Classificação:** [FALTANTE — ESSENCIAL]

**Objetivo:** impedir overselling e vincular supply disponível a um pedido/fulfillment.

**Atores:** plataforma, operador, inventory service e supplier/seller quando aplicável.

**Trigger:** pedido pago/aprovado ou decisão de iniciar fulfillment.

**Fluxo:** verificar disponibilidade; reservar; alocar uma fonte; expirar ou liberar reserva quando necessário; consumir após entrega confirmada; reconciliar saldo.

**Estados/eventos:** `AVAILABLE → RESERVED → ALLOCATED → CONSUMED`, com `RELEASED` e `EXPIRED`; `InventoryReserved`, `InventoryReleased`, `InventoryConsumed`.

**Evidência:** **[EVIDÊNCIA INTERNA]** `Product.quantity` e `inventoryStatus` são apenas dados editáveis, sem transição, reserva ou baixa. **[EVIDÊNCIA EXTERNA]** a referência descreve stock lock, allocation e decremento em tempo real para contas ([produto Steam](https://www.mmoexp.com/Account/Steam/169425005.html)).

### BP-014 — Criar e administrar pedido a partir do checkout

**Classificação:** [FALTANTE — ESSENCIAL]

**Objetivo:** criar o registro comercial que representa a intenção de compra e coordena pagamento, fulfillment, entrega e pós-venda.

**Atores:** cliente, plataforma, operador e payment provider.

**Trigger:** confirmação final do checkout.

**Fluxo:** congelar linhas, preço, contexto, dados de entrega e termos; criar pedido; associar carrinho/preference; encaminhar para pagamento e risco; impedir que `Cart` seja a referência definitiva da venda.

**Estados:** `CREATED → PAYMENT_PENDING → PAID/REJECTED → FULFILLING → DELIVERED → COMPLETED`, com `CANCELLED`, `EXPIRED`, `REFUNDED`, `DISPUTED` e `PARTIALLY_DELIVERED` quando aplicável.

**Evidência:** **[EVIDÊNCIA INTERNA]** o fluxo atual termina na preference e usa `Cart.id` como `externalReference`; não existe entidade/processo de pedido.

### BP-015 — Confirmar pagamento e processar webhook idempotentemente

**Classificação:** [PARCIALMENTE MODELADO]

**Objetivo:** transformar uma notificação/consulta confiável do gateway em decisão de pagamento aplicável ao pedido.

**Atores:** Mercado Pago, plataforma e operador de pagamentos.

**Trigger:** webhook, consulta de status, retry ou reconciliação.

**Fluxo:** autenticar/validar notificação; localizar pedido por referência segura; deduplicar evento; validar transição; atualizar payment/order; disparar reserva/fulfillment; registrar falhas para retry.

**Exception paths:** webhook duplicado, fora de ordem, inválido, assinatura ausente, payment inexistente ou status incompatível.

**Evidência:** **[EVIDÊNCIA INTERNA]** BP-008 apenas persiste JSON e BP-007 persiste `Payment`; não há confirmação, idempotência ou avanço do pedido. **[A VALIDAR]** contrato de assinatura e eventos efetivamente disponíveis no Mercado Pago.

### BP-016 — Criar e executar fulfillment

**Classificação:** [FALTANTE — ESSENCIAL]

**Objetivo:** converter um pedido pago e alocado em uma entrega executável.

**Atores:** plataforma, operador, supplier/seller e cliente.

**Trigger:** pagamento aprovado, risco liberado e supply alocado.

**Fluxo:** criar fulfillment; atribuir fonte/operador; preparar entrega conforme game e produto; iniciar tentativa; registrar progresso; concluir ou encaminhar exceção.

**Estados/eventos:** `PENDING → ASSIGNED → IN_PROGRESS → DELIVERED/FAILED`; `FulfillmentCreated`, `FulfillmentStarted`, `FulfillmentCompleted`.

**Evidência:** **[EVIDÊNCIA INTERNA]** não há fulfillment, assignment, fila ou entrega. **[EVIDÊNCIA EXTERNA]** a referência pública descreve order, pay, allocation e delivery como etapas distintas ([produto Steam](https://www.mmoexp.com/Account/Steam/169425005.html)).

### BP-017 — Coletar e validar instruções de entrega

**Classificação:** [FALTANTE — ESSENCIAL]

**Objetivo:** obter os dados necessários para executar a entrega sem tratar campos específicos de um game como universais.

**Atores:** cliente, plataforma e operador.

**Trigger:** checkout ou início de fulfillment.

**Fluxo:** determinar campos por game × produto × plataforma × servidor × método; coletar e validar; solicitar correção; proteger acesso operacional e registrar versão usada na entrega.

**Evidência:** **[EVIDÊNCIA INTERNA]** não existem dados de character, nickname, server, region, platform ou instruções. **[EVIDÊNCIA EXTERNA]** a MMOExp informa delivery por mail/face-to-face e tempos dependentes de produto e contexto ([FAQ](https://www.mmoexp.com/About/regular-questions-about-using.html), [delivery times](https://www.mmoexp.com/About/how-long-will-it-take-to-receive-my-order-after-payment.html)). **[A VALIDAR — GAME/PUBLISHER TERMS]** campos e métodos permitidos.

### BP-018 — Registrar tentativa, evidência e confirmação de entrega

**Classificação:** [FALTANTE — IMPORTANTE]

**Objetivo:** provar o que foi tentado e entregue, permitindo confirmação, suporte e disputa.

**Atores:** operador/supplier/seller, cliente e plataforma.

**Trigger:** início, sucesso, falha ou contestação de uma tentativa.

**Fluxo:** registrar tentativa, quantidade, horário, método, referência externa e evidência permitida; associar ao pedido/fulfillment; obter confirmação ou marcar como contestável.

**Evidência:** **[EVIDÊNCIA INTERNA]** não há delivery attempt, proof of delivery ou confirmação. **[EVIDÊNCIA EXTERNA]** a referência declara que cada etapa é registrada e que o handover é arquivado ([produto Steam](https://www.mmoexp.com/Account/Steam/169425005.html)).

### BP-019 — Tratar falha, timeout e fulfillment parcial

**Classificação:** [FALTANTE — IMPORTANTE]; **[A VALIDAR]** se quantidades parciais serão permitidas.

**Objetivo:** decidir o próximo passo quando a entrega não ocorre, demora ou ocorre apenas parcialmente.

**Atores:** operador, cliente, supplier/seller, suporte e plataforma.

**Trigger:** erro de entrega, timeout, falta de estoque, manutenção do game, informação incorreta ou entrega parcial.

**Decisões:** retry, reschedule, pedir ação do cliente, trocar source/método, completar saldo remanescente, cancelar, refund ou revisão manual.

**Evidência:** **[EVIDÊNCIA EXTERNA]** a política pública da MMOExp trata atraso por informação incorreta, falta de stock e refund; seus tempos também variam por produto ([refund policy](https://www.mmoexp.com/Refund.html), [delivery times](https://www.mmoexp.com/About/how-long-will-it-take-to-receive-my-order-after-payment.html)). Não se importam os prazos ou políticas da referência.

### BP-020 — Expirar e cancelar carrinho, pedido ou reserva

**Classificação:** [FALTANTE — IMPORTANTE]

**Objetivo:** encerrar com segurança fluxos que não prosseguem e liberar recursos.

**Atores:** cliente, plataforma e operador automático.

**Trigger:** prazo, abandono, falha de pagamento, solicitação do cliente, falta de supply ou timeout de fulfillment.

**Fluxo:** detectar condição; decidir elegibilidade; alterar estado; liberar reserva; cancelar tarefas; notificar; encaminhar refund quando o pagamento já ocorreu.

**Evidência:** **[EVIDÊNCIA INTERNA]** não há estados de expiração/cancelamento nem scheduler, job ou release de inventory.

### BP-021 — Solicitar, decidir e executar refund

**Classificação:** [FALTANTE — IMPORTANTE]

**Objetivo:** tratar devolução integral ou parcial após cancelamento, falha, disputa ou inelegibilidade.

**Atores:** cliente, suporte, operador financeiro, payment provider e plataforma.

**Trigger:** solicitação, falha de entrega, cancelamento, decisão de disputa ou chargeback.

**Fluxo:** receber solicitação; avaliar elegibilidade; aprovar/rejeitar; executar refund no gateway; atualizar pedido/payment; reconciliar; notificar.

**Evidência:** **[EVIDÊNCIA INTERNA]** não há refund. **[EVIDÊNCIA EXTERNA]** a política da MMOExp distingue refund antes da conclusão, falha em prazo, pedido concluído e correção de dados ([Refund Policy](https://www.mmoexp.com/Refund.html)). Prazos e critérios locais permanecem **[A VALIDAR]**.

### BP-022 — Tratar disputa e chargeback

**Classificação:** [FALTANTE — IMPORTANTE]

**Objetivo:** investigar reclamações e reversões iniciadas pelo cliente ou pelo provedor financeiro, preservando evidência e impacto financeiro.

**Atores:** cliente, suporte, risco, financeiro, payment provider e supplier/seller.

**Trigger:** dispute, chargeback, contestação de entrega ou alerta do gateway.

**Fluxo:** abrir caso; congelar decisões conflitantes; coletar order/payment/delivery evidence; decidir reject/retry/partial refund/full refund/escalation; responder ao gateway; reconciliar impacto e risco do cliente/source.

**Evidência:** **[EVIDÊNCIA INTERNA]** não há dispute, chargeback, evidência ou case management. **[A VALIDAR]** prazos, direitos, responsabilidade e política de risco.

### BP-023 — Operar suporte pré-venda, venda e pós-venda

**Classificação:** [FALTANTE — IMPORTANTE]

**Objetivo:** receber, classificar, atribuir, resolver e encerrar problemas de cliente ou operação.

**Atores:** cliente, agente de suporte, operador, financeiro e risco.

**Trigger:** pergunta, pedido em atraso, payment issue, delivery failure, refund, dispute ou account issue.

**Estados:** `OPEN → CLASSIFIED → ASSIGNED → WAITING/IN_PROGRESS → RESOLVED → CLOSED`, com reabertura quando necessário.

**Evidência:** **[EVIDÊNCIA INTERNA]** não há ticket, fila ou SLA documentado. **[EVIDÊNCIA EXTERNA]** a MMOExp expõe suporte pré, durante e pós-venda e atendimento contínuo ([Contact](https://www.mmoexp.com/Contact.html), [Help Center](https://www.mmoexp.com/About/help.html)). Canais e SLA locais são **[A VALIDAR]**.

### BP-024 — Comunicar eventos relevantes ao cliente e operador

**Classificação:** [FALTANTE — IMPORTANTE]

**Objetivo:** informar mudanças que exigem ação ou definem expectativa, sem confundir canal de comunicação com processo de negócio.

**Atores:** plataforma, cliente, operador, supplier/seller e suporte.

**Trigger:** criação/pagamento do pedido, ação necessária, início/falha/conclusão de entrega, refund, disputa ou expiração.

**Resultado:** mensagem entregue ou registrada como falha, com preferência/canal e deduplicação suficientes para não gerar instruções conflitantes.

**Evidência:** **[EVIDÊNCIA INTERNA]** não há eventos de domínio nem notificações de lifecycle; o webhook é apenas persistido.

### BP-025 — Reconciliar pagamentos, entregas, inventory e valores devidos

**Classificação:** [FALTANTE — IMPORTANTE]

**Objetivo:** comparar o esperado com o efetivamente confirmado e corrigir divergências.

**Atores:** financeiro, operador, plataforma, payment provider e supplier/seller se aplicável.

**Trigger:** fechamento periódico, callback, refund, chargeback, divergência manual ou falha de integração.

**Fluxos:** orders × gateway; orders × deliveries; inventory × deliveries; refunds × gateway; supplier activity × payable. Registrar diferença, investigar, ajustar e fechar o caso.

**Evidência:** **[EVIDÊNCIA INTERNA]** não há ledger, reconciliation, settlement ou payout. **[A VALIDAR]** se o modelo terá supplier/seller payable e qual nível contábil é necessário.

### BP-026 — Avaliar risco e executar revisão manual

**Classificação:** [FALTANTE — IMPORTANTE]; políticas **[A VALIDAR]**

**Objetivo:** decidir se uma transação pode prosseguir, aguardar verificação ou deve ser bloqueada/revertida.

**Atores:** risk operator, payment provider, suporte e plataforma.

**Trigger:** pedido de alto risco, padrão suspeito, inconsistência de conta, abuso de cupom/refund ou chargeback.

**Fluxo:** sinalizar; preservar contexto; revisar; decidir allow/hold/block; registrar motivo e evidências; liberar, cancelar ou escalar.

**Evidência:** **[EVIDÊNCIA INTERNA]** não há revisão manual ou política antifraude. **[EVIDÊNCIA EXTERNA]** a referência descreve payment risk control e verificação adicional para alguns pedidos ([produto Steam](https://www.mmoexp.com/Account/Steam/169425005.html), [FAQ](https://www.mmoexp.com/About/regular-questions-about-using.html)). Não se inferem thresholds ou regras específicas.

### BP-027 — Administrar regras por game/publisher e restrições

**Classificação:** [FALTANTE — IMPORTANTE]; **[A VALIDAR — GAME/PUBLISHER TERMS]**

**Objetivo:** manter as regras operacionais que tornam uma oferta tecnicamente e comercialmente permitida.

**Atores:** administrador, compliance, operador e publisher como fonte externa.

**Abrangência:** métodos de entrega, campos de account, quantidade, regiões, servidores, períodos de manutenção, restrições de conta e uso de dados. Regras devem ser configuráveis por contexto, não embutidas como universalidade.

**Evidência:** **[EVIDÊNCIA INTERNA]** não há configuração por game e o catálogo possui atributos genéricos. **[EVIDÊNCIA EXTERNA]** a MMOExp informa variação de método, produto e tempo por game ([FAQ](https://www.mmoexp.com/About/regular-questions-about-using.html)). Possibilidade técnica não equivale a permissão operacional.

### BP-028 — Operar fila de fulfillment e escalonamento

**Classificação:** [FALTANTE — IMPORTANTE]

**Objetivo:** coordenar trabalho humano quando a entrega não é totalmente automática.

**Atores:** operador, supervisor, suporte e supplier/seller.

**Trigger:** fulfillment criado, prioridade alterada, timeout, falha, cliente aguardando ou necessidade de evidência.

**Fluxo:** priorizar; atribuir; executar; registrar; reatribuir; escalar; encerrar. O resultado é responsabilidade operacional clara para cada fulfillment aberto.

**Evidência:** **[EVIDÊNCIA INTERNA]** não há work queue, assignment, scheduler ou operador documentado. **[EVIDÊNCIA EXTERNA]** a referência declara equipe global de serviço e delivery support ([About MMOEXP](https://www.mmoexp.com/About.html)).

### BP-029 — Coletar reviews e administrar reputação

**Classificação:** [FALTANTE — EVOLUTIVO]; **[A VALIDAR]**

**Objetivo:** obter feedback de transações elegíveis, moderar e usar sinais de qualidade.

**Atores:** cliente, plataforma, moderador e operador.

**Trigger:** pedido concluído, janela de avaliação ou denúncia.

**Fluxo:** determinar elegibilidade; receber; moderar; publicar/ocultar; responder; associar ao produto/serviço sem permitir abuso.

**Evidência:** **[EVIDÊNCIA INTERNA]** `Product.rating` existe como campo, mas não há processo de review. **[EVIDÊNCIA EXTERNA]** a MMOExp publica reviews verificadas e respostas da equipe ([Reviews](https://www.mmoexp.com/reviews.html)). O campo isolado não prova que esse processo seja requisito imediato.

## 20. Matriz de completude e rastreabilidade de gaps

| Macroprocesso | Processo esperado | Documentado antes | Cobertura após análise | Relevância | Ação |
|---|---|---:|---|---|---|
| Identidade | Provisionar conta | Sim | Adequada para o baseline | Essencial | Manter; expandir recuperação/perfil se necessário |
| Catálogo | Contexto de game e variante | Não | Ausente | Essencial | Adicionar BP-010 |
| Catálogo | Oferta e disponibilidade | Parcial | Parcial | Essencial | Expandir BP-003/BP-011 |
| Supply | Source, supplier/seller e aquisição | Não | Ausente | Essencial | Adicionar BP-012; validar modelo |
| Inventory | Reserva, allocation, consumo e release | Não | Ausente | Essencial | Adicionar BP-013 |
| Transaction | Pedido e lifecycle | Não | Ausente | Essencial | Adicionar BP-014 |
| Payment | Confirmação, idempotência e transição | Parcial | Parcial | Essencial | Expandir BP-007/BP-008/BP-015 |
| Fulfillment | Execução operacional | Não | Ausente | Essencial | Adicionar BP-016/BP-028 |
| Delivery | Instruções, tentativa, evidência e confirmação | Não | Ausente | Essencial/importante | Adicionar BP-017/BP-018 |
| Exceptions | Falha, timeout, parcial e cancelamento | Não | Ausente | Importante | Adicionar BP-019/BP-020 |
| After-sales | Refund, dispute e chargeback | Não | Ausente | Importante | Adicionar BP-021/BP-022 |
| Support | Case lifecycle | Não | Ausente | Importante | Adicionar BP-023 |
| Communication | Notificações de eventos | Não | Ausente | Importante | Adicionar BP-024 |
| Finance | Reconciliation e settlement | Não | Ausente | Importante | Adicionar BP-025 |
| Risk | Review e decisões de risco | Não | Ausente | Importante | Adicionar BP-026 |
| Rules | Regras por game/publisher | Não | Ausente | Importante | Adicionar BP-027 |
| Reputation | Reviews e moderação | Parcial (`rating`) | Ausente como processo | Evolutivo | Adicionar BP-029 se validado |

## 21. Lifecycle e eventos esperados

### 21.1 Pedido, pagamento e fulfillment

```text
Cart
  → Checkout validated
  → Order created
  → Payment pending
  → Payment confirmed / rejected / expired
  → Risk cleared / held / blocked
  → Supply reserved and allocated
  → Fulfillment assigned and started
  → Delivery attempted
  → Delivered / partially delivered / failed
  → Completed, refunded or disputed
```

### 21.2 Eventos que precisam de processo anterior e posterior

| Evento | Antes | Depois |
|---|---|---|
| `OrderCreated` | Checkout validado e preço congelado | Payment/risk |
| `PaymentConfirmed` | Webhook/consulta idempotente | Order paid, reserve e fulfillment |
| `InventoryReserved` | Disponibilidade e regra de reserva | Allocation/assignment |
| `FulfillmentStarted` | Pedido pago, risco liberado e source atribuído | Tentativa/evidência |
| `DeliveryFailed` | Tentativa registrada com motivo | Retry, ação do cliente, cancelamento ou refund |
| `DeliveryCompleted` | Evidência e quantidade confirmada | Completion, review, reconciliation |
| `RefundCompleted` | Elegibilidade e decisão | Order/payment update e reconciliação |
| `DisputeOpened` | Reclamação e contexto preservados | Investigação, decisão e comunicação |

**[GAP]** Nenhum desses eventos, exceto autenticação, possui produtor/consumidor de negócio demonstrado no baseline.

## 22. Business Process Gap Analysis

### Added

- **BP-010** contexto de game, plataforma, região e variante.
- **BP-012** supply e aquisição/register de supply.
- **BP-013** reservation, allocation e consumo de inventory.
- **BP-014** criação e lifecycle de pedido.
- **BP-016** fulfillment.
- **BP-017** instruções de entrega.
- **BP-018** evidência e confirmação.
- **BP-019** falha, timeout e parcial.
- **BP-020** expiração e cancelamento.
- **BP-021** refund.
- **BP-022** disputa e chargeback.
- **BP-023** suporte.
- **BP-024** comunicação de eventos.
- **BP-025** reconciliação.
- **BP-026** risco e revisão manual.
- **BP-027** regras por game/publisher.
- **BP-028** operação de fulfillment.
- **BP-029** reviews, como capacidade evolutiva.

### Expanded

- **BP-003/BP-002:** distinguir catálogo, produto e oferta; exigir contexto e disponibilidade antes de venda.
- **BP-006:** tratar preference como etapa de checkout, não como pedido ou confirmação de pagamento.
- **BP-007/BP-008:** separar persistência de Payment/Webhook do processo de confirmação, idempotência e avanço do pedido.
- **BP-009:** manter como integração de autorização, mas validar state, expiração, uso único, refresh e revogação.

### Validate

1. A plataforma é B2C, marketplace ou híbrida?
2. Existe inventory próprio, supplier, seller, aquisição on-demand ou combinação?
3. Quais games, plataformas, regiões, servidores, variantes e métodos de entrega serão suportados?
4. Fulfillment pode ser parcial? Há delivery windows e timeouts por produto?
5. Quais dados de jogador são necessários, por quanto tempo e quem pode acessá-los?
6. Quais critérios de risco, refund, dispute, chargeback, review e retenção são desejados?
7. Haverá settlement para supplier/seller ou somente reconciliação com o gateway?
8. Quais termos de publisher tornam uma oferta permitida, restrita ou proibida?

### Not Applicable to the current documented model

- **Seller onboarding, seller payout, seller commission e seller performance**: não devem ser tratados como existentes; só entram se o modelo for marketplace ou híbrido.
- **Supplier settlement**: depende de supplier externo e não pode ser assumido no modelo atual.
- **Reviews publicadas**: o campo `rating` é evidência insuficiente; tratar como evolutivo até decisão de produto.
- **Delivery method específico (mail, trade, auction house, redeem code, boosting ou top-up)**: não assumir como permitido; cada combinação requer **[A VALIDAR — GAME/PUBLISHER TERMS]**.

## 23. Relatório de alterações

**Arquivo atualizado:** `docs/business/business-processes.md`.

**Processos existentes analisados:** 9 (`BP-001` a `BP-009`).

**Processos adicionados à análise/documentação:** `BP-010`, `BP-012` a `BP-014` e `BP-016` a `BP-029`. `BP-011` e `BP-015` foram tratados como expansões de capacidades já parcialmente presentes.

**Novos macroprocessos reconhecidos:** contexto/catalog, supply, inventory, transaction/order, fulfillment/delivery, after-sales/support, risk, financial operations e reputation.

**Principais lacunas:** ausência de pedido como agregado comercial; confirmação de pagamento desconectada do webhook; ausência de reservation/allocation/consumption; ausência de fulfillment, instruções, evidência e falha de entrega; ausência de refund/dispute/support; ausência de reconciliation e decisões de risco; ambiguidade sobre B2C versus marketplace/híbrido.

**Fontes externas utilizadas:** [MMOExp About](https://www.mmoexp.com/About.html), [home/catalog](https://www.mmoexp.com/), [delivery times](https://www.mmoexp.com/About/how-long-will-it-take-to-receive-my-order-after-payment.html), [regular questions](https://www.mmoexp.com/About/regular-questions-about-using.html), [refund policy](https://www.mmoexp.com/Refund.html), [contact/support](https://www.mmoexp.com/Contact.html), [help center](https://www.mmoexp.com/About/help.html), [reviews](https://www.mmoexp.com/reviews.html) e [exemplo de fluxo de allocation/delivery](https://www.mmoexp.com/Account/Steam/169425005.html).
