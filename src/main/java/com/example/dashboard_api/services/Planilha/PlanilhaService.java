package com.example.dashboard_api.services.Planilha;

import com.example.dashboard_api.dtos.ErroDTO;
import com.example.dashboard_api.dtos.Planilha.PlanilhaResponseDTO;
import com.example.dashboard_api.dtos.Planilha.PlanilhaResumoDTO;
import com.example.dashboard_api.exceptions.ValidacaoException;
import com.example.dashboard_api.models.entities.Planilha;
import com.example.dashboard_api.repositories.PlanilhaRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.databind.json.JsonMapper;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PlanilhaService {

    private final PlanilhaRepository repository;
    private final JsonMapper jsonMapper;

    /**
     * Tetos de conteúdo. O limite de tamanho do upload não basta: .xlsx é um zip,
     * então um arquivo pequeno pode se expandir em milhões de células e gerar um
     * JSON enorme em memória.
     */
    @Value("${app.planilha.max-linhas}")
    private int maxLinhas;

    @Value("${app.planilha.max-colunas}")
    private int maxColunas;

    /**
     * Lê a primeira aba do .xlsx enviado, converte as linhas em JSON
     * (array de objetos, chaveado pelos títulos das colunas) e persiste.
     */
    @Transactional
    public PlanilhaResumoDTO salvar(String nome, MultipartFile arquivo) {
        validarEntrada(nome, arquivo);

        List<Map<String, Object>> linhas = lerPrimeiraAba(arquivo);
        if (linhas.isEmpty()) {
            throw erro(400, "A planilha não contém nenhuma linha de dados.");
        }

        Planilha planilha = new Planilha();
        planilha.setNome(nome.trim());
        planilha.setValor(jsonMapper.writeValueAsString(linhas));
        repository.save(planilha);

        return new PlanilhaResumoDTO(planilha.getUuid(), planilha.getNome());
    }

    /** Listagem sem o conteúdo — `valor` pode ser grande demais para trafegar numa lista. */
    public Page<PlanilhaResumoDTO> listar(int pagina, int tamanho) {
        Pageable pageable = PageRequest.of(Math.max(0, pagina - 1), tamanho,
                Sort.by("createdAt").descending());

        return repository.findAll(pageable)
                .map(p -> new PlanilhaResumoDTO(p.getUuid(), p.getNome()));
    }

    public PlanilhaResponseDTO buscarPorUuid(UUID uuid) {
        Planilha planilha = repository.findByUuid(uuid)
                .orElseThrow(() -> erro(404, "Planilha não encontrada."));

        return new PlanilhaResponseDTO(planilha.getUuid(), planilha.getNome(), planilha.getValor());
    }

    // ---------------------------------------------------------------- leitura

    private List<Map<String, Object>> lerPrimeiraAba(MultipartFile arquivo) {
        try (InputStream is = arquivo.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {

            if (workbook.getNumberOfSheets() == 0) {
                throw erro(400, "O arquivo não contém nenhuma aba.");
            }

            Sheet aba = workbook.getSheetAt(0);
            Row cabecalho = aba.getRow(aba.getFirstRowNum());
            if (cabecalho == null || cabecalho.getLastCellNum() <= 0) {
                throw erro(400, "A primeira aba está vazia ou não tem linha de cabeçalho.");
            }

            if (cabecalho.getLastCellNum() > maxColunas) {
                throw erro(400, "A planilha excede o limite de " + maxColunas + " colunas.");
            }

            List<String> colunas = lerCabecalho(cabecalho);
            List<Map<String, Object>> linhas = new ArrayList<>();

            for (int i = cabecalho.getRowNum() + 1; i <= aba.getLastRowNum(); i++) {
                Row linha = aba.getRow(i);
                if (linha == null) continue;

                Map<String, Object> registro = new LinkedHashMap<>();
                boolean vazia = true;

                for (int c = 0; c < colunas.size(); c++) {
                    Object valor = extrairValor(linha.getCell(c));
                    if (valor != null) vazia = false;
                    registro.put(colunas.get(c), valor);
                }

                if (!vazia) {
                    // corta antes de acumular, para não estourar a memória no caminho
                    if (linhas.size() >= maxLinhas) {
                        throw erro(400, "A planilha excede o limite de " + maxLinhas + " linhas.");
                    }
                    linhas.add(registro);
                }
            }

            return linhas;

        } catch (ValidacaoException e) {
            throw e;
        } catch (Exception e) {
            throw erro(400, "Não foi possível ler o arquivo. Verifique se é um .xlsx válido.");
        }
    }

    /** Títulos das colunas; preenche vazios e desambigua repetidos. */
    private List<String> lerCabecalho(Row cabecalho) {
        List<String> colunas = new ArrayList<>();
        Set<String> usados = new HashSet<>();

        for (int c = 0; c < cabecalho.getLastCellNum(); c++) {
            Object bruto = extrairValor(cabecalho.getCell(c));
            String nome = (bruto == null) ? "" : String.valueOf(bruto).trim();

            if (nome.isBlank()) nome = "coluna_" + (c + 1);

            String base = nome;
            int sufixo = 2;
            while (!usados.add(nome)) {
                nome = base + "_" + sufixo++;
            }
            colunas.add(nome);
        }
        return colunas;
    }

    /** Converte a célula para o tipo Java mais natural (texto, número, booleano, data ISO). */
    private Object extrairValor(Cell celula) {
        if (celula == null) return null;

        CellType tipo = (celula.getCellType() == CellType.FORMULA)
                ? celula.getCachedFormulaResultType()
                : celula.getCellType();

        return switch (tipo) {
            case STRING -> {
                String texto = celula.getStringCellValue().trim();
                yield texto.isEmpty() ? null : texto;
            }
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(celula)) {
                    LocalDateTime dataHora = celula.getLocalDateTimeCellValue();
                    yield dataHora.toLocalTime().equals(LocalTime.MIDNIGHT)
                            ? dataHora.toLocalDate().toString()
                            : dataHora.toString();
                }
                double numero = celula.getNumericCellValue();
                // evita que inteiros virem "6.0" no JSON
                yield (numero == Math.rint(numero) && !Double.isInfinite(numero))
                        ? (Object) (long) numero
                        : (Object) numero;
            }
            case BOOLEAN -> celula.getBooleanCellValue();
            default -> null;
        };
    }

    // ------------------------------------------------------------ validações

    private void validarEntrada(String nome, MultipartFile arquivo) {
        List<ErroDTO> erros = new ArrayList<>();

        if (nome == null || nome.isBlank()) {
            erros.add(new ErroDTO("Erro", 400, "O nome da planilha é obrigatório."));
        }
        if (arquivo == null || arquivo.isEmpty()) {
            erros.add(new ErroDTO("Erro", 400, "O arquivo da planilha é obrigatório."));
        } else {
            String original = arquivo.getOriginalFilename();
            if (original == null || !original.toLowerCase().endsWith(".xlsx")) {
                erros.add(new ErroDTO("Erro", 400, "Apenas arquivos .xlsx são aceitos."));
            }
        }

        if (!erros.isEmpty()) throw new ValidacaoException(erros);
    }

    private ValidacaoException erro(int status, String mensagem) {
        return new ValidacaoException(List.of(new ErroDTO("Erro", status, mensagem)));
    }
}
