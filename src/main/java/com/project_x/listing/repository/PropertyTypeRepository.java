package com.project_x.listing.repository;

import com.project_x.listing.entity.PropertyType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PropertyTypeRepository
        extends JpaRepository<PropertyType, UUID> {

    boolean existsByNameIgnoreCase(String name);

    boolean existsByCodeIgnoreCase(String code);

    boolean existsByNameIgnoreCaseAndIdNot(
            String name,
            UUID id
    );

    Optional<PropertyType> findByIdAndActiveTrue(UUID id);

    List<PropertyType> findAllByActiveTrueOrderByNameAsc();

    List<PropertyType> findAllByOrderByNameAsc();
}