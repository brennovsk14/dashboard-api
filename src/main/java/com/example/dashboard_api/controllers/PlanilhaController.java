package com.example.dashboard_api.controllers;

import com.example.dashboard_api.controllers.docs.PlanilhaControllerDocs;
import com.example.dashboard_api.dtos.Planilha.PlanilhaResponseDTO;
import com.example.dashboard_api.dtos.Planilha.PlanilhaResumoDTO;
import com.example.dashboard_api.services.Planilha.PlanilhaService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/planilhas")
@RequiredArgsConstructor
public class PlanilhaController implements PlanilhaControllerDocs {

    private final PlanilhaService service;

    @PostMapping(value = "/post", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PlanilhaResumoDTO> criar(
            @RequestParam String nome,
            @RequestPart("arquivo") MultipartFile arquivo) {

        return ResponseEntity.status(HttpStatus.CREATED).body(service.salvar(nome, arquivo));
    }

    @GetMapping("/get")
    public ResponseEntity<List<PlanilhaResumoDTO>> listar(
            @RequestParam(defaultValue = "1") int pagina,
            @RequestParam(name = "page-size", defaultValue = "20") int tamanho) {

        Page<PlanilhaResumoDTO> page = service.listar(pagina, tamanho);

        HttpHeaders headers = new HttpHeaders();
        headers.add("x-total-content", String.valueOf(page.getTotalElements()));
        headers.add("x-total-pages", String.valueOf(page.getTotalPages()));
        headers.add("x-pagina-atual", String.valueOf(page.getNumber() + 1));

        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    @GetMapping("/{uuid}/get")
    public ResponseEntity<PlanilhaResponseDTO> buscar(@PathVariable UUID uuid) {
        return ResponseEntity.ok(service.buscarPorUuid(uuid));
    }
}
