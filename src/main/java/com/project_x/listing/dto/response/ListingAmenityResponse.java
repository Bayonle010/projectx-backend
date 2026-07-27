package com.project_x.listing.dto.response;

import lombok.Builder;

import java.util.UUID;

@Builder
public record ListingAmenityResponse(
        UUID id,
        String name
) {
}