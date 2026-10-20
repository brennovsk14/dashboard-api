package com.example.dashboard_api.dtos.Planilha;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Representação enxuta da planilha, sem o conteúdo.
 * Usada na listagem e como resposta do upload.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlanilhaResumoDTO {
    private UUID uuid;
    private String nome;
}
