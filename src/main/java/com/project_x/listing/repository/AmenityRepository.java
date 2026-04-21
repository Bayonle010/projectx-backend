package com.project_x.listing.houseowners.repository;

import com.project_x.listing.houseowners.entity.Amenity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface AmenityRepository extends JpaRepository<Amenity, UUID> {

    List<Amenity> findAllByIdIn(Set<UUID> amenityIds);
}
