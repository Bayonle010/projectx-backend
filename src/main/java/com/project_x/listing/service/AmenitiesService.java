package com.project_x.listing.service;

import com.project_x.listing.dto.request.AmenitiesRequest;
import com.project_x.listing.dto.response.AmenitiesResponse;

import java.util.List;
import java.util.UUID;

public interface AmenitiesService {
    AmenitiesResponse createAmenity(AmenitiesRequest request);
    List<AmenitiesResponse> getAllAmenities();
    AmenitiesResponse getAmenityById(UUID id);
    void deleteAmenity(UUID id);



}
