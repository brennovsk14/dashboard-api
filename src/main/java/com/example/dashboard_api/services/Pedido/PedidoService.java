package com.example.dashboard_api.services.Pedido;

import com.example.dashboard_api.dtos.Pedido.PedidoResponseDTO;
import com.example.dashboard_api.repositories.PedidoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PedidoService {

    private final PedidoRepository repository;

    public Page<PedidoResponseDTO> listar(int pagina, int tamanho) {
        Pageable pageable = PageRequest.of(Math.max(0, pagina - 1), tamanho);

        return repository.findAll(pageable)
                .map(p -> new PedidoResponseDTO(
                        p.getUuid(), p.getData(), p.getRegiao(), p.getCategoria(),
                        p.getQuantidade(), p.getValorUnitario(), p.getValorTotal(), p.getAvaliacao()
                ));
    }
}
