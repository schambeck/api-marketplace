# Consulta de ofertas do seller

O consumidor de gerenciamento de ofertas deve usar as rotas próprias do seller:

```http
GET /seller/offers?status=DRAFT&page=0&size=25&search=elden
GET /seller/offers/{id}
```

As rotas exigem JWT com `SELLER` ou `ADMIN`. O username é obtido do claim
`preferred_username`; não existe parâmetro de `sellerId` ou `userId`.

O backend filtra no repository pela relação `Offer → Product → Product.user`.
Uma oferta inexistente ou pertencente a outro seller retorna `404` no detalhe.
O resultado inclui apenas os campos públicos da oferta: produto, contexto mínimo,
preço, entrega, disponibilidade, status e auditoria. Campos legados de contexto,
custos, settlement e supply não fazem parte da resposta.

O endpoint administrativo mantido é somente:

```http
GET /admin/offers/{id}
```

Ele exige `ADMIN` e é destinado à moderação. A listagem administrativa ampla não
faz parte do MVP.

Este repositório contém somente a API; a migração de um frontend externo para
`/seller/offers` precisa ser aplicada no repositório consumidor correspondente.
