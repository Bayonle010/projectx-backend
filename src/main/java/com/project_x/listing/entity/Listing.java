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
    @Column(nullable = false)
    private ListingRelationshipType relationshipType; // OWNER, AGENT

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private PropertyType propertyType;

    @Column(nullable = false)
    private Integer bedroomCount; // 0 means studio

    @Column(nullable = false)
    private Integer bathroomCount;

    @Column(nullable = false)
    private Integer toiletCount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PropertyCondition propertyCondition;

    @Column(nullable = false)
    private Integer unitCount;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private WaterSource waterSource;

    @Column(nullable = false)
    private Boolean parkingAvailable;

    @Column(nullable = false)
    private Boolean fencedOrGated;

    @Column(nullable = false)
    private Boolean renovated;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private FurnishingStatus furnishingStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "state_id", nullable = false)
    private State state;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lga_id", nullable = false)
    private Lga lga;

    @Column(nullable = false, length = 500)
    private String addressLine;

    @Column(length = 255)
    private String landmark;

    @Column
    private Double latitude;

    @Column
    private Double longitude;

    @Column(length = 255)
    private String placeId;

    @Column(nullable = false)
    private Boolean shareAddressWithSeekers;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal rentAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RentPaymentFrequency rentPaymentFrequency;

    @Column(precision = 19, scale = 2)
    private BigDecimal agencyFee;

    @Column(precision = 19, scale = 2)
    private BigDecimal legalAgreementFee;

    @Column(precision = 19, scale = 2)
    private BigDecimal cautionFee;

    @Column(precision = 19, scale = 2)
    private BigDecimal serviceCharge;

    @Column(nullable = false)
    private String proofOfOwnershipUrl;

    @ManyToMany
    @JoinTable(
            name = "listing_amenities",
            joinColumns = @JoinColumn(name = "listing_id"),
            inverseJoinColumns = @JoinColumn(name = "amenity_id")
    )
    private Set<Amenity> amenities = new HashSet<>();

    @OneToMany(mappedBy = "listing", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ListingImage> images = new ArrayList<>();

    @Column(name = "video_url", nullable = false)
    private String videoUrl;

    @Column(name = "video_public_id", nullable = false)
    private String videoPublicId;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

}
