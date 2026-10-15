package com.example.dashboard_api.controllers.docs;

import com.example.dashboard_api.dtos.VendasMensais.VendasMensaisResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.List;

@Tag(name = "Vendas Mensais", description = "Série histórica de vendas por categoria, agregada por mês.")
public interface VendasMensaisControllerDocs {

    @Operation(summary = "Listar vendas mensais", description = "Retorna a lista paginada de vendas mensais por categoria.")
    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso",
            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = VendasMensaisResponseDTO.class)))
    ResponseEntity<List<VendasMensaisResponseDTO>> listar(
            @Parameter(description = "Número da página (base 1)", example = "1") int pagina,
            @Parameter(description = "Registros por página", example = "20") int tamanho
    );
}
