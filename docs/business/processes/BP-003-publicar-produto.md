# BP-003 — Publicar e manter produto

**Situação:** Implementado

**Objetivo:** criar, editar, consultar e remover produtos associados a um usuário.

**Gatilho:** CRUD autenticado de `/products`.

**Fluxo principal:** recebe dados do produto; no caminho associado ao usuário, resolve o username autenticado e grava a relação `Product.user`; para atualização, carrega a entidade existente, copia campos e salva; para remoção, exclui pelo id. Há consultas gerais e consultas filtradas por vendedor (`ProductService.java:17-59`).

**Dados de negócio:** nome, descrição, preço, quantidade, categoria, `inventoryStatus`, rating, imagem e `objectId` (`Product.java:20-65`). A migration exige esses campos como não nulos (`V2__create_product.sql`).

**Estados observados:** `inventoryStatus` contém valores como `INSTOCK`, `LOWSTOCK` e `OUTOFSTOCK` nos dados iniciais, mas não existe enum nem transição calculada no serviço. Portanto, o backend armazena/exibe o status; não implementa a política de estoque.

**Resultado:** produto persistido, atualizado, consultado ou removido. Não há reserva, decremento de quantidade, avaliação ou autorização explícita por proprietário no serviço analisado.

**Evidência:** `ProductService.java`, `Product.java` e `V2__create_product.sql`.
