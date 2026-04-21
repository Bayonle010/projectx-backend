package com.project_x.listing.houseowners.dto.response;

import lombok.Builder;

@Builder
public record ImageResponse(
        String url,
        Integer position
) {
}
