package com.example.dashboard_api.services.VendasMensais;

import com.example.dashboard_api.dtos.VendasMensais.VendasMensaisResponseDTO;
import com.example.dashboard_api.repositories.VendasMensaisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VendasMensaisService {

    private final VendasMensaisRepository repository;

    public Page<VendasMensaisResponseDTO> listar(int pagina, int tamanho) {
        Pageable pageable = PageRequest.of(Math.max(0, pagina - 1), tamanho);

        return repository.findAll(pageable)
                .map(v -> new VendasMensaisResponseDTO(
                        v.getUuid(), v.getMes(), v.getEletronicos(),
                        v.getVestuario(), v.getAlimentos(), v.getLivros()
                ));
    }
}
