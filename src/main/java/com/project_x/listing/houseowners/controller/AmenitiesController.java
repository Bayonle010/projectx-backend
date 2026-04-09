package com.project_x.listing.houseowners.controller;


import com.project_x.core.response.ApiResponse;
import com.project_x.core.response.ResponseUtil;
import com.project_x.listing.houseowners.dto.request.AmenitiesRequest;
import com.project_x.listing.houseowners.dto.response.AmenitiesResponse;
import com.project_x.listing.houseowners.service.AmenitiesService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/amenities")
public class AmenitiesController {

    private final AmenitiesService amenitiesService;

    public AmenitiesController(AmenitiesService amenitiesService) {
        this.amenitiesService = amenitiesService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse> createAmenity(@Valid @RequestBody AmenitiesRequest request){
        AmenitiesResponse response = amenitiesService.createAmenity(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                ResponseUtil.success(0, "Amenity created successfully", "", response, "")
        );
    }
}
