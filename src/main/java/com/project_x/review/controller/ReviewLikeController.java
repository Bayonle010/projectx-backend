package com.project_x.review.controller;

import com.project_x.core.response.ApiResponse;
import com.project_x.core.response.ResponseUtil;
import com.project_x.core.security.model.AuthenticationIdentity;
import com.project_x.review.dto.response.ReviewLikeResponse;
import com.project_x.review.service.ReviewLikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewLikeController {

    private final ReviewLikeService likeService;

    @PutMapping("/{reviewId}/likes/me")
    public ResponseEntity<ApiResponse> like(
            @PathVariable UUID reviewId,
            @RequestAttribute("AUTH_IDENTITY")
            AuthenticationIdentity auth
    ) {
        ReviewLikeResponse response =
                likeService.like(reviewId, auth);

        return ResponseEntity.status(HttpStatus.OK).body(
                ResponseUtil.success(
                        0,
                        "Review liked",
                        "Review liked successfully",
                        response,
                        null
                )
        );
    }

    @DeleteMapping("/{reviewId}/likes/me")
    public ResponseEntity<ApiResponse> unlike(
            @PathVariable UUID reviewId,
            @RequestAttribute("AUTH_IDENTITY")
            AuthenticationIdentity auth
    ) {
        ReviewLikeResponse response =
                likeService.unlike(reviewId, auth);

        return ResponseEntity.status(HttpStatus.OK).body(
                ResponseUtil.success(
                        0,
                        "Review unliked",
                        "Review unliked successfully",
                        response,
                        null
                )
        );
    }
}