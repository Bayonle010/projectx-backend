package com.project_x.listing.controller;


import com.project_x.core.response.ApiResponse;
import com.project_x.core.response.ResponseUtil;
import com.project_x.listing.dto.request.AmenitiesRequest;
import com.project_x.listing.dto.response.AmenitiesResponse;
import com.project_x.listing.service.AmenitiesService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/amenities")
public class AmenitiesController {

    private final AmenitiesService amenitiesService;

    public AmenitiesController(AmenitiesService amenitiesService) {
        this.amenitiesService = amenitiesService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse> createAmenity(@Valid @RequestBody AmenitiesRequest request) {
        AmenitiesResponse response = amenitiesService.createAmenity(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                ResponseUtil.success(0, "Amenity created successfully", "", response, "")
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse> getAllAmenities() {
        List<AmenitiesResponse> response = amenitiesService.getAllAmenities();

        return ResponseEntity.ok(
                ResponseUtil.success(0, "Amenities fetched successfully", "", response, "")
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getAmenityById(@PathVariable UUID id) {
        AmenitiesResponse response = amenitiesService.getAmenityById(id);

        return ResponseEntity.ok(
                ResponseUtil.success(0, "Amenity fetched successfully", "", response, "")
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteAmenity(@PathVariable UUID id) {
        amenitiesService.deleteAmenity(id);

        return ResponseEntity.ok(
                ResponseUtil.success(0, "Amenity deleted successfully", "", null, "")

        );
    }

}
