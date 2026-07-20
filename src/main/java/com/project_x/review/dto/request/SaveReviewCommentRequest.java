package com.project_x.review.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SaveReviewCommentRequest(

        @NotBlank(message = "Comment cannot be empty")
        @Size(max = 2000, message = "Comment cannot exceed 2000 characters")
        String content
) {
}