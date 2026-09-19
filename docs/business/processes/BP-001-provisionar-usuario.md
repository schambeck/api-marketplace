# BP-001 — Provisionar usuário no primeiro login

**Situação:** Implementado

**Objetivo:** criar a conta local necessária para relacionar produtos, carrinho e credenciais ao usuário autenticado.

**Gatilho:** evento `AuthenticationSuccessEvent` após autenticação JWT.

**Fluxo:** o sistema extrai username, e-mail, nome e identificador externo; verifica se já existe username; se não existir, grava `User` com esses dados (`AuthenticationEvents.java:19-33`). Se já existir, não atualiza automaticamente o cadastro.

**Regras e decisões:** username é a chave de existência usada pelo listener; o identificador externo vem de `authentication.getName()`; criação duplicada é evitada apenas pela verificação de existência, apoiada pelo índice único de username (`V1__create_user.sql`).

**Estado alterado:** `User` passa de inexistente para persistido. Não há estados de bloqueio, ativação ou desativação.

**Resultado:** conta local disponível para os demais processos. Falhas de claim obrigatório podem interromper a criação (`AuthenticationService.java:25-31`).

**Evidência:** `AuthenticationEvents.java`, `AuthenticationService.java` e migration `V1__create_user.sql`.
