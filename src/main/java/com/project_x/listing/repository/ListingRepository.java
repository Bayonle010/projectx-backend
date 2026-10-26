package com.project_x.listing.repository;

import com.project_x.coverage.projection.StateCoverageCountProjection;
import com.project_x.listing.entity.Listing;
import com.project_x.listing.enums.ListingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ListingRepository
        extends JpaRepository<Listing, UUID> {

    /**
     * Reserves a friendly ID and creates the draft in one atomic statement.
     * PostgreSQL returns zero when any unique value conflicts, allowing the
     * caller to generate another reference without aborting its transaction.
     */
    @Modifying
    @Query(value = """
            INSERT INTO listings (
                id, friendly_id, status, owner_id, created_at, updated_at
            ) VALUES (
                :id, :friendlyId, 'DRAFT', :ownerId, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
            )
            ON CONFLICT DO NOTHING
            """, nativeQuery = true)
    int insertNewDraft(
            @Param("id") UUID id,
            @Param("friendlyId") String friendlyId,
            @Param("ownerId") UUID ownerId
    );

    @EntityGraph(attributePaths = {
            "state",
            "lga",
            "amenities",
            "images",
            "owner",
            "propertyType",
            "waterSources"
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
            "waterSources"
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
            "waterSources"
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
            "waterSources"
    })
    Page<Listing> findByOwnerIdAndStatus(
            UUID ownerId,
            ListingStatus status,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {
            "state",
            "lga",
            "owner",
            "propertyType"
    })
    Page<Listing> findByStatus(ListingStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {
            "state",
            "lga",
            "owner",
            "propertyType"
    })
    Optional<Listing> findByIdAndStatus(
            UUID id,
            ListingStatus status
    );

    boolean existsByPropertyType_Id(UUID propertyTypeId);

    boolean existsByVideoPublicId(String videoPublicId);

    boolean existsByProofOfOwnershipUrl(String proofOfOwnershipUrl);

    boolean existsByWaterSources_Id(UUID waterSourceId);

    long countByOwner_Id(UUID ownerId);

    long countByOwner_IdAndStatus(
            UUID ownerId,
            ListingStatus status
    );

    @Query("""
            SELECT
                listing.state.id AS stateId,
                listing.state.name AS stateName,
                COUNT(listing.id) AS availableListingCount
            FROM Listing listing
            WHERE listing.status = :status
              AND listing.state IS NOT NULL
            GROUP BY listing.state.id, listing.state.name
            ORDER BY listing.state.name ASC
            """)
    List<StateCoverageCountProjection> findStateCoverageByStatus(
            @Param("status") ListingStatus status
    );
}
