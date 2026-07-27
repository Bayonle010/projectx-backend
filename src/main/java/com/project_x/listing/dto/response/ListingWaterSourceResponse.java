package com.project_x.listing.dto.response;

import lombok.Builder;

import java.util.UUID;

@Builder
public record ListingWaterSourceResponse(
        UUID id,
        String name,
        String code
) {
}