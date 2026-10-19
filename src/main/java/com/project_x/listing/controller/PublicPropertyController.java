package com.project_x.listing.controller;

import com.project_x.core.paginationhelper.PaginationAdapters;
import com.project_x.core.response.ApiResponse;
import com.project_x.core.response.ResponseUtil;
import com.project_x.listing.dto.response.PublicPropertyResponse;
import com.project_x.listing.service.PublicPropertyService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/properties")
@RequiredArgsConstructor
public class PublicPropertyController {

    private final PublicPropertyService publicPropertyService;

    @GetMapping
    public ResponseEntity<ApiResponse> fetchPublishedProperties(
            @RequestParam(defaultValue = "1") Long page,
            @RequestParam(defaultValue = "20") Long pageSize
    ) {
        Page<PublicPropertyResponse> response = publicPropertyService.fetchPublishedProperties(page, pageSize);

        return ResponseEntity.ok(ResponseUtil.success(
                        0,
                        "Properties fetched",
                        "Published properties fetched successfully",
                        response.getContent(),
                        PaginationAdapters.toMeta(response)
                )
        );
    }
}
