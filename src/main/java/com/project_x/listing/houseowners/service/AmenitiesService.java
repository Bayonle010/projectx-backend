package com.project_x.listing.houseowners.service;

import com.project_x.listing.houseowners.dto.request.AmenitiesRequest;
import com.project_x.listing.houseowners.dto.response.AmenitiesResponse;

import java.util.List;

public interface AmenitiesService {
    AmenitiesResponse createAmenity(AmenitiesRequest request);
    List<AmenitiesResponse> getAllAmenities();

}
