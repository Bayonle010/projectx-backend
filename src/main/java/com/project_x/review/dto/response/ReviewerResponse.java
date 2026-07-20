package com.project_x.review.dto.response;

import java.util.UUID;

public record ReviewerResponse(
        UUID id,
        String name
) {
}
