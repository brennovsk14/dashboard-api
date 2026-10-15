CREATE TABLE vendas_mensais (
                                id          BIGSERIAL PRIMARY KEY,
                                uuid        UUID NOT NULL UNIQUE,
                                mes         VARCHAR(10) NOT NULL,
                                eletronicos NUMERIC(12,2) NOT NULL,
                                vestuario   NUMERIC(12,2) NOT NULL,
                                alimentos   NUMERIC(12,2) NOT NULL,
                                livros      NUMERIC(12,2) NOT NULL,
                                created_at  TIMESTAMP NOT NULL DEFAULT now(),
                                updated_at  TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE vendas_por_regiao (
                                   id            BIGSERIAL PRIMARY KEY,
                                   uuid          UUID NOT NULL UNIQUE,
                                   regiao        VARCHAR(50) NOT NULL,
                                   clientes      INTEGER NOT NULL,
                                   pedidos       INTEGER NOT NULL,
                                   receita       NUMERIC(14,2) NOT NULL,
                                   ticket_medio  NUMERIC(10,2) NOT NULL,
                                   satisfacao    NUMERIC(3,1) NOT NULL,
                                   created_at    TIMESTAMP NOT NULL DEFAULT now(),
                                   updated_at    TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE pedidos (
                         id              BIGSERIAL PRIMARY KEY,
                         uuid            UUID NOT NULL UNIQUE,
                         data            DATE NOT NULL,
                         regiao          VARCHAR(50) NOT NULL,
                         categoria       VARCHAR(50) NOT NULL,
                         quantidade      INTEGER NOT NULL,
                         valor_unitario  NUMERIC(10,2) NOT NULL,
                         valor_total     NUMERIC(12,2) NOT NULL,
                         avaliacao       INTEGER NOT NULL CHECK (avaliacao BETWEEN 1 AND 5),
                         created_at      TIMESTAMP NOT NULL DEFAULT now(),
                         updated_at      TIMESTAMP NOT NULL DEFAULT now()
);