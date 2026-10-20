package com.example.dashboard_api.dtos.Planilha;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Planilha completa, com o conteúdo serializado em `valor`.
 * O front aplica JSON.parse em `valor` para montar os gráficos.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlanilhaResponseDTO {
    private UUID uuid;
    private String nome;
    private String valor;
}
