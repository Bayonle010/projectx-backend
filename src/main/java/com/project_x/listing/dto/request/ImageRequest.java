package com.project_x.listing.houseowners.dto.request;

public record ImageRequest(
        String publicId,
        String optimizedUrl,
        String resourceType,
        String format
) {}