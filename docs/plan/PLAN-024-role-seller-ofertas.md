# PLAN-024 — Role Seller e autorização de ofertas

## Objetivo

Permitir que usuários com role `SELLER` criem e gerenciem suas próprias offers, enquanto usuários autenticados sem essa role não podem executar operações de vendedor. Administradores mantêm acesso administrativo.

## Estado atual

- O backend lê roles do realm Keycloak e as converte em authorities `ROLE_*`.
- `POST /offers`, edição e transições de estado exigem apenas autenticação.
- `OfferService` já verifica que o produto pertence ao usuário autenticado, mas não exige role de seller.
- `GET /seller/offers` também exige apenas autenticação.
- As rotas frontend `/offer` e `/offer/:id` estão restritas a `ADMIN`, e a interface não possui fluxo de criação.
- O DTO frontend de atualização não corresponde ao payload recebido pelo backend.

## Decisão de produto pendente

Definir como um usuário recebe a role `SELLER`:

1. **Atribuição administrativa** — administrador aprova/atribui a role no Keycloak. É a opção mais simples e controlada para a primeira entrega.
2. **Autoatendimento** — usuário solicita ou ativa o perfil seller via onboarding; requer definir validação, revisão e prevenção de abuso.

Até essa decisão, a implementação deve assumir que `SELLER` é atribuída fora do fluxo de cadastro da conta e não concedê-la automaticamente a todo usuário.

## Escopo

### Keycloak e identidade

- Criar/configurar a role de realm `SELLER` nos arquivos de realm mantidos pelo projeto, sem alterar credenciais ou outros dados sensíveis.
- Confirmar que tokens emitidos incluem `SELLER` em `realm_access.roles` e que `SecurityConfig` produz `ROLE_SELLER`.
- Documentar o procedimento escolhido para atribuir a role aos usuários.

### Backend

- Restringir criação, edição de draft e transições de offer a `SELLER` ou `ADMIN`.
- Manter a checagem de propriedade: seller só opera offers associadas a produtos próprios.
- Restringir `/seller/offers` a `SELLER` ou `ADMIN`; sellers recebem somente suas próprias offers e admins conservam a visão administrativa pretendida.
- Manter endpoints/controllers administrativos sob `ADMIN`.
- Revisar comportamento de usuários com ambas as roles e garantir que `ADMIN` continue podendo atuar administrativamente.
- Acrescentar testes de autorização para usuário comum, seller proprietário, seller não proprietário e admin.

### Frontend

- Liberar a área de gerenciamento para `SELLER` e `ADMIN` usando a semântica OR do guard. O guard atual verifica todas as roles configuradas (AND), então deverá ser adaptado ou a rota deverá usar guards/configuração que expressem OR.
- Exibir navegação de offers para sellers e admins, ocultando ações que não se aplicam ao papel/status.
- Implementar criação de offer associada a um produto do seller autenticado.
- Corrigir DTOs de criação/atualização para refletir o contrato backend (`productId`, `game`, `platform`, `region`, `variant`, `price`, `deliveryMethod`, além de `reason` e `source` na atualização conforme requerido).
- Apresentar erros de validação e autorização de forma compreensível.

## Critérios de aceite

- Usuário autenticado sem `SELLER` nem `ADMIN` recebe `403` ao criar, editar, publicar, pausar, reativar ou retirar uma offer.
- Seller pode criar uma offer somente para produto próprio; criação inicia em `DRAFT`.
- Seller consegue consultar e administrar apenas as próprias offers.
- Seller não consegue ler/alterar offers de outro usuário mesmo conhecendo o identificador.
- Admin conserva o acesso administrativo previsto.
- Frontend permite que seller crie e gerencie offers próprias, sem expor a tela como exclusiva de admin.
- Payloads frontend/backend estão alinhados e o fluxo de criação pode ser concluído pela interface.

## Sequência sugerida

1. Resolver e documentar o processo de atribuição da role `SELLER`.
2. Configurar role e validar emissão/leitura no token Keycloak.
3. Aplicar autorização no backend e cobrir casos de propriedade e papel.
4. Ajustar guard, navegação e acesso às rotas no frontend.
5. Implementar formulário/fluxo de criação e alinhar os DTOs.
6. Validar cenários ponta a ponta com tokens de usuário comum, seller e admin.

## Fora de escopo

- Verificação legal, fiscal ou bancária do seller.
- Comissão, repasses e settlement.
- Workflow de aprovação de cada offer, além das transições já existentes.
- Construção de um portal completo de onboarding, caso seja escolhida atribuição administrativa na primeira entrega.
