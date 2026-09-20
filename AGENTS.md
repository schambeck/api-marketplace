# Instruções para agentes

## Visão geral

`api-marketplace` é uma API REST em Java 17 baseada em Spring Boot 3.4.1. O serviço usa Spring Web, Spring Data JPA/Hibernate, Spring Security como resource server OAuth2/JWT, OpenFeign, Flyway e PostgreSQL. Há integrações com Keycloak, Mercado Pago, Google Cloud Storage e MinIO.

O ponto de entrada é `com.schambeck.marketplace.MarketplaceApplication`. O README atual só identifica o projeto e não contém um procedimento adicional de operação; para comandos e ambientes, use o `Makefile`, o `pom.xml`, os arquivos `application*.properties` e o `compose.yaml` como fonte de verdade.

## Estrutura do projeto

- `src/main/java/com/schambeck/marketplace/`: código da aplicação.
  - `config/`: segurança HTTP, CORS, Jackson e tratamento de exceções.
  - `security/`: leitura de atributos do JWT e eventos de autenticação.
  - `usecase/`: módulos de negócio organizados por caso de uso (`user`, `product`, `image`, `cart`, `cartitem`, `shoppingcart`, OAuth e Mercado Pago).
  - `objectstorage/`: Google Cloud Storage, MinIO, upload/download e cliente Feign do Mercado Pago.
  - `webhook/`: recebimento e persistência de webhooks.
  - `exception/` e `filter/`: exceções de domínio e logging de requisições.
- `src/main/resources/db/migration/`: migrations Flyway versionadas (`V1__...` a `V10__...`).
- `src/main/resources/application.properties`: configuração HTTP/local padrão; `application-dev.properties` e `application-tailscale.properties` definem variantes com HTTPS e endpoints externos.
- `schema/api-schema-marketplace.json`: descrição de entidades/recursos usada como referência do modelo.
- `realm*.json`: configurações/importações do realm do Keycloak.
- `compose.yaml`: serviços de aplicação, Nginx, PostgreSQL, Keycloak e MinIO, com perfis `local`, `production` e `tailscale` para Keycloak.
- `Dockerfile` e `Dockerfile.kc`: imagens da API e do Keycloak.
- `.github/workflows/maven.yml`: CI para push/PR em `main`.

Não há código de testes em `src/test` no estado atual do repositório.

## Configuração do ambiente

Pré-requisitos observados no projeto:

- JDK 17 para desenvolvimento e CI. O Dockerfile usa runtime/build Java 21, portanto valide mudanças de imagem separadamente do fluxo local/CI.
- Maven Wrapper (`./mvnw`); a versão configurada no wrapper é Maven 3.9.2.
- Docker e Docker Compose para PostgreSQL, Keycloak, MinIO e execução containerizada.
- PostgreSQL acessível em `localhost:5432`, banco `marketplace`, ou os valores equivalentes fornecidos por variáveis.

O Compose fornece um PostgreSQL 17.3-alpine com valores locais padrão. Para iniciar apenas o banco, use:

```bash
make compose-up-postgres
```

Para iniciar banco e Keycloak local:

```bash
make compose-up-postgres-keycloak
```

O realm local é importado de `realm.json` e o Keycloak local expõe a porta `9000`. O serviço espera o issuer padrão `http://localhost:9000/realms/marketplace`, salvo sobrescrita por `AUTH_URI`.

Os arquivos `.env`, `.env.dev` e `.env.tailscale` fornecem conjuntos de variáveis para o Compose, com nomes como `SPRING_PROFILES_ACTIVE`, `SPRING_DATASOURCE_*`, `AUTH_URI`, `CUSTOM_FRONTEND_URL`, `CUSTOM_CORS_ALLOWED_ORIGINS`, `MERCADO_PAGO_REDIRECT_URI`, `OBJECT_STORAGE_GCS_*` e `MINIO_*`. Não copie valores desses arquivos para novos commits; passe credenciais por ambiente seguro.

## Comandos essenciais

Dependências Maven são resolvidas pelo wrapper durante a execução. Para pré-baixá-las:

```bash
./mvnw dependency:go-offline
```

Comandos definidos no `Makefile`:

```bash
make clean                 # ./mvnw clean
make check                 # ./mvnw clean test -Dmaven.plugin.validation=BRIEF
make dist                  # ./mvnw clean package -DskipTests=true -Dmaven.plugin.validation=BRIEF
make dist-run              # empacota e executa o JAR no perfil tailscale
make run                   # executa target/api-*.jar com perfil tailscale
make compose-down
make compose-up
```

Equivalentes úteis do Maven:

