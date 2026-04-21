package com.project_x.adress.repository;

import com.project_x.adress.entity.Lga;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LgaRepository extends JpaRepository<Lga, UUID> {
    List<Lga> findByStateIdOrderByNameAsc(UUID stateId);
}
