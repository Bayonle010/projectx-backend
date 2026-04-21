package com.project_x.listing.houseowners.dto.response;

import lombok.Builder;

import java.util.UUID;

@Builder
public record AmenitiesResponse(
        UUID id,
        String name,
        String imageUrl,
        String imagePublicId
) {
}
