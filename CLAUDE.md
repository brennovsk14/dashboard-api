# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Sobre o projeto

dashboard.api é o backend (Spring Boot 4.1 / Java 21) de um sistema que lê planilhas e as transforma em gráficos (frontend Angular + ECharts, em projeto separado). É um projeto pessoal, construído seguindo a mesma arquitetura em camadas do [net-tracker-api](../net-tracker-api) (COOASD/SEPLAG-RJ), mas sem a autenticação que aquele projeto tem.

Tem duas gerações de recursos convivendo:

1. **Tabelas fixas** (`vendas_mensais`, `vendas_por_regiao`, `pedidos`) — schema tipado, populado via migrations de seed a partir de `dados-graficos.xlsx`. Só leitura.
2. **Tabela genérica** (`planilhas`) — recebe upload de qualquer `.xlsx`, converte para JSON e guarda como texto. É a direção para onde o sistema está indo: o front envia a planilha, busca pelo uuid e monta o gráfico a partir do JSON.

## Comandos

```bash
# subir o Postgres de dev via docker-compose (porta 5435 — mesma porta do net-tracker-api, não rode os dois ao mesmo tempo)
docker compose up -d db

# rodar localmente (perfil local, porta 8080)
./mvnw spring-boot:run -Dspring-boot.run.profiles=local

# build completo
./mvnw clean package

# rodar testes
./mvnw test
```

Pelo IntelliJ: a Run Configuration precisa ter o profile `local` ativo (campo "Active profiles" se for Ultimate, ou VM option `-Dspring.profiles.active=local` caso contrário) — sem isso a aplicação sobe sem `spring.datasource.url` configurado e falha ao iniciar.

Perfil `local`: `application-local.properties`, porta 8080, DB em `localhost:5435` (`Dashboard_development` / `DASHBOARD_DEV` / `Dashboard.DEV123`).

Swagger UI em `/swagger-ui.html` (sem context-path, diferente do net-tracker-api que usa `/api`).

### Banco de dados

**Compartilha a porta 5435 com o net-tracker-api de propósito** (combinado para não rodar os dois projetos ao mesmo tempo) — se a porta já estiver em uso pelo outro projeto, o container não sobe.

Sempre suba/derrube o banco via `docker compose`, nunca pelo botão "Restart" do container na IDE ou Docker Desktop — isso pode recriar o container sem reaplicar o mapeamento de porta do `docker-compose.yml` (já aconteceu uma vez). O serviço tem `restart: unless-stopped` para se recompor sozinho se o Docker reiniciar.

## Arquitetura

### Diferença-chave em relação ao net-tracker-api: schema via Flyway, não `ddl-auto=update`

Esse projeto usa `spring.jpa.hibernate.ddl-auto=validate` + `spring.flyway.enabled=true`. O **Flyway é o dono do schema** (migrations versionadas em `src/main/resources/db/migration`); o Hibernate só valida se as entidades batem com as tabelas. Isso é diferente do net-tracker-api, que usa `ddl-auto=update` e não tem pasta `db/migration` ainda.

Migrations atuais:
- `V1__create_tables.sql` — cria `vendas_mensais`, `vendas_por_regiao`, `pedidos`
- `V2`/`V3`/`V4__seed_*.sql` — populam as 3 tabelas com os dados da planilha original (carga única, via `INSERT ... VALUES` com `gen_random_uuid()`)
- `V5__create_table_planilhas.sql` — cria `planilhas` (upload genérico)
- `V6`/`V7` — correções do tipo da coluna `planilhas.valor` (`jsonb` → `json` → `text`); ver "Por que `valor` é text" abaixo

Regra do Flyway: depois que uma migration já rodou em algum ambiente, **nunca editar o arquivo** — criar uma nova versão para qualquer correção. (V6 e V7 são exatamente isso: correções de uma decisão errada na V5.)

### Camadas (simplificado)

Comparado ao net-tracker-api, camadas deliberadamente omitidas: sem `Validation` como camada separada (as validações vivem no próprio Service), sem histórico manual (`Historico*`), sem Hibernate Envers (`@Audited`).

Fluxo por recurso: `Controller` (+ interface `ControllerDocs` para anotações do Swagger) → `Service` → `Repository` → `Entity`, com `DTO` de resposta.

Convenções comuns a todos os recursos:
- `GET /<recurso>/get` — paginado (`pagina` base 1, `page-size`), headers `x-total-content`/`x-total-pages`/`x-pagina-atual` (mesma convenção do net-tracker-api)
- Entidades expõem só `uuid` externamente, nunca o `id` (Long) interno
- `BaseEntity` (createdAt/updatedAt) depende de `@EnableJpaAuditing`, configurado em `config/JpaConfig.java`

Recursos de leitura (`VendasMensais`, `VendasPorRegiao`, `Pedido`): só o `GET /get`. `PedidoRepository` já tem `JpaSpecificationExecutor` pronto para filtro futuro (região/categoria/data), mas o Service ainda não usa.

### Upload de planilhas (`Planilha`)

Único recurso com escrita. Endpoints:
- `POST /planilhas/post` — multipart (`nome` como query param, `arquivo` como parte), devolve **201** com `{uuid, nome}`
- `GET /planilhas/get` — lista paginada **sem** o campo `valor` (pode ser grande demais para trafegar numa listagem)
- `GET /planilhas/{uuid}/get` — planilha completa, com `valor`

