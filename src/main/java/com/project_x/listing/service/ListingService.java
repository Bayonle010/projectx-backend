package com.project_x.listing.service;

import com.project_x.core.security.model.AuthenticationIdentity;
import com.project_x.listing.dto.request.CreateListingRequest;
import com.project_x.listing.dto.response.ListingResponse;

public interface ListingService {
    ListingResponse create(CreateListingRequest request, AuthenticationIdentity auth);
}
