package com.project_x.listing.dto.response;

import lombok.Builder;

@Builder
public record ImageResponse(
        String url,
        Integer position
) {
}
