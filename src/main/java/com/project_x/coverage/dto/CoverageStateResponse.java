package com.project_x.coverage.dto;

import java.util.UUID;

public record CoverageStateResponse(
        UUID stateId,
        String stateName,
        String displayName,
        String mapKey,
        String imageUrl,
        long availableListingCount
) {
}
