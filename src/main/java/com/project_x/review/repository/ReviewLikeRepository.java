package com.project_x.review.repository;

import com.project_x.review.entity.ReviewLike;
import com.project_x.review.projection.ReviewCountProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface ReviewLikeRepository extends JpaRepository<ReviewLike, UUID> {

    boolean existsByReview_IdAndUser_Id(
            UUID reviewId,
            UUID userId
    );

    long countByReview_Id(UUID reviewId);

    long deleteByReview_IdAndUser_Id(
            UUID reviewId,
            UUID userId
    );

    @Query("""
            SELECT
                reviewLike.review.id AS reviewId,
                COUNT(reviewLike.id) AS total
            FROM ReviewLike reviewLike
            WHERE reviewLike.review.id IN :reviewIds
            GROUP BY reviewLike.review.id
            """)
    List<ReviewCountProjection> countLikesByReviewIds(
            @Param("reviewIds") Collection<UUID> reviewIds
    );

    @Query("""
            SELECT reviewLike.review.id
            FROM ReviewLike reviewLike
            WHERE reviewLike.user.id = :userId
              AND reviewLike.review.id IN :reviewIds
            """)
    List<UUID> findLikedReviewIds(
            @Param("userId") UUID userId,
            @Param("reviewIds") Collection<UUID> reviewIds
    );

    /*
     * PostgreSQL idempotent insert.
     *
     * Multiple requests attempting to like the same review will not create
     * duplicate records.
     */
    @Modifying
    @Query(
            value = """
                    INSERT INTO review_likes (
                        id,
                        review_id,
                        user_id,
                        created_at
                    )
                    VALUES (
                        :id,
                        :reviewId,
                        :userId,
                        CURRENT_TIMESTAMP
                    )
                    ON CONFLICT (review_id, user_id) DO NOTHING
                    """,
            nativeQuery = true
    )
    int insertIfAbsent(
            @Param("id") UUID id,
            @Param("reviewId") UUID reviewId,
            @Param("userId") UUID userId
    );
}