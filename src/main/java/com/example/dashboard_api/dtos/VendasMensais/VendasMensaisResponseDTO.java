package com.example.dashboard_api.dtos.VendasMensais;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VendasMensaisResponseDTO {
    private UUID uuid;
    private String mes;
    private BigDecimal eletronicos;
    private BigDecimal vestuario;
    private BigDecimal alimentos;
    private BigDecimal livros;
}
