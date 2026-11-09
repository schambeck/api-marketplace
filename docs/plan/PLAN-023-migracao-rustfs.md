# PLAN-023 — Migração de MinIO para RustFS

**Tipo:** migração de infraestrutura de object storage.

**Prioridade:** importante para desenvolvimento local; condicional para produção.

**Status:** implementação inicial concluída; validação de runtime, migração de objetos existentes e decisão de produção permanecem pendentes.

**Dependências:** configuração atual de object storage, contratos de `/object-storage`, validação S3 e decisão sobre o backend de produção.

## 1. Objetivo

Substituir o MinIO Community Edition usado no ambiente local por RustFS, mantendo o contrato HTTP do backend e a compatibilidade S3 da aplicação. A migração deve remover a dependência operacional de imagens MinIO indisponíveis, sem alterar o fluxo de upload/download consumido pelo frontend.

## 2. Escopo

- executar RustFS via Docker Compose;
- manter o bucket e as chaves de objetos existentes;
- configurar endpoint, credenciais, região e acesso path-style por ambiente;
- preservar `POST /object-storage/upload-file` e `GET /object-storage/download/{objectId}`;
- validar upload, download, persistência e reinício;
- migrar objetos existentes pela API S3, nunca copiando o volume interno diretamente;
- documentar rollback para MinIO, GCS ou outro backend S3 compatível;
- substituir nomes e configurações específicas de MinIO por uma configuração genérica de S3.

## 3. Fora de escopo

- migração de GCS para RustFS em produção sem decisão operacional explícita;
- alteração do contrato público do frontend;
- migração de dados do PostgreSQL;
- uso de APIs administrativas específicas do MinIO;
- cópia direta do volume interno do MinIO para o volume do RustFS;
- remoção imediata da implementação GCS;
- alteração de políticas de autorização dos endpoints de imagem.

## 4. Estado atual

O backend possui implementações separadas para GCS e MinIO, com `MinioStorageService` usando o SDK Java do MinIO. O frontend acessa o backend por `/object-storage`, portanto não deve conhecer o provedor real.

O Compose atual define `minio-server`, `minio-client` e o volume `minio_data`. As imagens oficiais usadas por essa configuração podem não estar mais disponíveis nos registries públicos.

Há credenciais de object storage em arquivos de ambiente e propriedades locais. Elas devem ser tratadas como comprometidas, rotacionadas e removidas de arquivos versionados sempre que forem credenciais reais.

## 5. Arquitetura alvo

O código deve depender de uma configuração genérica:

```properties
object-storage.s3.enabled=true
object-storage.s3.endpoint=http://rustfs:9000
object-storage.s3.access-key=${RUSTFS_ACCESS_KEY}
object-storage.s3.secret-key=${RUSTFS_SECRET_KEY}
object-storage.s3.bucket-name=${RUSTFS_BUCKET:marketplace}
object-storage.s3.region=${RUSTFS_REGION:us-east-1}
object-storage.s3.path-style-access=true
object-storage.s3.secure=false
```

O SDK atual pode ser mantido inicialmente, desde que use apenas operações S3 compatíveis. A implementação deve evitar dependência de `mc` e de APIs administrativas específicas do MinIO.

## 6. Sequência de implementação

1. Inventariar buckets, objetos, tamanhos, chaves e URLs atualmente persistidas.
2. Rotacionar credenciais expostas e mover segredos para o mecanismo seguro de cada ambiente.
3. Criar `S3StorageConfigProperties` e uma configuração de endpoint S3 genérica.
4. Refatorar `MinioStorageService` para uma implementação sem nome de provedor, preservando `StorageService`.
5. Atualizar propriedades `.env`, `application*.properties` e documentação sem commitar segredos.
6. Substituir os serviços MinIO do Compose por RustFS e um comando idempotente de criação/seed do bucket.
7. Adicionar healthcheck e espera explícita pelo endpoint S3 antes do seed.
8. Executar upload/download contra RustFS com bucket vazio.
9. Migrar dados existentes pela API S3, comparando chaves, tamanhos e amostras de conteúdo.
10. Validar o frontend sem alterar suas URLs `/object-storage`.
11. Testar reinício, volume persistente, credenciais inválidas e falhas de storage.
12. Habilitar RustFS em dev e tailscale; manter o backend anterior como fallback temporário.
13. Decidir separadamente se RustFS será promovido para produção ou se GCS continuará como backend produtivo.

