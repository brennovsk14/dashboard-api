package com.example.dashboard_api.models.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "vendas_mensais")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class VendasMensais extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false)
    private UUID uuid = UUID.randomUUID();

    @Column(nullable = false)
    private String mes;

    @Column(nullable = false)
    private BigDecimal eletronicos;

    @Column(nullable = false)
    private BigDecimal vestuario;

    @Column(nullable = false)
    private BigDecimal alimentos;

    @Column(nullable = false)
    private BigDecimal livros;
}
