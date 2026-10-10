package com.project_x.review.repository;

import com.project_x.review.entity.ReviewCommentLike;
import com.project_x.review.projection.CommentCountProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface ReviewCommentLikeRepository
        extends JpaRepository<ReviewCommentLike, UUID> {

    long countByComment_Id(UUID commentId);

    long deleteByComment_IdAndUser_Id(
            UUID commentId,
            UUID userId
    );

    @Query("""
            SELECT
                commentLike.comment.id AS commentId,
                COUNT(commentLike.id) AS total
            FROM ReviewCommentLike commentLike
            WHERE commentLike.comment.id IN :commentIds
            GROUP BY commentLike.comment.id
            """)
    List<CommentCountProjection> countLikesByCommentIds(
            @Param("commentIds") Collection<UUID> commentIds
    );

    @Query("""
            SELECT commentLike.comment.id
            FROM ReviewCommentLike commentLike
            WHERE commentLike.user.id = :userId
              AND commentLike.comment.id IN :commentIds
            """)
    List<UUID> findLikedCommentIds(
            @Param("userId") UUID userId,
            @Param("commentIds") Collection<UUID> commentIds
    );

    @Modifying
    @Query(
            value = """
                    INSERT INTO review_comment_likes (
                        id,
                        comment_id,
                        user_id,
                        created_at
                    )
                    VALUES (
                        :id,
                        :commentId,
                        :userId,
                        CURRENT_TIMESTAMP
                    )
                    ON CONFLICT (comment_id, user_id) DO NOTHING
                    """,
            nativeQuery = true
    )
    int insertIfAbsent(
            @Param("id") UUID id,
            @Param("commentId") UUID commentId,
            @Param("userId") UUID userId
    );
}
