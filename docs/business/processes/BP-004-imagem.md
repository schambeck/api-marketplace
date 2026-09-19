# BP-004 — Armazenar e associar imagem

**Situação:** Implementado parcialmente

**Objetivo:** armazenar arquivo em GCS/MinIO e associar metadados a um produto.

**Fluxo de arquivo:** upload simples ou múltiplo em `/object-storage`; o serviço cria o bucket se necessário, copia o arquivo para temporário, gera `UUID + extensão`, envia ao bucket e devolve id, nome original, tipo e tamanho (`StorageService.java:44-77`). Download recebe o id, baixa para arquivo temporário e devolve o stream (`StorageService.java:83-92`). GET de object storage é público (`SecurityConfig.java:35`).

**Fluxo de vínculo:** `/images` cria/edita/remove `Image`; o mapper resolve o produto pelo id (`ImageMapper.java:15-27`) e a entidade guarda `objectId`, nome, tipo e tamanho (`Image.java:20-60`).

**Limites:** upload e vínculo são processos separados; não há transação compensatória caso o upload ocorra e o vínculo falhe, nem remoção automática do objeto quando a imagem é excluída. A seleção GCS/MinIO depende de configuração.

**Evidência:** `StorageService.java`, `ImageMapper.java` e `Image.java`.
