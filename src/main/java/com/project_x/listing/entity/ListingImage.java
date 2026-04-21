package com.project_x.listing.houseowners.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "listing_images")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ListingImage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "listing_id", nullable = false)
    private Listing listing;

    @Column(name = "public_id", nullable = false)
    private String publicId;

    @Column(name = "url", nullable = false)
    private String url; // optimizedUrl for display

    @Column(name = "resource_type", nullable = false)
    private String resourceType; // image, video, raw

    @Column(name = "format")
    private String format; // jpg, png, webp

    @Column(nullable = false)
    private Integer position;
}