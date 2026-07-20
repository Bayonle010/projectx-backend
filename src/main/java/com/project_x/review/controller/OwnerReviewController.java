package com.project_x.review.controller;

import com.project_x.core.paginationhelper.PaginationAdapters;
import com.project_x.core.response.ApiResponse;
import com.project_x.core.response.ResponseUtil;
import com.project_x.core.security.model.AuthenticationIdentity;
import com.project_x.review.dto.response.OwnerPropertyReviewResponse;
import com.project_x.review.dto.response.OwnerReviewDashboardResponse;
import com.project_x.review.service.PropertyReviewQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/owner/reviews")
@RequiredArgsConstructor
public class OwnerReviewController {

    private final PropertyReviewQueryService queryService;

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse> getDashboard(
            @RequestAttribute("AUTH_IDENTITY")
            AuthenticationIdentity auth
    ) {
        OwnerReviewDashboardResponse response =
                queryService.getOwnerDashboard(auth);

        return ResponseEntity.status(HttpStatus.OK).body(
                ResponseUtil.success(
                        0,
                        "Review dashboard retrieved",
                        "Review dashboard retrieved successfully",
                        response,
                        null
                )
        );
    }

    @GetMapping("/properties")
    public ResponseEntity<ApiResponse> getProperties(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") Long page,
            @RequestParam(defaultValue = "10") Long pageSize,
            @RequestAttribute("AUTH_IDENTITY")
            AuthenticationIdentity auth
    ) {
        Page<OwnerPropertyReviewResponse> response =
                queryService.getOwnerProperties(
                        search,
                        page,
                        pageSize,
                        auth
                );

        return ResponseEntity.status(HttpStatus.OK).body(
                ResponseUtil.success(
                        0,
                        "Properties retrieved",
                        "Property review summaries retrieved successfully",
                        response.getContent(),
                        PaginationAdapters.toMeta(response)
                )
        );
    }
}