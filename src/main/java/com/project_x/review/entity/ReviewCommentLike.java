package com.project_x.review.entity;

import com.project_x.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "review_comment_likes",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_review_comment_like_comment_user",
                        columnNames = {"comment_id", "user_id"}
                )
        },
        indexes = {
                @Index(
                        name = "idx_review_comment_like_comment",
                        columnList = "comment_id"
                ),
                @Index(
                        name = "idx_review_comment_like_user",
                        columnList = "user_id"
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewCommentLike {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "comment_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_review_comment_like_comment"
            )
    )
    private ReviewComment comment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_review_comment_like_user"
            )
    )
    private User user;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
