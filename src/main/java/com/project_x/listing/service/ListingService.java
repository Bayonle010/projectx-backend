package com.project_x.listing.service;

import com.project_x.core.security.model.AuthenticationIdentity;
import com.project_x.listing.dto.request.SaveListingRequest;
import com.project_x.listing.dto.response.ListingResponse;
import org.springframework.data.domain.Page;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.util.UUID;

public interface ListingService {
    ListingResponse save(SaveListingRequest request, AuthenticationIdentity auth);
    ListingResponse submitForReview(UUID listingId, AuthenticationIdentity authenticationIdentity);

    Page<ListingResponse> fetchListings(String status, Long page, Long pageSize, AuthenticationIdentity authenticationIdentity);

    ListingResponse fetchListingById(
            UUID listingId,
            AuthenticationIdentity authenticationIdentity
    );

    ListingResponse archiveProperty(
            UUID listingId,
            AuthenticationIdentity authenticationIdentity
    );
}
