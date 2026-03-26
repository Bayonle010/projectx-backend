package com.project_x.file.dto;

import jakarta.validation.constraints.NotBlank;

public record FileDeleteRequest(
        @NotBlank(message = "publicId cannot be blank")
        String publicId,

        @NotBlank(message = "resourceType cannot be blank")
        String resourceType
) {
}
