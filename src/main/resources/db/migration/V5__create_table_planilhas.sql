CREATE TABLE planilhas (
                           id         BIGSERIAL PRIMARY KEY,
                           uuid       UUID NOT NULL UNIQUE,
                           nome       VARCHAR(255) NOT NULL,
                           valor      JSONB NOT NULL,
                           created_at TIMESTAMP NOT NULL DEFAULT now(),
                           updated_at TIMESTAMP NOT NULL DEFAULT now()
);
