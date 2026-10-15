package com.example.dashboard_api.services.VendasPorRegiao;

import com.example.dashboard_api.dtos.VendasPorRegiao.VendasPorRegiaoResponseDTO;
import com.example.dashboard_api.repositories.VendasPorRegiaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VendasPorRegiaoService {

    private final VendasPorRegiaoRepository repository;

    public Page<VendasPorRegiaoResponseDTO> listar(int pagina, int tamanho) {
        Pageable pageable = PageRequest.of(Math.max(0, pagina - 1), tamanho);

        return repository.findAll(pageable)
                .map(v -> new VendasPorRegiaoResponseDTO(
                        v.getUuid(), v.getRegiao(), v.getClientes(), v.getPedidos(),
                        v.getReceita(), v.getTicketMedio(), v.getSatisfacao()
                ));
    }
}
