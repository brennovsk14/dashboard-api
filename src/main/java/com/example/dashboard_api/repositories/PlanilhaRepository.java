package com.example.dashboard_api.repositories;

import com.example.dashboard_api.models.entities.Planilha;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PlanilhaRepository extends JpaRepository<Planilha, Long> {

    Optional<Planilha> findByUuid(UUID uuid);
}
