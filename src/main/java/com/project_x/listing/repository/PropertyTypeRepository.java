package com.project_x.listing.repository;

import com.project_x.listing.entity.PropertyType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PropertyTypeRepository
        extends JpaRepository<PropertyType, UUID> {

    Optional<PropertyType> findByIdAndActiveTrue(UUID id);

    boolean existsByCodeIgnoreCase(String code);

    boolean existsByNameIgnoreCase(String name);
}