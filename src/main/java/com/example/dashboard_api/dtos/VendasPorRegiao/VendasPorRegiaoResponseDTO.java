package com.example.dashboard_api.dtos.VendasPorRegiao;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VendasPorRegiaoResponseDTO {
    private UUID uuid;
    private String regiao;
    private Integer clientes;
    private Integer pedidos;
    private BigDecimal receita;
    private BigDecimal ticketMedio;
    private BigDecimal satisfacao;
}
