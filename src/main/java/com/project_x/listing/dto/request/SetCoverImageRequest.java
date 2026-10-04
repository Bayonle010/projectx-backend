package com.project_x.listing.dto.request;

import jakarta.validation.constraints.NotBlank;

public record SetCoverImageRequest(
        @NotBlank(message = "Image public ID is required")
        String imagePublicId
) {
}
