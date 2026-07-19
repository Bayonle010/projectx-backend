package com.project_x.review.entity;

import com.project_x.listing.entity.Listing;
import com.project_x.review.enums.ReviewStatus;
import com.project_x.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
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
                        name = "idx_property_review_listing",
                        columnList = "listing_id"
                ),
                @Index(
                        name = "idx_property_review_reviewer",
                        columnList = "reviewer_id"
                ),
                @Index(
                        name = "idx_property_review_listing_created_at",
                        columnList = "listing_id, created_at"
                ),
                @Index(
                        name = "idx_property_review_status",
                        columnList = "status"
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

    /**
     * Property being reviewed.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "listing_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_property_review_listing")
    )
    private Listing listing;

    /**
     * User who submitted the review.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "reviewer_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_property_review_reviewer")
    )
    private User reviewer;

    /**
     * Main star rating displayed on the review card.
     */
    @Min(1)
    @Max(5)
    @Column(name = "overall_rating", nullable = false)
    private Integer overallRating;

    /**
     * Ratings used for the rating breakdown in the UI.
     */
    @Min(1)
    @Max(5)
    @Column(name = "cleanliness_rating", nullable = false)
    private Integer cleanlinessRating;

    @Min(1)
    @Max(5)
    @Column(name = "communication_rating", nullable = false)
    private Integer communicationRating;

    @Min(1)
    @Max(5)
    @Column(name = "accuracy_rating", nullable = false)
    private Integer accuracyRating;

    @Min(1)
    @Max(5)
    @Column(name = "value_for_money_rating", nullable = false)
    private Integer valueForMoneyRating;

    /**
     * Written review is optional.
     */
    @Size(max = 5000)
    @Column(name = "review_text", columnDefinition = "TEXT")
    private String reviewText;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private ReviewStatus status = ReviewStatus.ACTIVE;

    /**
     * Comments made under this review.
     */
    @OneToMany(
            mappedBy = "review",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private List<ReviewComment> comments = new ArrayList<>();

    /**
     * Users who liked this review.
     */
    @OneToMany(
            mappedBy = "review",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private List<ReviewLike> likes = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}