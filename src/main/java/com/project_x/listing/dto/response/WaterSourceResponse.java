package com.project_x.listing.dto.response;

import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
public record WaterSourceResponse(
        UUID id,
        String code,
        String name,
        String description,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {
}