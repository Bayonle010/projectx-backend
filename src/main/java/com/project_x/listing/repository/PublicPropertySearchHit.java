package com.project_x.listing.repository;

import com.project_x.listing.enums.LocationMatchLevel;

import java.util.UUID;

public record PublicPropertySearchHit(
        UUID listingId,
        Long distanceMeters,
        LocationMatchLevel locationMatchLevel
) {
}
