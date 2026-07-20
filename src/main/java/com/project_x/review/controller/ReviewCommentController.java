package com.project_x.review.controller;

import com.project_x.core.paginationhelper.PaginationAdapters;
import com.project_x.core.response.ApiResponse;
import com.project_x.core.response.ResponseUtil;
import com.project_x.core.security.model.AuthenticationIdentity;
import com.project_x.review.dto.request.SaveReviewCommentRequest;
import com.project_x.review.dto.response.ReviewCommentResponse;
import com.project_x.review.service.ReviewCommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewCommentController {

    private final ReviewCommentService commentService;

    @PostMapping("/{reviewId}/comments")
    public ResponseEntity<ApiResponse> createComment(
            @PathVariable UUID reviewId,
            @Valid @RequestBody SaveReviewCommentRequest request,
            @RequestAttribute("AUTH_IDENTITY")
            AuthenticationIdentity auth
    ) {
        ReviewCommentResponse response =
                commentService.create(
                        reviewId,
                        request,
                        auth
                );

        return ResponseEntity.status(HttpStatus.OK).body(
                ResponseUtil.success(
                        0,
                        "Comment saved",
                        "Review comment saved successfully",
                        response,
                        null
                )
        );
    }

    @GetMapping("/{reviewId}/comments")
    public ResponseEntity<ApiResponse> getComments(
            @PathVariable UUID reviewId,
            @RequestParam(defaultValue = "0") Long page,
            @RequestParam(defaultValue = "20") Long pageSize
    ) {
        Page<ReviewCommentResponse> response =
                commentService.getComments(
                        reviewId,
                        page,
                        pageSize
                );

        return ResponseEntity.status(HttpStatus.OK).body(
                ResponseUtil.success(
                        0,
                        "Comments retrieved",
                        "Review comments retrieved successfully",
                        response.getContent(),
                        PaginationAdapters.toMeta(response)
                )
        );
    }
}