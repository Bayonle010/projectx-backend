package com.project_x.review.controller;

import com.project_x.core.response.ApiResponse;
import com.project_x.core.response.ResponseUtil;
import com.project_x.core.security.model.AuthenticationIdentity;
import com.project_x.review.dto.response.ReviewCommentLikeResponse;
import com.project_x.review.service.ReviewCommentLikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reviews/comments")
@RequiredArgsConstructor
public class ReviewCommentLikeController {

    private final ReviewCommentLikeService commentLikeService;

    @PutMapping("/{commentId}/likes/me")
    public ResponseEntity<ApiResponse> like(
            @PathVariable UUID commentId,
            @RequestAttribute("AUTH_IDENTITY")
            AuthenticationIdentity auth
    ) {
        ReviewCommentLikeResponse response =
                commentLikeService.like(commentId, auth);

        return ResponseEntity.ok(
                ResponseUtil.success(
                        0,
                        "Comment liked",
                        "Review comment liked successfully",
                        response,
                        null
                )
        );
    }

    @DeleteMapping("/{commentId}/likes/me")
    public ResponseEntity<ApiResponse> unlike(
            @PathVariable UUID commentId,
            @RequestAttribute("AUTH_IDENTITY")
            AuthenticationIdentity auth
    ) {
        ReviewCommentLikeResponse response =
                commentLikeService.unlike(commentId, auth);

        return ResponseEntity.ok(
                ResponseUtil.success(
                        0,
                        "Comment unliked",
                        "Review comment unliked successfully",
                        response,
                        null
                )
        );
    }
}
