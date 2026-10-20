package com.example.dashboard_api.exceptions;

import com.example.dashboard_api.dtos.ErroDTO;
import lombok.Getter;

import java.util.List;

@Getter
public class ValidacaoException extends RuntimeException {
    private final List<ErroDTO> erros;

    public ValidacaoException(List<ErroDTO> erros) {
        super("Falha na validação");
        this.erros = erros;
    }
}
