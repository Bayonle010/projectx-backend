package com.project_x.listing.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateWaterSourceRequest(

        @NotBlank(message = "Water source name is required")
        @Size(
                max = 100,
                message = "Water source name cannot exceed 100 characters"
        )
        String name,

        @Size(
                max = 500,
                message = "Description cannot exceed 500 characters"
        )
        String description
) {
}