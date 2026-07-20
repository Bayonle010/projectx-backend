package com.project_x.review.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SavePropertyReviewRequest(

        @NotNull(message = "Overall rating is required")
        @Min(value = 1, message = "Overall rating cannot be less than 1")
        @Max(value = 5, message = "Overall rating cannot be greater than 5")
        Integer overallRating,

        @NotNull(message = "Cleanliness rating is required")
        @Min(value = 1, message = "Cleanliness rating cannot be less than 1")
        @Max(value = 5, message = "Cleanliness rating cannot be greater than 5")
        Integer cleanlinessRating,

        @NotNull(message = "Communication rating is required")
        @Min(value = 1, message = "Communication rating cannot be less than 1")
        @Max(value = 5, message = "Communication rating cannot be greater than 5")
        Integer communicationRating,

        @NotNull(message = "Accuracy rating is required")
        @Min(value = 1, message = "Accuracy rating cannot be less than 1")
        @Max(value = 5, message = "Accuracy rating cannot be greater than 5")
        Integer accuracyRating,

        @NotNull(message = "Value for money rating is required")
        @Min(value = 1, message = "Value for money rating cannot be less than 1")
        @Max(value = 5, message = "Value for money rating cannot be greater than 5")
        Integer valueForMoneyRating,

        @Size(max = 3000, message = "Review cannot exceed 3000 characters")
        String reviewText
) {
}