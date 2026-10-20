package com.example.dashboard_api.models.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "planilhas")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Planilha extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false)
    private UUID uuid = UUID.randomUUID();

    @Column(nullable = false)
    private String nome;

    /**
     * Conteúdo da planilha serializado como JSON (array de objetos, um por linha),
     * guardado como texto puro.
     *
     * Não usar os tipos json/jsonb do Postgres aqui: o Hibernate envia o parâmetro
     * tipado como jsonb, que normaliza o documento e reordena as chaves por tamanho,
     * perdendo a ordem original das colunas da planilha.
     */
    @Column(nullable = false, columnDefinition = "text")
    private String valor;
}
