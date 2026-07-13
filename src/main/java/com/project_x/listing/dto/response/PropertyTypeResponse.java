package com.project_x.listing.dto.response;

import java.time.Instant;
import java.util.UUID;

public record PropertyTypeResponse(
        UUID id,
        String code,
        String name,
        String description,
        boolean active,
        Instant createdAt,
        Instant updatedAt
) {
}