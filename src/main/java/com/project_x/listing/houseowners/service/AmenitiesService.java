package com.project_x.listing.houseowners.service;

import com.project_x.listing.houseowners.dto.request.AmenitiesRequest;
import com.project_x.listing.houseowners.dto.response.AmenitiesResponse;

public interface AmenitiesService {
    AmenitiesResponse createAmenity(AmenitiesRequest request);
}
