package com.project_x.listing.houseowners.event.dto;

import lombok.Builder;

@Builder
public record AmenityDeletedEVent(
        String publicId,
        String resourceType
) {
}
