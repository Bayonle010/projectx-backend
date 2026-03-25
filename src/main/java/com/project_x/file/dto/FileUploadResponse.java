package com.project_x.file.dto;

import lombok.Builder;

@Builder
public record FileUploadResponse(
        String publicId,
        String originalUrl,
        String optimizedUrl,
        String resourceType,
        String format
) {
}