`PlanilhaService` usa Apache POI (`poi-ooxml`) para ler **apenas a primeira aba**: a primeira linha vira o cabeçalho (chaves do JSON) e cada linha seguinte vira um objeto. Detalhes do parser que importam:
- Datas viram string ISO (`"2026-01-01"`), não o número serial do Excel
- Inteiros saem como inteiro (`6`), não `6.0`
- Células de fórmula usam o valor em cache (`getCachedFormulaResultType`)
- Cabeçalhos vazios viram `coluna_N`; repetidos ganham sufixo `_2`, `_3`…
- Linhas totalmente vazias são descartadas
- Ordem das colunas é preservada (`LinkedHashMap`)

Limite de upload em `spring.servlet.multipart.max-file-size` (10MB; o padrão do Spring é só 1MB).

#### Por que `valor` é `text` e não `json`/`jsonb`

Tentativa inicial foi `jsonb`, corrigida depois para `json` e enfim `text` (migrations V5→V6→V7). Motivo: o Hibernate, com `@JdbcTypeCode(SqlTypes.JSON)` no dialeto Postgres, envia o parâmetro **tipado como jsonb** independente do tipo da coluna. O jsonb normaliza o documento e **reordena as chaves por tamanho em bytes**, embaralhando a ordem original das colunas da planilha — o que importa para gráficos (convenção "primeira coluna = eixo X"). Como o JSON é gerado pelo Jackson e portanto sempre válido por construção, a validação do banco não agregava nada aqui.

**Não "melhore" essa coluna de volta para jsonb** sem resolver o problema de ordenação (exigiria um `JdbcType` customizado que envie `PGobject` com tipo `json`).

### Jackson 3, não Jackson 2

Spring Boot 4.1 auto-configura `tools.jackson.databind.json.JsonMapper` (Jackson 3, pacote `tools.jackson`). O classpath **também** tem `com.fasterxml.jackson` 2.x, puxado transitivamente por `java-jwt` e `springdoc` — injetar o `ObjectMapper` do `com.fasterxml` não funciona. Em Jackson 3, `JacksonException` é unchecked, então `writeValueAsString` não precisa de try/catch.

### Tratamento de erro

`exceptions/GlobalExceptionHandler.java` (`@RestControllerAdvice`) converte exceções em `List<ErroDTO>`, seguindo a convenção do net-tracker-api. Services lançam `ValidacaoException(List<ErroDTO>)`; o status HTTP sai do primeiro `ErroDTO` da lista.

### Segurança — ainda não implementada de verdade

`config/SecurityConfig.java` libera todas as rotas (`permitAll`) porque não existe sistema de autenticação/usuários no projeto.

O que limita o acesso hoje:
- **CORS restrito** às origens em `app.cors.origens` (por padrão o dev server do Angular, `localhost:4200`). Origem não listada recebe **403** no preflight.
- **Limites de upload**: `spring.servlet.multipart.max-file-size` (10MB) e, no `PlanilhaService`, tetos de `app.planilha.max-linhas` / `max-colunas`. Os tetos de conteúdo não são redundantes com o de arquivo: `.xlsx` é um zip, então um arquivo pequeno pode expandir em milhões de células.

**Isso não é autenticação.** CORS só vale para chamadas de navegador — `curl`/Postman acessam normalmente. É uma medida de proporcionalidade para uso local, deliberadamente temporária.

**Evolução planejada (decidida em 2026-10-20): JWT completo**, nos moldes do net-tracker-api — tabela de usuários, endpoint de login, hash de senha, filtro JWT, mais tela de login e interceptor no Angular. O `java-jwt` (com.auth0) já está no `pom.xml` para isso. Não tratar o estado atual como final nem propor API key como alternativa.

## Testes de API

Collection do Postman em `postman/dashboard-api.postman_collection.json`, com testes automatizados (status, headers de paginação, contagem de registros, regras de negócio como `valorTotal = quantidade × valorUnitario`, e preservação da ordem das colunas no upload). Usa as variáveis `{{baseUrl}}` (`http://localhost:8080`) e `{{planilhaUuid}}` (preenchida automaticamente pela request de upload).

Para testar o upload pela linha de comando:
```bash
curl -X POST "http://localhost:8080/planilhas/post?nome=Minha%20Planilha" -F "arquivo=@caminho/para/planilha.xlsx"
```

## Frontend (Angular)

O consumidor é o projeto Angular em [../../dashboard](../../dashboard) (Angular 21 + ngx-echarts, SSR habilitado). Pontos de contato:

- `src/environments/environment.ts` guarda a `apiUrl`; a origem do dev server (`localhost:4200`) precisa estar em `app.cors.origens` aqui.
- `services/planilha/planilha-api.service.ts` é quem fala com esta API: envia o `.xlsx`, lista as salvas e busca por uuid, fazendo o `JSON.parse` do campo `valor`.
- As colunas do gráfico saem de `Object.keys()` da primeira linha do `valor` — **é daí que vem a dependência da ordem das colunas** descrita acima. A primeira coluna vira o eixo X por padrão.
- O `services/planilha/planilha.service.ts` (SheetJS, parsing no navegador) continua no código mas não é mais usado: o upload passa pelo backend para ficar persistido.

## Repositório

Hospedado em `https://github.com/brennovsk14/dashboard-api` (GitHub pessoal, não o GitLab interno da SEPLAG usado pelo net-tracker-api).
