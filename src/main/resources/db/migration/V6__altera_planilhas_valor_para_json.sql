-- jsonb normaliza o documento e não preserva a ordem das chaves, o que embaralharia
-- a ordem original das colunas da planilha. O tipo `json` guarda o texto exato
-- (ainda validando que é JSON válido), mantendo a ordem das colunas.
ALTER TABLE planilhas
    ALTER COLUMN valor TYPE json USING valor::text::json;
