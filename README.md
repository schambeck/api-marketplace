# api-marketplace [![CircleCI](https://dl.circleci.com/status-badge/img/gh/schambeck/api-marketplace/tree/main.svg?style=svg&circle-token=CCIPRJ_Hkfe6do7qVVvtfH6zH7rmv_f4cf760637f0db63efdeeb5a26614711330c928f)](https://dl.circleci.com/status-badge/redirect/gh/schambeck/api-marketplace/tree/main)

API REST de um marketplace, com catálogo de produtos, usuários, carrinho de compras, imagens, pagamentos via Mercado Pago, armazenamento de objetos e integração OAuth2/JWT.

## Visão do Negócio

Este backend sustenta a operação de um marketplace desde a autenticação do usuário até a preparação do checkout. Ele mantém as contas locais, permite que vendedores publiquem produtos e imagens, oferece um catálogo público para clientes, administra o carrinho de compras e cria preferências de pagamento no Mercado Pago.

Os principais atores são usuários autenticados — que podem atuar como clientes e vendedores —, visitantes do catálogo e o Mercado Pago, responsável pelo checkout externo e pela autorização da conta de pagamento. O fluxo principal é: o usuário autentica, consulta a oferta, adiciona produtos ao seu carrinho, vincula sua conta Mercado Pago quando necessário e solicita uma preferência de pagamento com os itens do carrinho. Webhooks e dados de pagamento podem ser recebidos e persistidos para consulta e auditoria.

O escopo atual termina na criação da preferência e no registro dos dados recebidos. Não há implementação de pedido concluído, reserva ou baixa de estoque, expedição, entrega, estorno ou fechamento financeiro. Para os processos detalhados e suas regras, consulte [`docs/business/business-processes.md`](docs/business/business-processes.md).

## Tecnologias

- Java 21 e Maven Wrapper;
- Spring Boot 3.4.1, Spring Web, Spring Data JPA e Spring Security Resource Server;
- PostgreSQL e Flyway;
- Keycloak para emissão e validação de tokens JWT;
- OpenFeign, MapStruct e Lombok;
- Google Cloud Storage e MinIO para armazenamento de objetos;
- Mercado Pago para pagamentos;
- Docker e Docker Compose;
- Springdoc OpenAPI/Swagger UI.

## Pré-requisitos

Para o desenvolvimento local, instale:

- JDK 21;
- Docker e Docker Compose, caso os serviços de infraestrutura sejam executados em contêineres;
- PostgreSQL acessível na porta `5432`, com banco `marketplace`;
- Git.

O projeto usa Java 21 no desenvolvimento local, no CI e na imagem Docker. O Maven 3.9.16 e Java 21 são usados para construir e executar a imagem.

## Configuração local

O perfil padrão usa HTTP na porta `8080` e espera o PostgreSQL em `localhost:5432`. Os valores locais padrão são:

```text
Banco: marketplace
Usuário: postgres
Senha: postgres
Keycloak: http://localhost:9000/realms/marketplace
```

Não coloque credenciais reais no repositório. Sobrescreva as propriedades por variáveis de ambiente ou argumentos de execução quando necessário. As principais propriedades configuráveis incluem:

