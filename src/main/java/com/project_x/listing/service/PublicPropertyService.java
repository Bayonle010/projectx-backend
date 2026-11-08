package com.project_x.listing.service;

import com.project_x.listing.dto.request.PublicPropertyFilterRequest;
import com.project_x.listing.dto.response.PublicPropertyDetailsResponse;
import com.project_x.listing.dto.response.PublicPropertyResponse;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface PublicPropertyService {

    Page<PublicPropertyResponse> fetchPublishedProperties(PublicPropertyFilterRequest filter);

    PublicPropertyDetailsResponse fetchPublishedProperty(UUID propertyId);
}
