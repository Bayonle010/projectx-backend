package com.project_x.listing.repository;

import com.project_x.listing.entity.Listing;
import com.project_x.listing.enums.ListingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ListingRepository extends JpaRepository<Listing, UUID> {

    @EntityGraph(attributePaths = {
            "state",
            "lga",
            "amenities",
            "images",
            "owner",
            "propertyType",
            "waterSource"
    })
    Optional<Listing> findByIdAndOwnerId(
            UUID id,
            UUID ownerId
    );

    @EntityGraph(attributePaths = {
            "state",
            "lga",
            "amenities",
            "images",
            "owner",
            "propertyType",
            "waterSource"
    })
    Optional<Listing> findWithDetailsByIdAndOwnerId(
            UUID id,
            UUID ownerId
    );

    @EntityGraph(attributePaths = {
            "state",
            "lga",
            "amenities",
            "images",
            "owner",
            "propertyType",
            "waterSource"
    })
    Page<Listing> findByOwnerId(
            UUID ownerId,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {
            "state",
            "lga",
            "amenities",
            "images",
            "owner",
            "propertyType",
            "waterSource"
    })
    Page<Listing> findByOwnerIdAndStatus(
            UUID ownerId,
            ListingStatus status,
            Pageable pageable
    );

    boolean existsByPropertyType_Id(UUID propertyTypeId);
}