package com.project_x.listing.service;

import com.project_x.listing.dto.response.PublicPropertyDetailsResponse;
import com.project_x.listing.dto.response.PublicPropertyResponse;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface PublicPropertyService {

    Page<PublicPropertyResponse> fetchPublishedProperties(Long page, Long pageSize);

    PublicPropertyDetailsResponse fetchPublishedProperty(UUID propertyId);
}
