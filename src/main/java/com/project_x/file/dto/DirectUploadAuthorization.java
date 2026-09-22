package com.project_x.file.dto;

import java.util.UUID;

public record DirectUploadAuthorization(
        UUID mediaId,
        String uploadUrl,
        String cloudName,
        String apiKey,
        long timestamp,
        String publicId,
        String uploadPreset,
        String overwrite,
        String signature,
        String resourceType,
        long maximumBytes,
        int maximumDurationSeconds
) {}
