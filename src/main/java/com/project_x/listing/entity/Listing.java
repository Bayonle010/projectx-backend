package com.project_x.listing.entity;

import com.project_x.adress.entity.Lga;
import com.project_x.adress.entity.State;
import com.project_x.listing.enums.*;
import com.project_x.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

@Entity
@Table(name = "listings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Listing {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private ListingRelationshipType relationshipType; // OWNER, AGENT

    @Column(nullable = true)
    private Integer bedroomCount; // 0 means studio

    @Column(nullable = true)
    private Integer bathroomCount;

    @Column(nullable = true)
    private Integer toiletCount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true, length = 30)
    private PropertyCondition propertyCondition;

    @Column(nullable = true)
    private Integer unitCount;

    @Column(nullable = true, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = true)
    private Boolean parkingAvailable;

    @Column(nullable = true)
    private Boolean fencedOrGated;

    @Column(nullable = true)
    private Boolean renovated;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true, length = 30)
    private FurnishingStatus furnishingStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "state_id", nullable = true)
    private State state;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lga_id", nullable = true)
    private Lga lga;

    @Column(nullable = true, length = 500)
    private String addressLine;

    @Column(name = "neighbourhood", length = 150)
    private String neighbourhood;

    @Column(length = 255)
    private String landmark;

    @Column
    private Double latitude;

    @Column
    private Double longitude;

    @Column(length = 255)
    private String placeId;

    @Column(nullable = true)
    private Boolean shareAddressWithSeekers;

    @Column(nullable = true, precision = 19, scale = 2)
    private BigDecimal rentAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true, length = 20)
    private RentPaymentFrequency rentPaymentFrequency;

    @Column(precision = 19, scale = 2)
    private BigDecimal agencyFee;

    @Column(precision = 19, scale = 2)
    private BigDecimal legalAgreementFee;

    @Column(precision = 19, scale = 2)
    private BigDecimal cautionFee;

    @Column(precision = 19, scale = 2)
    private BigDecimal serviceCharge;

    @Column(nullable = true)
    private String proofOfOwnershipUrl;

    @ManyToMany
    @JoinTable(
            name = "listing_amenities",
            joinColumns = @JoinColumn(name = "listing_id"),
            inverseJoinColumns = @JoinColumn(name = "amenity_id")
    )
    private Set<Amenity> amenities = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "property_type_id",
            foreignKey = @ForeignKey(name = "fk_listing_property_type")
    )
    private PropertyType propertyType;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "listing_water_sources",
            joinColumns = @JoinColumn(
                    name = "listing_id",
                    foreignKey = @ForeignKey(
                            name = "fk_listing_water_sources_listing"
                    )
            ),
            inverseJoinColumns = @JoinColumn(
                    name = "water_source_id",
                    foreignKey = @ForeignKey(
                            name = "fk_listing_water_sources_water_source"
                    )
            ),
            uniqueConstraints = @UniqueConstraint(
                    name = "uk_listing_water_source",
                    columnNames = {
                            "listing_id",
                            "water_source_id"
                    }
            )
    )
    @Builder.Default
    private Set<WaterSource> waterSources = new HashSet<>();

    @OneToMany(mappedBy = "listing", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ListingImage> images = new ArrayList<>();

    @Column(name = "video_url", nullable = true)
    private String videoUrl;

    @Column(name = "video_public_id", nullable = true)
    private String videoPublicId;

    @CreationTimestamp
    @Column(nullable = true, updatable = false)
    private Instant createdAt;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private ListingStatus status = ListingStatus.DRAFT;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;
}
