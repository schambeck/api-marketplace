# PLAN-019 — Consulta administrativa de ofertas

**PRD relacionado:** [`PRD-002`](../prd/PRD-002-oferta-disponibilidade.md)

**Planos relacionados:** [`PLAN-002`](PLAN-002-oferta-disponibilidade.md) · [`PLAN-018`](PLAN-018-busca-textual-ofertas.md)

**Objetivo:** disponibilizar para usuários `ADMIN` a consulta paginada de ofertas em qualquer estado comercial, permitindo que a UI administrativa liste e abra ofertas `DRAFT`, `PUBLISHED`, `PAUSED` e `RETIRED` sem alterar o comportamento do catálogo público.

## Estado atual e lacuna

O backend já possui:

- `POST /offers` para criar ofertas;
- `PUT /offers/{id}` para editar ofertas `DRAFT`;
- ações administrativas de publicar, pausar, reativar, retirar e alterar disponibilidade;
- `GET /offers/{id}/history` para consultar o histórico;
- `OfferService.findById(Integer)`, que consulta qualquer estado, mas não é exposto pelo controller.

Atualmente `GET /offers` e `GET /offers/{id}` são consultas públicas restritas a ofertas `PUBLISHED`. A UI não consegue listar ou abrir ofertas `DRAFT`, `PAUSED` ou `RETIRED`.

## Alvo

Adicionar uma consulta administrativa separada, sem reutilizar a semântica pública:

```http
GET /admin/offers?status=DRAFT&page=0&size=25&search=elden
GET /admin/offers/{id}
```

Ambos os endpoints exigem `ROLE_ADMIN`. A consulta pública `GET /offers` permanece limitada a `PUBLISHED` e pública.

## Contrato proposto

### Listagem

`GET /admin/offers`

Parâmetros:

- `status`: opcional; valores `DRAFT`, `PUBLISHED`, `PAUSED` ou `RETIRED`;
- `search`: opcional; busca case-insensitive em produto e snapshot do contexto;
- `page`, `size` e `sort`: paginação Spring existente.

Com `status` ausente, retornar todos os estados. A ordenação padrão deve ser estável por `offer.id asc`, salvo `sort` explícito compatível com o contrato existente.

Resposta: `Page<OfferWeb>`.

### Detalhe

`GET /admin/offers/{id}`

Retorna `OfferWeb` independentemente do status, sem expor dados de auditoria além dos campos já públicos. O histórico continua em `/admin/offers/{id}/history` ou deve receber uma rota equivalente consistente; não duplicar duas URLs para o mesmo recurso sem necessidade.

## Abordagem técnica

- Criar `AdminOfferController` com `@RequestMapping("admin/offers")` e `@PreAuthorize("hasRole('ADMIN')")`, mantendo separado o `OfferController` público.
- Adicionar ao `OfferService` métodos `searchAdmin(Pageable, OfferStatus, String)` e `findAdminById(Integer)`, reutilizando a mesma entidade e `OfferMapper`.
- Adicionar ao `OfferRepository` uma consulta paginada com `join fetch offer.product`, filtro opcional por `OfferStatus` e busca textual nos campos de `Product` e no snapshot de contexto (`contextGame`, `contextPublisher`, `contextPlatform`, `contextRegion`, `contextServer`, `contextVariant`).
- Usar `countQuery` sem `fetch` para evitar contagem incorreta em `Page`.
- Normalizar `search == null` e texto em branco para ausência de filtro.
- Validar `status` pelo enum; valor inválido deve resultar em `400` com mensagem clara.
- Usar `findByIdOrThrow` para o detalhe administrativo e preservar `404` para ID inexistente.
- Não alterar regras de transição, disponibilidade, locking, histórico ou checkout.
- Atualizar `schema/api-schema-marketplace.json` e documentação OpenAPI, se o schema versionado cobrir os endpoints do módulo.

## Segurança

- `GET /offers/**` continua público para catálogo.
- `GET /admin/offers/**` deve exigir autenticação e `ROLE_ADMIN` tanto no controller quanto, se necessário, na configuração HTTP.
- Não permitir que um usuário autenticado sem `ADMIN` use o endpoint administrativo.
- Não retornar credenciais, dados de supply, custo, tokens ou informações fora de `OfferWeb`.
- Confirmar que a conversão de roles JWT continua produzindo `ROLE_ADMIN`.

## Sequência de implementação

1. Confirmar o contrato final de rota (`/admin/offers`) e os campos de busca com o consumidor frontend.
2. Implementar consulta paginada no `OfferRepository`, incluindo `countQuery` e filtro por status.
3. Implementar métodos de consulta no `OfferService`.
4. Criar `AdminOfferController` e validar `status`/`search`.
5. Atualizar schema/OpenAPI e documentação do endpoint.
6. Adicionar testes unitários de serviço/controller e testes de integração PostgreSQL/Spring Security.
7. Validar manualmente com tokens `ADMIN` e usuário sem `ADMIN`.
8. Executar Maven, revisar diff e confirmar que o catálogo público não mudou.

## Testes e evidências

| Critério | Evidência esperada |
|---|---|
| Admin lista todos os estados | teste de integração com fixtures `DRAFT`, `PUBLISHED`, `PAUSED` e `RETIRED` |
| Filtro por status | teste verifica que somente o enum solicitado aparece |
| Busca textual | teste cobre produto, game, plataforma, região e campos opcionais do snapshot |
| Paginação e ordenação | teste verifica `totalElements`, página, tamanho e ordem por ID |
| Detalhe de oferta não publicada | `GET /admin/offers/{id}` retorna `DRAFT`, `PAUSED` e `RETIRED` |
| ID inexistente | resposta `404` |
| Segurança | `ADMIN` recebe `200`; anônimo recebe `401`; usuário autenticado sem role recebe `403` |
| Catálogo público preservado | `GET /offers` e `/catalog/offers` continuam retornando somente `PUBLISHED` |
| Regressão | `./mvnw clean test -Dmaven.plugin.validation=BRIEF` |

Se não houver infraestrutura de teste de segurança/HTTP disponível, registrar a limitação e executar pelo menos testes unitários do serviço/repository e validação manual com JWT local.

## Compatibilidade com o frontend

O frontend deverá consumir a nova rota para:

- listar ofertas por status;
- abrir detalhes de ofertas não publicadas;
- exibir histórico;
- acionar os endpoints administrativos já existentes.

Esta etapa não altera as rotas públicas nem exige migration de banco.

## Riscos e mitigação

- **Vazamento de dados administrativos:** manter DTO `OfferWeb` e autorização explícita; adicionar teste negativo de role.
- **Regressão do catálogo:** usar controller/serviço separados e testar `/offers` sem status administrativo.
- **Contagem incorreta com `join fetch`:** usar `countQuery` dedicado.
- **Filtro inconsistente entre público e admin:** centralizar a expressão de busca no repository e documentar os campos.
- **Enum inválido:** converter/validar antes de chamar o serviço e retornar `400` determinístico.

## Rollback

Reverter o controller administrativo, os métodos do serviço/repository, os testes e a documentação/schema. Não há migration nem alteração de dados. As rotas públicas e as ações administrativas existentes permanecem intactas.

