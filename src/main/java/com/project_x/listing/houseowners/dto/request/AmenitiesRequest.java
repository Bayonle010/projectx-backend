package com.project_x.listing.houseowners.dto.request;

import lombok.Builder;

@Builder
public record AmenitiesRequest(
        String name,
        String imageUrl,
        String imagePublicId
) {
}
