package com.project_x.listing.service;

import com.project_x.listing.enums.LocationVerificationStatus;

import java.time.Instant;

public interface ListingLocationVerificationService {

    VerificationResult verify(
            String placeId,
            Double suppliedLatitude,
            Double suppliedLongitude
    );

    record VerificationResult(
            Double latitude,
            Double longitude,
            LocationVerificationStatus status,
            Instant verifiedAt
    ) {
    }
}
