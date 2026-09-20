# Plano: migração para Java 21

## Estado atual e objetivo

- O Dockerfile já usa Java 21.
- O `pom.xml`, a documentação e o workflow do GitHub Actions ainda indicam Java 17.
- Objetivo: tornar Java 21 a versão oficial de compilação, CI, desenvolvimento local e documentação, sem alterar o comportamento da API ou atualizar dependências fora do escopo.

## Abordagem e arquivos afetados

1. Alterar a propriedade `java.version` e a configuração explícita do Maven Compiler Plugin para 21 em `pom.xml`.
2. Atualizar o workflow `.github/workflows/maven.yml` para configurar JDK 21.
3. Atualizar as referências operacionais em `README.md` e `AGENTS.md`.
4. Verificar referências residuais a Java 17, compilar/testar e empacotar o projeto.

## Critérios de aceitação e evidência

| Critério | Evidência |
| --- | --- |
| Maven compila o código para Java 21 | `./mvnw clean test -Dmaven.plugin.validation=BRIEF` |
| O artefato empacota com Java 21 | `./mvnw clean package` |
| CI usa JDK 21 | inspeção de `.github/workflows/maven.yml` |
| Documentação não orienta Java 17 para este projeto | busca por referências em `README.md` e `AGENTS.md` |
| Não há mudança funcional ou segredo incluído | revisão de `git diff` e `git status` |

## Riscos e mitigação

- Annotation processors e dependências podem revelar incompatibilidades no JDK 21; a mitigação é executar o ciclo Maven completo.
- O ambiente local pode não ter JDK 21; nesse caso, registrar a limitação e validar com o máximo possível sem alterar o escopo.
- As referências a PostgreSQL 17.3 não são referências ao JDK e devem permanecer inalteradas.

## Rollback

Reverter somente as alterações de versão no `pom.xml`, workflow e documentação, sem editar migrations ou código de negócio.

## Resultado da validação

- JDK local: Temurin 21.0.11.
- `mvn clean test -Dmaven.plugin.validation=BRIEF`: aprovado; 8 testes, 0 falhas, 0 erros.
- `mvn clean package -Dmaven.plugin.validation=BRIEF -DskipTests=false`: aprovado; JAR Spring Boot gerado em `target/api-marketplace-1.0.0.jar`.
- Bytecode verificado com major version 65 (Java 21).
- `./mvnw` não pôde ser usado porque o repositório não contém `.mvn/wrapper/maven-wrapper.jar`; a validação equivalente foi executada com Maven 3.9.16 instalado no ambiente.