```bash
./mvnw clean test -Dmaven.plugin.validation=BRIEF
./mvnw clean package
java -jar target/api-*.jar --spring.profiles.active=tailscale
```

`make compose-up` sobe os serviços selecionados pelo Compose em background; a imagem `web` usa `api-marketplace:latest` por padrão. Para executar o código localmente, empacote com Maven e use a configuração de banco/Keycloak correspondente.

## Desenvolvimento

O perfil sem sufixo usa HTTP na porta `8080`, PostgreSQL em `localhost:5432` e Keycloak local em `localhost:9000`. Os perfis `dev` e `tailscale` usam HTTPS na porta `8443`, keystores versionados e URLs externas específicas. Se precisar de outra configuração, prefira sobrescrever propriedades Spring por variáveis de ambiente ou argumentos de execução, sem alterar os valores destinados a um ambiente existente.

A API expõe Swagger/OpenAPI pelos caminhos liberados em `SecurityConfig` (`/swagger-ui.html`, `/swagger-ui/**` e `/v3/api-docs/**`). A configuração de CORS vem de `custom.cors.allowed-origins` e permite credenciais; qualquer novo origin deve ser revisado como mudança de segurança.

Ao implementar um caso de uso, mantenha a organização existente: controller REST, serviço transacional, repository Spring Data, entidade JPA e objetos `*Web`/mapper quando aplicável. Os serviços normalmente usam `@Transactional(readOnly = true)` na classe e `@Transactional` nos métodos mutáveis; repositórios encapsulam consultas derivadas e alguns `findByIdOrThrow`.

## Testes

Não há testes automatizados versionados atualmente. `make check` ainda deve ser executado porque chama o ciclo Maven de testes e detecta falhas de compilação/configuração, mas uma execução verde não substitui testes de comportamento.

Quando alterar um fluxo, valide manualmente os endpoints afetados com banco e autenticação disponíveis, incluindo autorização JWT para rotas protegidas e os endpoints explicitamente públicos em `SecurityConfig`. Para mudanças em pagamentos, webhooks ou armazenamento, use credenciais e endpoints de teste apropriados e não dados reais.

## Qualidade e formatação

O projeto não configura Checkstyle, Spotless, PMD, formatter Maven ou outro lint. Não invente uma etapa de lint no CI. Preserve o estilo observado: classes Java em `com.schambeck.marketplace`, indentação predominantemente de quatro espaços (com alguns arquivos legados usando tabs), imports explícitos quando possível, Lombok para construtores/getters/setters e MapStruct para conversão entre entidades e DTOs `*Web`.

Antes de finalizar uma alteração Java, execute ao menos:

```bash
./mvnw clean test -Dmaven.plugin.validation=BRIEF
```

Para validar o artefato empacotável, execute também `./mvnw clean package` ou `make dist`. O workflow de CI usa `mvn -B package --file pom.xml` em JDK 17.

## Convenções de código

- Use nomes de pacotes e classes coerentes com os casos de uso existentes; controllers usam `@RequestMapping` no plural (`products`, `users`, `cart-items`, etc.).
- Mantenha a separação entre entidade persistida (`Product`, `User`, etc.) e representação HTTP (`ProductWeb`, `UserWeb`, etc.). Atualizações normalmente copiam campos para a entidade carregada via mapper antes de salvar.
- Preserve `@Version`, `created_at`/`modified_at` e o `@EntityListeners(AuditingEntityListener.class)` nas entidades que seguem o padrão atual.
- Use `Pageable`/`Page` para listagens paginadas e preserve a ordenação explícita por `id` nas consultas existentes, salvo decisão de contrato documentada.
- Reaproveite `AuthenticationService` para obter `preferred_username`, `email` ou `name` do `JwtAuthenticationToken`; não extraia claims de forma divergente sem necessidade.
- Novos mappers devem seguir o padrão MapStruct (`@Mapper`, `INSTANCE`, `toWeb`, `toEntity` e `copy` quando necessário).

## Arquitetura e dependências

Controllers recebem requisições e delegam a serviços; serviços coordenam regras e transações; repositories acessam JPA/PostgreSQL. A aplicação habilita JPA auditing e OpenFeign em `MarketplaceApplication`.

Dependências externas relevantes no `pom.xml`: Spring Boot Web/Data JPA/Security, Spring Cloud OpenFeign, Flyway, PostgreSQL, Lombok, MapStruct, Google Cloud Storage, MinIO, SDK Java do Mercado Pago e Springdoc OpenAPI. Alterações de versões devem considerar a combinação Spring Boot 3.4.1, Spring Cloud 2024.0.0, Java 17 e os annotation processors de Lombok/MapStruct.

