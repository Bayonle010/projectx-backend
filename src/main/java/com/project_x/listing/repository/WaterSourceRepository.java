package com.project_x.listing.repository;

import com.project_x.listing.entity.WaterSource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface WaterSourceRepository
        extends JpaRepository<WaterSource, UUID> {

    Optional<WaterSource> findByIdAndActiveTrue(UUID id);

    boolean existsByCodeIgnoreCase(String code);

    boolean existsByNameIgnoreCase(String name);
}