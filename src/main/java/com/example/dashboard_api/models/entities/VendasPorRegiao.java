package com.example.dashboard_api.models.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "vendas_por_regiao")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class VendasPorRegiao extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false)
    private UUID uuid = UUID.randomUUID();

    @Column(nullable = false)
    private String regiao;

    @Column(nullable = false)
    private Integer clientes;

    @Column(nullable = false)
    private Integer pedidos;

    @Column(nullable = false)
    private BigDecimal receita;

    @Column(name = "ticket_medio", nullable = false)
    private BigDecimal ticketMedio;

    @Column(nullable = false)
    private BigDecimal satisfacao;
}