## Banco de dados e migrations

Flyway executa as migrations em `src/main/resources/db/migration/` e possui tentativas de conexão configuradas. O schema contém tabelas de usuários, produtos, carrinho/itens, pagamentos, imagens, webhooks e armazenamento do fluxo OAuth. Algumas migrations também inserem dados iniciais de usuários, produtos e imagens.

Para alterar o banco:

1. Adicione uma nova migration com o próximo número `Vn__descricao.sql`; não edite uma migration já aplicada.
2. Preserve nomes de tabelas/colunas existentes, chaves estrangeiras, índices únicos e a coluna `version` usada por optimistic locking.
3. Atualize a entidade, o repository, o mapper/DTO e `schema/api-schema-marketplace.json` quando a mudança atravessar essas camadas.
4. Suba um PostgreSQL limpo ou controlado e execute `make check`/`./mvnw clean package`, verificando nos logs a aplicação da migration.

Não use `spring.jpa.hibernate.ddl-auto` como substituto: o repositório não o configura e o versionamento é feito por Flyway. Migrations destrutivas ou que alterem dados iniciais exigem revisão explícita e teste em cópia do banco.

## APIs e contratos

Os contratos HTTP são implementados nos controllers e os campos públicos ficam nos objetos `*Web`; o schema JSON também descreve recursos e campos. Rotas de leitura do catálogo, OAuth, download de objetos e documentação, além de `POST /webhooks/**`, são liberadas conforme `SecurityConfig`; as demais exigem autenticação JWT.

Ao mudar endpoint, método HTTP, payload, paginação, status ou nomes de campos, revise controller, `*Web`, mapper, `schema/api-schema-marketplace.json`, configurações do frontend/origens e integrações consumidoras. Compatibilidade deve ser preferida; não reutilize um caminho existente para semântica incompatível sem migrar os consumidores.

## Segurança e variáveis de ambiente

Há arquivos versionados que contêm ou podem conter credenciais, tokens, certificados, keystores e material de chave, incluindo `.env*`, `src/main/resources/credentials.json`, keystores/certificados em `certbot*`, `zerossl` e `src/main/resources`. As propriedades também exibem credenciais de integrações. Trate esses valores como comprometidos: não os copie, não os imprima em logs e não adicione novos segredos ao Git. Antes de qualquer trabalho de segurança, confirme rotação/revogação com o responsável pelo ambiente.

Tenha cuidado especial com:

- `SecurityConfig`: desabilita CSRF, define rotas públicas e exige JWT no restante.
- `CorsConfig` e `custom.cors.allowed-origins`: uma origem indevida pode ampliar acesso autenticado.
- `application*.properties`, `.env*`, `realm*.json`, `credentials.json` e certificados/keystores: podem conter endpoints, identidades ou segredos de ambiente.
- `compose.yaml`, `Dockerfile*` e `nginx/conf.d/default.conf`: alterá-los pode mudar portas, proxy, TLS, importação do realm e persistência.
- `ObjectStorageController` e integrações Mercado Pago/Google/MinIO: valide exposição de arquivos, callbacks, tokens e tratamento de erros.

## Fluxo de contribuição

O repositório está na branch `main`, com `origin/main`; o CI é acionado em push e pull request para `main`. O histórico usa mensagens curtas, por exemplo `Dev env`, `SSL` e `Mercado Pago Webhook`; não há política de commits mais detalhada registrada.

Mantenha cada alteração focada, não reformate arquivos inteiros sem necessidade e não inclua certificados, dumps, tokens ou credenciais. Descreva no commit/PR as migrations, mudanças de contrato, variáveis novas e passos manuais necessários.

## Checklist para alterações

- [ ] Li o controller, serviço, repository, entidade/DTO e configuração relacionada antes de alterar o fluxo.
- [ ] Mantive compatibilidade de rotas, payloads e autenticação, ou atualizei os contratos correspondentes.
- [ ] Para banco, criei migration Flyway nova e revisei chaves, índices, `version` e dados iniciais.
- [ ] Não adicionei nem expus credenciais, tokens, certificados ou arquivos de ambiente.
- [ ] Executei `./mvnw clean test -Dmaven.plugin.validation=BRIEF`.
- [ ] Executei `./mvnw clean package`/`make dist` quando a mudança afeta o artefato ou a imagem.
- [ ] Validei manualmente endpoints e integrações afetados quando o ambiente local permitiu.
- [ ] Conferi `git diff`, `git status` e arquivos gerados antes de finalizar.
