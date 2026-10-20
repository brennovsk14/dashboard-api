package com.example.dashboard_api.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ErroDTO {
    private String tipoErro;
    private int status;
    private String mensagem;
}
