package com.example.dashboard_api.controllers.docs;

import com.example.dashboard_api.dtos.VendasPorRegiao.VendasPorRegiaoResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.List;

@Tag(name = "Vendas por Região", description = "Resumo de clientes, pedidos e receita agregados por região.")
public interface VendasPorRegiaoControllerDocs {

    @Operation(summary = "Listar vendas por região", description = "Retorna a lista paginada do resumo de vendas por região.")
    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = VendasPorRegiaoResponseDTO.class)))
    ResponseEntity<List<VendasPorRegiaoResponseDTO>> listar(
            @Parameter(description = "Número da página (base 1)", example = "1") int pagina,
            @Parameter(description = "Registros por página", example = "20") int tamanho
    );
}
