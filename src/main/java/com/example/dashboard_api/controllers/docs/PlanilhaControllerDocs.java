package com.example.dashboard_api.controllers.docs;

import com.example.dashboard_api.dtos.Planilha.PlanilhaResponseDTO;
import com.example.dashboard_api.dtos.Planilha.PlanilhaResumoDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Tag(
        name = "Planilhas",
        description = """
                Upload genérico de planilhas .xlsx.

                O backend lê a **primeira aba** do arquivo, usa a primeira linha como
                cabeçalho e converte cada linha seguinte em um objeto JSON. O resultado
                é armazenado como um array JSON no campo `valor`, que o front pode
                consumir diretamente para montar gráficos.
                """
)
public interface PlanilhaControllerDocs {

    @Operation(
            summary = "Enviar planilha",
            description = """
                    Recebe um arquivo `.xlsx` via multipart e devolve o `uuid` gerado,
                    que deve ser usado depois para buscar o conteúdo convertido.

                    **Exemplo de conversão** — uma aba com as colunas `Mês | Vendas`:
                    ```json
                    [
                      {"Mês": "Jan", "Vendas": 42954.15},
                      {"Mês": "Fev", "Vendas": 41838.83}
                    ]
                    ```
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Planilha armazenada com sucesso",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PlanilhaResumoDTO.class),
                            examples = @ExampleObject(value = """
                                    {"uuid": "d3e4f5a6-3333-4def-b011-223344556677", "nome": "Vendas 2026"}
                                    """))),
            @ApiResponse(responseCode = "400", description = "Arquivo ausente, formato inválido ou planilha vazia",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)),
            @ApiResponse(responseCode = "413", description = "Arquivo acima do tamanho máximo permitido",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    })
    ResponseEntity<PlanilhaResumoDTO> criar(
            @Parameter(description = "Nome de identificação da planilha", example = "Vendas 2026", required = true) String nome,
            @Parameter(description = "Arquivo .xlsx", required = true) MultipartFile arquivo
    );


    @Operation(
            summary = "Listar planilhas",
            description = "Lista paginada das planilhas armazenadas, da mais recente para a mais antiga. Não inclui o conteúdo (`valor`)."
    )
    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = PlanilhaResumoDTO.class)))
    ResponseEntity<List<PlanilhaResumoDTO>> listar(
            @Parameter(description = "Número da página (base 1)", example = "1") int pagina,
            @Parameter(description = "Registros por página", example = "20") int tamanho
    );


    @Operation(
            summary = "Buscar planilha por UUID",
            description = "Retorna a planilha com o conteúdo completo em `valor` (string JSON pronta para `JSON.parse`)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Planilha encontrada",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = PlanilhaResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Planilha não encontrada",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE))
    })
    ResponseEntity<PlanilhaResponseDTO> buscar(
            @Parameter(description = "UUID da planilha", example = "d3e4f5a6-3333-4def-b011-223344556677", required = true) UUID uuid
    );
}
