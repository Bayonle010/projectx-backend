package com.project_x.listing.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreatePropertyTypeRequest(

        @NotBlank(message = "Property type name is required")
        @Size(
                max = 100,
                message = "Property type name cannot exceed 100 characters"
        )
        String name,

        @Size(
                max = 500,
                message = "Description cannot exceed 500 characters"
        )
        String description
) {
}