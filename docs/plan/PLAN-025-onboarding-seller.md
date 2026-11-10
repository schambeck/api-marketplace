# PLAN-025 — Onboarding de seller

## Objetivo

Definir um fluxo pelo qual uma conta existente possa solicitar acesso de seller, ser avaliada e, quando aprovada, receber a role `SELLER` no Keycloak. Até a aprovação, a conta continua podendo usar as funções comuns de comprador, mas não pode criar nem operar offers.

Este plano complementa o [PLAN-024 — Role Seller e autorização de ofertas](PLAN-024-role-seller-ofertas.md), que implementa autorização e gerenciamento de offers.

## Estado atual

- A identidade e autenticação são fornecidas pelo Keycloak; o backend converte roles de realm no token em authorities `ROLE_*`.
- O backend mantém um registro local `User` associado ao identificador externo do provedor.
- Não há entidade nem endpoints de solicitação/aprovação de seller.
- O fluxo administrativo atual de usuários não deve ser tratado como onboarding: não há ciclo de revisão ou estado de seller.

## Princípios do fluxo

- Cadastro de conta e solicitação para vender são etapas distintas.
- A role `SELLER` só é atribuída após decisão explícita de aprovação.
- O estado de onboarding deve ser mantido na aplicação; o Keycloak é a fonte de autorização efetiva, não o histórico do processo.
- A API deve derivar a identidade do solicitante do token, nunca aceitar username, user id ou role enviados pelo cliente como autoridade.
- Aprovação, rejeição, suspensão e reenvio devem ser auditáveis.
- Usuário pode continuar comprando enquanto a solicitação está em análise ou foi rejeitada.

## Fluxo proposto

1. Usuário autenticado abre “Quero vender” e envia os dados mínimos de perfil e operação.
2. Backend cria uma solicitação `PENDING`, vinculada ao usuário autenticado, e registra data e versão dos termos aceitos.
3. Usuário vê confirmação e estado da solicitação; pode corrigir ou complementar dados conforme a política definida, sem criar duplicatas pendentes.
4. Administrador consulta a fila e os detalhes necessários, então aprova ou rejeita com justificativa.
5. Na aprovação, backend atribui `SELLER` no Keycloak usando credencial de serviço com privilégio mínimo e registra ator, horário e resultado. Só considerar a solicitação aprovada depois de confirmar a atribuição da role.
6. Frontend atualiza o estado de autorização/token e libera o painel de offers. O seller cadastra um produto quando necessário e cria a offer em `DRAFT`.
7. Em caso de suspensão ou revogação, a role é removida no Keycloak; a conta permanece ativa como comprador e as offers são tratadas pela política operacional definida abaixo.

## Dados da solicitação

Proposta inicial para MVP, sujeita à política de negócio:

- identificador e usuário solicitante;
- estado: `PENDING`, `APPROVED`, `REJECTED`, `SUSPENDED` ou `WITHDRAWN`;
- nome público/da loja e descrição breve;
- email de contato, preferencialmente confirmado pelo provedor de identidade;
- tipo de itens e método de entrega pretendido;
- aceite versionado dos termos para sellers e política de conteúdo;
- justificativa de decisão, autor e timestamps;
- timestamps de criação/atualização e histórico de transições.

Não coletar documentos fiscais, dados bancários ou documentos de identidade no MVP sem requisito legal/operacional explícito, política de retenção e proteção adequada. Dados de pagamento devem seguir o fluxo financeiro próprio.

## Estados e transições

| Estado atual | Ação | Estado seguinte | Ator |
|---|---|---|---|
| — | Enviar solicitação válida | `PENDING` | Usuário autenticado |
| `PENDING` | Aprovar e atribuir role com sucesso | `APPROVED` | Admin |
| `PENDING` | Rejeitar com justificativa | `REJECTED` | Admin |
| `PENDING` | Retirar solicitação | `WITHDRAWN` | Solicitante |
| `REJECTED` | Reenviar após correções | `PENDING` (nova versão/histórico) | Solicitante |
| `APPROVED` | Suspender e revogar role | `SUSPENDED` | Admin |
| `SUSPENDED` | Reativar e restaurar role | `APPROVED` | Admin |

