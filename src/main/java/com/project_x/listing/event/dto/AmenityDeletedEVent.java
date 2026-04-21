package com.project_x.listing.event.dto;

import lombok.Builder;

@Builder
public record AmenityDeletedEVent(
        String publicId,
        String resourceType
) {
}