## 7. Compose inicial

O serviço RustFS deve usar imagem versionada, não `latest`, e volume dedicado. O diretório montado precisa ser gravável pelo usuário não-root utilizado pela imagem.

```yaml
rustfs:
  image: rustfs/rustfs:<versão-validada>
  restart: always
  command: server /data
  environment:
    RUSTFS_ACCESS_KEY: ${RUSTFS_ACCESS_KEY}
    RUSTFS_SECRET_KEY: ${RUSTFS_SECRET_KEY}
  volumes:
    - rustfs_data:/data
  ports:
    - '9010:9000'
    - '9011:9001'
```

O cliente de inicialização pode usar AWS CLI, `rclone` ou o cliente RustFS. A criação do bucket deve tolerar bucket já existente e não deve copiar `./minio` por glob sem verificar se existem arquivos.

## 8. Migração de dados

1. Colocar uploads em modo controlado durante a janela de migração.
2. Criar o bucket de destino.
3. Copiar objetos usando S3, preservando as chaves.
4. Comparar listagem, quantidade e tamanho dos objetos.
5. Validar downloads por amostragem e imagens referenciadas por produtos.
6. Alterar o endpoint do backend para RustFS.
7. Repetir uma rodada de upload e download.
8. Manter a origem em modo somente leitura até o período de observação terminar.

Não assumir compatibilidade entre formatos físicos de volumes MinIO e RustFS.

## 9. Testes e critérios de aceite

- aplicação inicia com RustFS habilitado;
- bucket é criado automaticamente quando ausente;
- upload cria o objeto e retorna o mesmo contrato atual;
- download encontra objetos existentes;
- download inexistente retorna erro controlado;
- objetos sobrevivem ao reinício do container;
- frontend continua exibindo imagens do catálogo;
- chaves com extensão, Unicode e caracteres especiais funcionam;
- arquivo grande e multipart são validados;
- credenciais inválidas falham sem expor segredo nos logs;
- GCS continua funcionando quando selecionado;
- nenhuma chamada depende de `mc` ou de API administrativa do MinIO;
- rollback para o backend anterior é executável sem migration de banco.

Validação mínima:

```bash
docker compose config
docker compose up -d rustfs
./mvnw clean test -Dmaven.plugin.validation=BRIEF
```

Se o Maven Wrapper ou o registry não estiverem disponíveis, registrar a limitação e executar os testes contra um RustFS já disponível no ambiente.

## 10. Rollback

O backend deve permitir selecionar o provedor por configuração. Em caso de falha:

1. bloquear temporariamente novos uploads;
2. restaurar endpoint e credenciais do backend anterior;
3. validar leitura e upload;
4. manter o volume RustFS intacto para investigação;
5. não reativar automaticamente dados parcialmente migrados sem relatório de validação.

## 11. Riscos

- diferenças de compatibilidade S3: cobrir as operações reais com testes de integração;
- permissões do volume RustFS: preparar ownership para o UID da imagem;
- URLs públicas e proxy: validar `/object-storage` pelo Nginx e pelo frontend;
- perda ou duplicação de objetos: comparar listagens e hashes/amostras antes do cutover;
- credenciais versionadas: rotacionar imediatamente e remover valores reais dos arquivos;
- imagem sem tag fixa: usar versão validada e revisar atualizações explicitamente;
- RustFS ainda não validado para produção: manter GCS como opção até existir evidência operacional suficiente.

## 12. Critério de conclusão

O PLAN-023 estará concluído quando RustFS funcionar em dev e tailscale, os fluxos de upload/download e catálogo forem validados, os dados necessários tiverem sido migrados com evidência, o rollback tiver sido testado e a documentação de configuração não depender de imagens ou comandos MinIO.
