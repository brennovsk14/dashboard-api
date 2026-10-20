-- Tanto `jsonb` quanto `json` acabam normalizando o documento na escrita, porque o
-- Hibernate envia o parâmetro tipado como jsonb (chaves reordenadas por tamanho),
-- o que embaralha a ordem original das colunas da planilha.
-- Como o JSON é gerado pelo Jackson (sempre válido por construção), a validação do
-- banco não agrega aqui; `text` guarda exatamente o que foi serializado.
ALTER TABLE planilhas
    ALTER COLUMN valor TYPE text USING valor::text;
