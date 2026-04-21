package com.project_x.listing.controller;

import com.project_x.core.response.ApiResponse;
import com.project_x.core.response.ResponseUtil;
import com.project_x.core.security.model.AuthenticationIdentity;
import com.project_x.listing.dto.request.CreateListingRequest;
import com.project_x.listing.dto.response.ListingResponse;
import com.project_x.listing.service.ListingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/listings")
@RequiredArgsConstructor
public class ListingController {

    private final ListingService listingService;

    @PostMapping
    public ResponseEntity<ApiResponse> create(
            @Valid @RequestBody CreateListingRequest request,
            @RequestAttribute("AUTH_IDENTITY") AuthenticationIdentity auth
    ) {
        ListingResponse response = listingService.create(request, auth);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                ResponseUtil.success(0, "Property listed", "Listing created successfully", response, null)
        );
    }
}