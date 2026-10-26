package com.project_x.listing.dto.response;

import lombok.Builder;

@Builder
public record PublicPropertyImageResponse(
        String url,
        Integer position,
        String format
) {
}
