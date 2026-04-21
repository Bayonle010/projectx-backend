package com.project_x.listing.houseowners.service.impl;

import com.project_x.core.security.model.AuthenticationIdentity;
import com.project_x.listing.houseowners.dto.request.CreateListingRequest;
import com.project_x.listing.houseowners.dto.response.ListingResponse;
import com.project_x.listing.houseowners.service.ListingService;
import org.springframework.stereotype.Service;

@Service
public class ListingServiceImpl implements ListingService {
    @Override
    public ListingResponse create(CreateListingRequest request, AuthenticationIdentity auth) {
        return null;
    }
}
