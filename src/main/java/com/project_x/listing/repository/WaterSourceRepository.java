package com.project_x.listing.repository;

import com.project_x.listing.entity.WaterSource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WaterSourceRepository
        extends JpaRepository<WaterSource, UUID> {

    boolean existsByNameIgnoreCase(String name);

    boolean existsByCodeIgnoreCase(String code);

    boolean existsByNameIgnoreCaseAndIdNot(
            String name,
            UUID id
    );

    Optional<WaterSource> findByIdAndActiveTrue(UUID id);

    List<WaterSource> findAllByActiveTrueOrderByNameAsc();

    List<WaterSource> findAllByOrderByNameAsc();
}