package com.example.dashboard_api.controllers;

import com.example.dashboard_api.controllers.docs.VendasPorRegiaoControllerDocs;
import com.example.dashboard_api.dtos.VendasPorRegiao.VendasPorRegiaoResponseDTO;
import com.example.dashboard_api.services.VendasPorRegiao.VendasPorRegiaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/vendas-por-regiao")
@RequiredArgsConstructor
public class VendasPorRegiaoController implements VendasPorRegiaoControllerDocs {

    private final VendasPorRegiaoService service;

    @GetMapping("/get")
    public ResponseEntity<List<VendasPorRegiaoResponseDTO>> listar(
            @RequestParam(defaultValue = "1") int pagina,
            @RequestParam(name = "page-size", defaultValue = "20") int tamanho) {

        Page<VendasPorRegiaoResponseDTO> page = service.listar(pagina, tamanho);

        HttpHeaders headers = new HttpHeaders();
        headers.add("x-total-content", String.valueOf(page.getTotalElements()));
        headers.add("x-total-pages", String.valueOf(page.getTotalPages()));
        headers.add("x-pagina-atual", String.valueOf(page.getNumber() + 1));

        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }
}
