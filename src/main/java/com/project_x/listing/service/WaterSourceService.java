package com.project_x.listing.service;

import com.project_x.listing.dto.request.CreateWaterSourceRequest;
import com.project_x.listing.dto.request.UpdateWaterSourceRequest;
import com.project_x.listing.dto.response.WaterSourceResponse;

import java.util.List;
import java.util.UUID;

public interface WaterSourceService {

    WaterSourceResponse createWaterSource(
            CreateWaterSourceRequest request
    );

    List<WaterSourceResponse> getAllWaterSources(
            boolean includeInactive
    );

    WaterSourceResponse getWaterSourceById(UUID id);

    WaterSourceResponse updateWaterSource(
            UUID id,
            UpdateWaterSourceRequest request
    );

    WaterSourceResponse archiveWaterSource(UUID id);

    WaterSourceResponse restoreWaterSource(UUID id);

    void deleteWaterSource(UUID id);
}