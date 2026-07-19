package com.project_x.review.entity;

import com.project_x.review.enums.ReviewCommentStatus;
import com.project_x.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "review_comments",
        indexes = {
                @Index(
                        name = "idx_review_comment_review_status_created",
                        columnList = "review_id, status, created_at"
                ),
                @Index(
                        name = "idx_review_comment_author",
                        columnList = "author_id"
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewComment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "review_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_review_comment_review")
    )
    private PropertyReview review;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "author_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_review_comment_author")
    )
    private User author;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private ReviewCommentStatus status = ReviewCommentStatus.ACTIVE;

    @Version
    private Long version;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}