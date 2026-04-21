package com.project_x.listing.houseowners.service;

import com.project_x.core.security.model.AuthenticationIdentity;
import com.project_x.listing.houseowners.dto.request.CreateListingRequest;
import com.project_x.listing.houseowners.dto.response.ListingResponse;

public interface ListingService {
    ListingResponse create(CreateListingRequest request, AuthenticationIdentity auth);
}
