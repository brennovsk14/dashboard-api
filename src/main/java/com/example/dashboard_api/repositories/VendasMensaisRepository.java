package com.example.dashboard_api.repositories;

import com.example.dashboard_api.models.entities.VendasMensais;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VendasMensaisRepository extends JpaRepository<VendasMensais, Long> {
}
