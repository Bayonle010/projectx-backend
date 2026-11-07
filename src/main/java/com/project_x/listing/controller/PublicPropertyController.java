package com.project_x.listing.controller;

import com.project_x.core.paginationhelper.PaginationAdapters;
import com.project_x.core.response.ApiResponse;
import com.project_x.core.response.ResponseUtil;
import com.project_x.listing.dto.request.PublicPropertyFilterRequest;
import com.project_x.listing.dto.response.PublicPropertyDetailsResponse;
import com.project_x.listing.dto.response.PublicPropertyResponse;
import com.project_x.listing.service.PublicPropertyService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/available-properties")
@RequiredArgsConstructor
public class PublicPropertyController {

    private final PublicPropertyService publicPropertyService;

    @GetMapping
    public ResponseEntity<ApiResponse> fetchPublishedProperties(
            @ModelAttribute PublicPropertyFilterRequest filter
    ) {
        Page<PublicPropertyResponse> response =
                publicPropertyService.fetchPublishedProperties(filter);

        return ResponseEntity.ok(ResponseUtil.success(
                        0,
                        "Properties fetched",
                        "Published properties fetched successfully",
                        response.getContent(),
                        PaginationAdapters.toMeta(response)
                )
        );
    }

    @GetMapping("/{propertyId}")
    public ResponseEntity<ApiResponse> fetchPublishedProperty(
            @PathVariable UUID propertyId
    ) {
        PublicPropertyDetailsResponse response =
                publicPropertyService.fetchPublishedProperty(propertyId);

        return ResponseEntity.ok(ResponseUtil.success(
                0,
                "Property fetched",
                "Published property fetched successfully",
                response,
                null
        ));
    }
}
