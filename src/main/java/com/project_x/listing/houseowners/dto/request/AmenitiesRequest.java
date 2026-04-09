package com.project_x.listing.houseowners.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record AmenitiesRequest(

        @NotBlank(message = "name is required")
        String name,

        @NotBlank(message = "imageUrl is required")
        String imageUrl,

        @NotBlank(message = "imagePublicId is required")
        String imagePublicId
) {
}