Falha de comunicação com Keycloak durante aprovação não pode deixar estado `APPROVED` sem a role. Registrar falha operacional e permitir repetição idempotente da operação. Se a aplicação confirmar a atribuição, mas falhar antes de persistir o estado, a repetição deve detectar a role já presente e concluir sem duplicar efeitos.

## Escopo de implementação

### Identidade e Keycloak

- Definir a role de realm `SELLER` e configurar os clientes/escopos para incluí-la em `realm_access.roles`.
- Criar integração de administração do Keycloak usando service account, segredo fornecido por ambiente e permissões mínimas para atribuir/remover apenas a role necessária.
- Nunca expor credenciais administrativas do Keycloak ao frontend nem registrar tokens/segredos.
- Determinar como solicitar renovação de token ou nova sessão após aprovação/revogação; a UI deve orientar o usuário se novo login for necessário.

### Backend

- Persistir solicitação e histórico em tabelas próprias via migration Flyway.
- Implementar endpoints do usuário autenticado para criar, consultar estado, atualizar enquanto pendente e retirar solicitação.
- Implementar endpoints administrativos para listar, consultar, aprovar, rejeitar, suspender e reativar solicitações.
- Garantir que o solicitante só leia/edite sua própria solicitação e que decisões exijam `ADMIN`.
- Validar campos, aceite de termos e transições no serviço; aplicar idempotência às ações de provisionamento da role.
- Manter sincronização consistente entre estado persistido e role Keycloak, com retry/observabilidade para falhas externas.
- Não permitir que endpoints genéricos de usuário alterem roles ou estados de seller.

### Frontend

- Adicionar entrada “Quero vender” para usuário autenticado sem `SELLER`.
- Criar formulário de solicitação, estado de confirmação, acompanhamento e correção/reenvio quando aplicável.
- Criar área administrativa para fila e decisão, exibindo somente dados necessários à análise.
- Exibir estado e justificativa ao solicitante; após aprovação, orientar atualização da sessão e encaminhar ao painel de offers.
- Usar roles apenas para experiência de navegação; toda autorização deve continuar aplicada no backend.

### Operação e comunicação

- Definir responsável pela fila, prazo esperado de análise, conteúdo de rejeição e canal de suporte.
- Registrar eventos sem incluir dados pessoais excessivos.
- Definir procedimento para falha de provisionamento, reconciliação de roles e recuperação após indisponibilidade do Keycloak.

## Critérios de aceite

- Usuário autenticado pode solicitar perfil de seller sem receber a role antes da aprovação.
- Solicitações duplicadas em estado pendente são impedidas ou consolidadas.
- Solicitante consulta apenas seus próprios dados e não consegue aprovar a própria solicitação.
- Apenas admin pode aprovar/rejeitar/suspender/reativar.
- Aprovação só termina em `APPROVED` quando `SELLER` está atribuído; repetição após falha parcial é segura.
- Usuário sem `SELLER` não cria/gerencia offers, mesmo chamando diretamente a API.
- Após aprovação e renovação de autenticação, seller acessa somente seus produtos e offers conforme PLAN-024.
- Rejeição e suspensão preservam histórico e justificativa; a conta continua podendo comprar.
- Tokens, credenciais de serviço e dados sensíveis não aparecem em respostas ou logs.

## Decisões de produto necessárias antes de congelar o contrato

1. Quais dados são realmente necessários para aprovar um seller e quais podem ser adiados?
2. O onboarding começa como aprovação manual para todos ou há aprovação automática para algum perfil?
3. Quais termos e políticas devem ser aceitos e como sua versão é mantida?
4. Em suspensão, offers publicadas são pausadas automaticamente ou apenas bloqueadas para novas compras?
5. Qual prazo de análise e canal de suporte serão informados ao usuário?

Recomendação para o primeiro MVP: aprovação manual, coleta mínima de dados, sem documentos fiscais/bancários, decisão auditada e sem automatizar a pausa das offers até haver política operacional definida.

## Fora de escopo

- KYC, validação fiscal e coleta de documentos.
- Pagamentos, repasses, comissão e settlement.
- Automação de análise antifraude.
- Migração de identidade para fora do Keycloak.
