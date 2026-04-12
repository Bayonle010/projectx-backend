package com.project_x.listing.adress.controller;


import com.project_x.listing.adress.dto.LgaResponse;
import com.project_x.listing.adress.dto.StateResponse;
import com.project_x.listing.adress.service.LocationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/locations")
public class LocationController {
    private final LocationService locationService;

    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    @GetMapping("/states")
    public List<StateResponse> getStates() {
        return locationService.getStates();
    }

    @GetMapping("/states/{stateId}/lgas")
    public List<LgaResponse> getLgasByStateId(@PathVariable UUID stateId) {
        return locationService.getLgasByStateId(stateId);
    }
}
