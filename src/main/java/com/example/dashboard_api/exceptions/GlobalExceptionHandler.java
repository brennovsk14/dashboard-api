package com.example.dashboard_api.exceptions;

import com.example.dashboard_api.dtos.ErroDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ValidacaoException.class)
    public ResponseEntity<List<ErroDTO>> handleValidacao(ValidacaoException ex) {
        int status = ex.getErros().isEmpty() ? HttpStatus.BAD_REQUEST.value() : ex.getErros().get(0).getStatus();
        return ResponseEntity.status(status).body(ex.getErros());
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErroDTO> handleUploadMuitoGrande() {
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(new ErroDTO("Erro", HttpStatus.PAYLOAD_TOO_LARGE.value(),
                        "Arquivo excede o tamanho máximo permitido."));
    }

    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    public ResponseEntity<ErroDTO> handleIntegridade() {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErroDTO("Conflito", HttpStatus.CONFLICT.value(),
                        "Erro de integridade de dados (possível duplicidade)."));
    }
}
