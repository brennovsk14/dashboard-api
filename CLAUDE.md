# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Sobre o projeto

dashboard.api é um backend (Spring Boot 4.1 / Java 21) de leitura, servindo dados de dashboard a partir de 3 tabelas populadas via Flyway a partir de uma planilha de exemplo (`dados-graficos.xlsx`: vendas mensais, vendas por região, pedidos). É um projeto pessoal, construído seguindo a mesma arquitetura em camadas do [net-tracker-api](../net-tracker-api) (COOASD/SEPLAG-RJ), mas sem o lado de escrita/autenticação que aquele projeto tem.

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

Regra do Flyway: depois que uma migration já rodou em algum ambiente, **nunca editar o arquivo** — criar uma nova versão (`V5`, `V6`...) para qualquer correção.

### Camadas (simplificado — API é só leitura)

Diferente do net-tracker-api, não existe escrita via API (sem `POST`/`PUT`/`DELETE`), então várias camadas do padrão original foram deliberadamente omitidas: sem `Validation`, sem histórico manual (`Historico*`), sem Hibernate Envers (`@Audited`).

Fluxo por recurso: `Controller` (+ interface `ControllerDocs` para anotações do Swagger) → `Service` → `Repository` → `Entity`, com `DTO` de resposta.

Recursos existentes (`VendasMensais`, `VendasPorRegiao`, `Pedido`), cada um com:
- `GET /<recurso>/get` — paginado (`pagina`, `page-size`), headers `x-total-content`/`x-total-pages`/`x-pagina-atual` (mesma convenção do net-tracker-api)
- Entidades expõem só `uuid` externamente, nunca o `id` (Long) interno
- `PedidoRepository` já tem `JpaSpecificationExecutor` pronto para filtro futuro (região/categoria/data), mas o `Service` ainda não usa — `Pedido` é a única tabela com volume (300 linhas) que justificaria filtro

`BaseEntity` (createdAt/updatedAt) depende de `@EnableJpaAuditing`, configurado em `config/JpaConfig.java`.

### Segurança — ainda não implementada de verdade

`config/SecurityConfig.java` libera todas as rotas (`permitAll`) porque não existe sistema de autenticação/usuários no projeto. Isso é aceitável para uso local/pessoal, mas **precisa ser revisto antes de qualquer exposição pública** — o net-tracker-api tem um sistema de JWT (`java-jwt`, já presente no `pom.xml` daqui também, mas não usado ainda) que serviria de referência caso autenticação seja implementada.

## Testes de API

Collection do Postman em `postman/dashboard-api.postman_collection.json`, com testes automatizados (status, headers de paginação, contagem de registros, regras de negócio como `valorTotal = quantidade × valorUnitario`). Usa a variável `{{baseUrl}}` (`http://localhost:8080`).

## Repositório

Hospedado em `https://github.com/brennovsk14/dashboard-api` (GitHub pessoal, não o GitLab interno da SEPLAG usado pelo net-tracker-api).
