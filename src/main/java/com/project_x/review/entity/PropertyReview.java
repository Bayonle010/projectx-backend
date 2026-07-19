package com.project_x.review.entity;

import com.project_x.listing.entity.Listing;
import com.project_x.review.enums.ReviewStatus;
import com.project_x.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "property_reviews",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_property_review_listing_reviewer",
                        columnNames = {"listing_id", "reviewer_id"}
                )
        },
        indexes = {
                @Index(
                        name = "idx_property_review_listing_status_created",
                        columnList = "listing_id, status, created_at"
                ),
                @Index(
                        name = "idx_property_review_reviewer",
                        columnList = "reviewer_id"
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PropertyReview {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "listing_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_property_review_listing")
    )
    private Listing listing;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "reviewer_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_property_review_reviewer")
    )
    private User reviewer;

    @Column(name = "overall_rating", nullable = false)
    private Integer overallRating;

    @Column(name = "cleanliness_rating", nullable = false)
    private Integer cleanlinessRating;

    @Column(name = "communication_rating", nullable = false)
    private Integer communicationRating;

    @Column(name = "accuracy_rating", nullable = false)
    private Integer accuracyRating;

    @Column(name = "value_for_money_rating", nullable = false)
    private Integer valueForMoneyRating;

    @Column(name = "review_text", columnDefinition = "TEXT")
    private String reviewText;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private ReviewStatus status = ReviewStatus.ACTIVE;

    /*
     * Prevents one update from silently overwriting another update.
     */
    @Version
    private Long version;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}