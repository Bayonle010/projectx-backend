package com.project_x.review.repository;

import com.project_x.review.entity.ReviewComment;
import com.project_x.review.enums.ReviewCommentStatus;
import com.project_x.review.projection.ReviewCountProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface ReviewCommentRepository
        extends JpaRepository<ReviewComment, UUID> {

    @EntityGraph(attributePaths = {"author"})
    Page<ReviewComment> findByReview_IdAndStatusOrderByCreatedAtAsc(
            UUID reviewId,
            ReviewCommentStatus status,
            Pageable pageable
    );

    @Query("""
            SELECT
                comment.review.id AS reviewId,
                COUNT(comment.id) AS total
            FROM ReviewComment comment
            WHERE comment.review.id IN :reviewIds
              AND comment.status = :status
            GROUP BY comment.review.id
            """)
    List<ReviewCountProjection> countCommentsByReviewIds(
            @Param("reviewIds") Collection<UUID> reviewIds,
            @Param("status") ReviewCommentStatus status
    );
}