package com.project_x.listing.houseowners.repository;

import com.project_x.listing.houseowners.entity.Amenities;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AmenitiesRepository extends JpaRepository<Amenities, UUID> {

}
