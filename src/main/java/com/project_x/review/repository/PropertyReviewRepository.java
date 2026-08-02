package com.project_x.review.repository;

import com.project_x.review.entity.PropertyReview;
import com.project_x.review.enums.ReviewStatus;
import com.project_x.review.projection.ListingRatingSummaryProjection;
import com.project_x.review.projection.OwnerPropertyReviewProjection;
import com.project_x.review.projection.OwnerReviewAggregateProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface PropertyReviewRepository
        extends JpaRepository<PropertyReview, UUID> {

    boolean existsByListing_IdAndReviewer_Id(
            UUID listingId,
            UUID reviewerId
    );

    @EntityGraph(attributePaths = {"reviewer"})
    Page<PropertyReview> findByListing_IdAndStatusOrderByCreatedAtDesc(
            UUID listingId,
            ReviewStatus status,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {"reviewer", "listing"})
    Optional<PropertyReview> findByIdAndStatus(
            UUID id,
            ReviewStatus status
    );

    @Query("""
            SELECT
                COUNT(review.id) AS totalReviews,
                COALESCE(AVG(review.overallRating), 0.0) AS averageRating,
                COALESCE(AVG(review.cleanlinessRating), 0.0) AS cleanlinessRating,
                COALESCE(AVG(review.communicationRating), 0.0) AS communicationRating,
                COALESCE(AVG(review.accuracyRating), 0.0) AS accuracyRating,
                COALESCE(AVG(review.valueForMoneyRating), 0.0) AS valueForMoneyRating
            FROM PropertyReview review
            WHERE review.listing.id = :listingId
              AND review.status = :status
            """)
    ListingRatingSummaryProjection getListingRatingSummary(
            @Param("listingId") UUID listingId,
            @Param("status") ReviewStatus status
    );

    @Query("""
            SELECT
                COUNT(review.id) AS totalReviews,
                COALESCE(AVG(review.overallRating), 0.0) AS averageRating,
                COALESCE(AVG(review.cleanlinessRating), 0.0) AS cleanlinessRating,
                COALESCE(AVG(review.communicationRating), 0.0) AS communicationRating,
                COALESCE(AVG(review.accuracyRating), 0.0) AS accuracyRating,
                COALESCE(AVG(review.valueForMoneyRating), 0.0) AS valueForMoneyRating
            FROM PropertyReview review
            WHERE review.listing.owner.id = :ownerId
              AND review.status = :status
            """)
    OwnerReviewAggregateProjection getOwnerReviewSummary(
            @Param("ownerId") UUID ownerId,
            @Param("status") ReviewStatus status
    );

    @Query(
            value = """
                SELECT
                    listing.id AS listingId,
                    listing.addressLine AS address,
                    COUNT(review.id) AS totalReviews,
                    COALESCE(AVG(review.overallRating), 0.0) AS averageRating
                FROM Listing listing
                LEFT JOIN PropertyReview review
                    ON review.listing = listing
                    AND review.status = :reviewStatus
                WHERE listing.owner.id = :ownerId
                  AND (
                      :search = ''
                      OR LOWER(listing.addressLine)
                         LIKE CONCAT('%', LOWER(:search), '%')
                  )
                GROUP BY listing.id, listing.addressLine, listing.createdAt
                ORDER BY listing.createdAt DESC
                """,
            countQuery = """
                SELECT COUNT(listing.id)
                FROM Listing listing
                WHERE listing.owner.id = :ownerId
                  AND (
                      :search = ''
                      OR LOWER(listing.addressLine)
                         LIKE CONCAT('%', LOWER(:search), '%')
                  )
                """
    )
    Page<OwnerPropertyReviewProjection> findOwnerPropertyReviewSummary(
            @Param("ownerId") UUID ownerId,
            @Param("reviewStatus") ReviewStatus reviewStatus,
            @Param("search") String search,
            Pageable pageable
    );
}