package com.project_x.listing.dto.response;

import lombok.Builder;

import java.util.UUID;

@Builder
public record PublicPropertyOwnerResponse(
        UUID id,
        String firstName,
        String lastName,
        String avatar
) {
}