| Variável | Finalidade |
| --- | --- |
| `SPRING_PROFILES_ACTIVE` | Seleciona o perfil Spring ativo. |
| `SERVER_PORT` | Porta HTTP/HTTPS da aplicação. |
| `SPRING_DATASOURCE_URL` | URL JDBC do PostgreSQL. |
| `SPRING_DATASOURCE_USERNAME` / `SPRING_DATASOURCE_PASSWORD` | Acesso ao banco. |
| `AUTH_URI` | Issuer do Keycloak usado para validar JWTs. |
| `CUSTOM_FRONTEND_URL` | URL principal do frontend. |
| `CUSTOM_CORS_ALLOWED_ORIGINS` | Origens permitidas pelo CORS. |
| `MERCADO_PAGO_REDIRECT_URI` | URL de retorno do fluxo OAuth do Mercado Pago. |
| `MERCADO_PAGO_ACCESS_TOKEN` | Token de acesso do Mercado Pago. |
| `OBJECT_STORAGE_GCS_PROJECT_ID` / `OBJECT_STORAGE_GCS_BUCKET_NAME` | Projeto e bucket do Google Cloud Storage. |
| `RUSTFS_ACCESS_KEY` / `RUSTFS_SECRET_KEY` / `RUSTFS_BUCKET` / `RUSTFS_REGION` | Configuração do RustFS/S3 no Compose. Durante a transição, `MINIO_ROOT_USER`, `MINIO_ROOT_PASSWORD` e `MINIO_BUCKET` são aceitos como fallback local. |

O arquivo `application.properties` contém os padrões locais. `application-dev.properties` e `application-tailscale.properties` habilitam HTTPS na porta `8443` e configuram endpoints externos específicos; use-os somente no ambiente correspondente.

## Banco, Keycloak e execução

Para iniciar somente o PostgreSQL definido no Compose:

```bash
make compose-up-postgres
```

Para iniciar PostgreSQL e Keycloak local:

```bash
make compose-up-postgres-keycloak
```

O Keycloak local importa o realm de `realm.json` e fica disponível na porta `9000`. A aplicação usa o realm `marketplace` por padrão.

Para baixar dependências, compilar e executar as verificações Maven:

```bash
./mvnw dependency:go-offline
make check
```

`make check` executa `./mvnw clean test -Dmaven.plugin.validation=BRIEF`. O projeto possui a dependência de testes do Spring Boot, mas não há testes automatizados versionados em `src/test` no estado atual.

Para gerar o artefato sem executar testes:

```bash
make dist
```

Para empacotar e executar o JAR com o perfil `tailscale`:

```bash
make dist-run
```

O comando `make run` executa o JAR existente em `target/` com esse mesmo perfil. Para interromper os serviços do Compose:

```bash
make compose-down
```

## Docker Compose

O `compose.yaml` define os serviços da aplicação (`web`), Nginx, PostgreSQL, Keycloak e RustFS. O serviço `web` usa a imagem `api-marketplace:latest` por padrão e pode ser sobrescrito com `WEB_IMAGE`.

Para subir os serviços padrão:

```bash
make compose-up
```

Os perfis opcionais do Compose são:

- `local`: Keycloak em HTTP na porta `9000`;
- `production`: Keycloak em HTTPS na porta `9443`;
- `tailscale`: Keycloak em HTTPS na porta `9443` com o hostname configurado no Compose.

O PostgreSQL é exposto na porta `5432`. O RustFS usa as portas `9010` (API) e `9011` (console). A persistência do PostgreSQL fica em `db/data` e a do RustFS no volume `rustfs_data`.

Antes de usar MinIO ou ambientes externos, forneça as variáveis e credenciais necessárias por um mecanismo seguro. Não commite esses valores.

## Documentação da API

Com a aplicação em execução, a documentação pode ser acessada em:

```text
http://localhost:8080/swagger-ui.html
http://localhost:8080/v3/api-docs
```

Em um perfil HTTPS, substitua o protocolo e a porta conforme a configuração ativa, normalmente `https://localhost:8443`.

Os principais grupos de endpoints são:

- `/catalog`: leitura pública do catálogo;
- `/products`, `/users`, `/carts`, `/cart-items` e `/shopping-cart`: recursos autenticados de marketplace;
- `/images` e `/object-storage`: imagens e objetos armazenados;
- `/mercado-pago/payments` e `/mercado-pago/preferences`: pagamentos e preferências;
- `/oauth` e `/oauth-tokens`: fluxos OAuth;
- `/webhooks`: recebimento de webhooks.

## Autenticação e rotas públicas

