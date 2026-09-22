package com.project_x.file.dto;

import lombok.Builder;

import java.util.UUID;

@Builder
public record FileUploadResponse(
        UUID mediaId,
        String publicId,
        String originalUrl,
        String optimizedUrl,
        String resourceType,
        String format
) {
}
