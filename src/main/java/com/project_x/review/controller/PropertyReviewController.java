package com.project_x.review.controller;

import com.project_x.core.paginationhelper.PaginationAdapters;
import com.project_x.core.response.ApiResponse;
import com.project_x.core.response.ResponseUtil;
import com.project_x.core.security.model.AuthenticationIdentity;
import com.project_x.review.dto.request.SavePropertyReviewRequest;
import com.project_x.review.dto.response.ListingRatingSummaryResponse;
import com.project_x.review.dto.response.PropertyReviewResponse;
import com.project_x.review.service.PropertyReviewCommandService;
import com.project_x.review.service.PropertyReviewQueryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class PropertyReviewController {

    private final PropertyReviewCommandService commandService;
    private final PropertyReviewQueryService queryService;

    @PostMapping("/listings/{listingId}")
    public ResponseEntity<ApiResponse> create(
            @PathVariable UUID listingId,
            @Valid @RequestBody SavePropertyReviewRequest request,
            @RequestAttribute("AUTH_IDENTITY")
            AuthenticationIdentity auth
    ) {
        PropertyReviewResponse response =
                commandService.create(
                        listingId,
                        request,
                        auth
                );

        return ResponseEntity.status(HttpStatus.OK).body(
                ResponseUtil.success(
                        0,
                        "Review saved",
                        "Property review saved successfully",
                        response,
                        null
                )
        );
    }

    @GetMapping("/listings/{listingId}")
    public ResponseEntity<ApiResponse> getListingReviews(
            @PathVariable UUID listingId,
            @RequestParam(defaultValue = "0") Long page,
            @RequestParam(defaultValue = "20") Long pageSize,
            @RequestAttribute("AUTH_IDENTITY")
            AuthenticationIdentity auth
    ) {
        Page<PropertyReviewResponse> response =
                queryService.getListingReviews(
                        listingId,
                        page,
                        pageSize,
                        auth
                );

        return ResponseEntity.status(HttpStatus.OK).body(
                ResponseUtil.success(
                        0,
                        "Reviews retrieved",
                        "Property reviews retrieved successfully",
                        response.getContent(),
                        PaginationAdapters.toMeta(response)
                )
        );
    }

    @GetMapping("/listings/{listingId}/summary")
    public ResponseEntity<ApiResponse> getListingSummary(
            @PathVariable UUID listingId
    ) {
        ListingRatingSummaryResponse response =
                queryService.getListingSummary(listingId);

        return ResponseEntity.status(HttpStatus.OK).body(
                ResponseUtil.success(
                        0,
                        "Review summary retrieved",
                        "Property review summary retrieved successfully",
                        response,
                        null
                )
        );
    }
}