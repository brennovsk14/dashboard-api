package com.example.dashboard_api.dtos.Pedido;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PedidoResponseDTO {
    private UUID uuid;
    private LocalDate data;
    private String regiao;
    private String categoria;
    private Integer quantidade;
    private BigDecimal valorUnitario;
    private BigDecimal valorTotal;
    private Integer avaliacao;
}
