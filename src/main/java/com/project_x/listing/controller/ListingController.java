package com.project_x.listing.controller;

import com.project_x.core.paginationhelper.PaginationAdapters;
import com.project_x.core.response.ApiResponse;
import com.project_x.core.response.ResponseUtil;
import com.project_x.core.security.model.AuthenticationIdentity;
import com.project_x.listing.dto.request.SaveListingRequest;
import com.project_x.listing.dto.response.ListingResponse;
import com.project_x.listing.dto.response.GeneratedListingDescriptionResponse;
import com.project_x.listing.service.ListingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/listings")
@RequiredArgsConstructor
public class ListingController {

    private final ListingService listingService;

    @PostMapping("/draft")
    public ResponseEntity<ApiResponse> create(
            @Valid @RequestBody SaveListingRequest request,
            @RequestAttribute("AUTH_IDENTITY") AuthenticationIdentity auth
    ) {
        ListingResponse response = listingService.save(request, auth);

        return ResponseEntity.status(HttpStatus.OK).body(
                ResponseUtil.success(0, "Listing saved", "Listing saved successfully", response, null)
        );
    }


    @PatchMapping("/{listingId}/submit")
    public ResponseEntity<ApiResponse> submitForReview(
            @PathVariable UUID listingId,
            @RequestAttribute("AUTH_IDENTITY") AuthenticationIdentity auth

    ) {
        ListingResponse response = listingService.submitForReview(listingId, auth);

        return ResponseEntity.ok(
                ResponseUtil.success(0, "Listing submitted for review successfully","" , response, "")
        );
    }

    @PostMapping("/{listingId}/description/generate")
    public ResponseEntity<ApiResponse> generateDescription(
            @PathVariable UUID listingId,
            @RequestAttribute("AUTH_IDENTITY") AuthenticationIdentity auth
    ) {
        GeneratedListingDescriptionResponse response =
                listingService.generateDescription(listingId, auth);

        return ResponseEntity.ok(
                ResponseUtil.success(
                        0,
                        "Description generated",
                        "Review and save the generated description",
                        response,
                        null
                )
        );
    }

    @GetMapping("")
    public ResponseEntity<ApiResponse> fetchProperties(
            @RequestParam(required = false) String status,
            @RequestParam(required = false, defaultValue = "1") Long page,
            @RequestParam(required = false, defaultValue = "35")  Long pageSize,
            @RequestAttribute("AUTH_IDENTITY") AuthenticationIdentity authenticationIdentity
    ){
        Page<ListingResponse> response = listingService.fetchListings(status, page, pageSize, authenticationIdentity);

        return ResponseEntity.ok(
                ResponseUtil.success(
                        0,
                        "Listings fetched",
                        "Listings fetched successfully",
                        response.getContent(),
                        PaginationAdapters.toMeta(response)
                )
        );
    }

    @GetMapping("/{listingId}")
    public ResponseEntity<ApiResponse> fetchPropertyById(
            @PathVariable UUID listingId,
            @RequestAttribute("AUTH_IDENTITY") AuthenticationIdentity authenticationIdentity
    ) {
        ListingResponse response = listingService.fetchListingById(
                listingId,
                authenticationIdentity
        );

        return ResponseEntity.ok(
                ResponseUtil.success(
                        0,
                        "Listing fetched",
                        "Listing fetched successfully",
                        response,
                        null
                )
        );
    }

    @PatchMapping("/{listingId}/archive")
    public ResponseEntity<ApiResponse> archiveProperty(
            @PathVariable UUID listingId,
            @RequestAttribute("AUTH_IDENTITY") AuthenticationIdentity authenticationIdentity
    ) {
        ListingResponse response = listingService.archiveProperty(
                listingId,
                authenticationIdentity
        );

        return ResponseEntity.ok(
                ResponseUtil.success(
                        0,
                        "Listing archived",
                        "Listing archived successfully",
                        response,
                        null
                )
        );
    }
}
