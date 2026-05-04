package com.project_x.listing.repository;

import com.project_x.listing.entity.Listing;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ListingRepository extends JpaRepository<Listing,UUID> {
    Optional<Listing> findByIdAndOwnerId(UUID id, UUID ownerId);
}