A aplicação funciona como um OAuth2 Resource Server e valida tokens JWT emitidos pelo Keycloak. O issuer pode ser alterado pela variável `AUTH_URI`.

São liberados sem autenticação:

- Swagger UI, recursos Swagger e `/v3/api-docs/**`;
- requisições `GET` para `/catalog/**`;
- requisições `GET` para `/oauth/**`;
- requisições `GET` para `/object-storage/**`;
- requisições `POST` para `/webhooks/**`.

As demais rotas exigem autenticação. O CORS é controlado por `custom.cors.allowed-origins`; novas origens devem ser avaliadas como mudança de segurança.

## Arquitetura

O ponto de entrada é `com.schambeck.marketplace.MarketplaceApplication`.

- `config`: segurança HTTP, CORS, Jackson e tratamento de exceções;
- `security`: integração com atributos do JWT e eventos de autenticação;
- `usecase`: controllers, serviços, repositories, entidades e mapeadores organizados por caso de uso;
- `objectstorage`: Google Cloud Storage, MinIO e upload/download de objetos;
- `webhook`: recebimento e persistência de webhooks;
- `exception` e `filter`: exceções de domínio e filtros de requisição.

Os controllers delegam regras aos serviços transacionais, que usam repositories Spring Data JPA para persistência. As representações HTTP são separadas das entidades persistidas por objetos `*Web` e mapeadores MapStruct quando aplicável.

## Migrations do banco

As migrations Flyway ficam em `src/main/resources/db/migration/` e são executadas automaticamente pela aplicação. A sequência atual cria:

1. usuários;
2. produtos;
3. carrinhos e itens de carrinho;
4. pagamentos;
5. imagens;
6. webhooks;
7. tabelas de autorização OAuth;
8. tokens OAuth.

Para alterações de schema, crie uma nova migration com o próximo número (`V11__descricao.sql`, por exemplo). Não edite migrations já aplicadas e mantenha entidades, repositories, mapeadores, contratos HTTP e `schema/api-schema-marketplace.json` sincronizados quando a mudança atravessar essas camadas.

## Desenvolvimento e validação

Ao alterar um fluxo:

1. revise o controller, serviço, repository, entidade/DTO e configuração relacionada;
2. preserve os contratos HTTP e as regras de autenticação existentes;
3. execute `make check`;
4. execute `make dist` quando a alteração afetar o artefato empacotável;
5. valide manualmente os endpoints afetados com PostgreSQL e Keycloak disponíveis;
6. confira `git diff` e `git status` antes de criar o commit.

Não adicione credenciais, tokens, certificados, dumps ou arquivos de ambiente ao Git.

## Encerrando o ambiente

Para parar e remover os contêineres criados pelo Compose:

```bash
make compose-down
```
# Configuração do onboarding de seller

A aprovação do seller atribui a role de realm `SELLER` por meio da API REST de administração do Keycloak. Configure estas variáveis de ambiente na API:

- `KEYCLOAK_ADMIN_BASE_URL`: URL base do Keycloak (por exemplo, `http://keycloak:8080` dentro do Compose).
- `KEYCLOAK_REALM`: nome do realm; o padrão é `marketplace`.
- `KEYCLOAK_ADMIN_CLIENT_ID` e `KEYCLOAK_ADMIN_CLIENT_SECRET`: cliente confidencial com conta de serviço que usa credenciais de cliente.

Habilite contas de serviço para esse cliente e conceda somente as permissões de gerenciamento do realm necessárias para consultar usuários, visualizar roles do realm e gerenciar as associações de roles dos usuários. Mantenha o segredo fora do controle de versão. Se a integração estiver indisponível, a aprovação não será concluída e a solicitação permanecerá pendente para que um administrador possa tentar novamente após restabelecer o acesso ao Keycloak. A importação das definições de realm fornecidas cria a role `SELLER`; ela só é atribuída após aprovação manual.
