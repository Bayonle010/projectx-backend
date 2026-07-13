package com.project_x.listing.controller;

import com.project_x.core.response.ApiResponse;
import com.project_x.core.response.ResponseUtil;
import com.project_x.listing.dto.request.CreateWaterSourceRequest;
import com.project_x.listing.dto.request.UpdateWaterSourceRequest;
import com.project_x.listing.dto.response.WaterSourceResponse;
import com.project_x.listing.service.WaterSourceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/water-sources")
@RequiredArgsConstructor
public class WaterSourceController {

    private final WaterSourceService waterSourceService;

    @PostMapping
    public ResponseEntity<ApiResponse> createWaterSource(
            @Valid @RequestBody CreateWaterSourceRequest request
    ) {
        WaterSourceResponse response =
                waterSourceService.createWaterSource(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                ResponseUtil.success(
                        0,
                        "Water source created",
                        "Water source created successfully",
                        response,
                        ""
                )
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse> getAllWaterSources(
            @RequestParam(
                    required = false,
                    defaultValue = "false"
            )
            boolean includeInactive
    ) {
        List<WaterSourceResponse> response =
                waterSourceService.getAllWaterSources(
                        includeInactive
                );

        return ResponseEntity.ok(
                ResponseUtil.success(
                        0,
                        "Water sources fetched",
                        "Water sources fetched successfully",
                        response,
                        ""
                )
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getWaterSourceById(
            @PathVariable UUID id
    ) {
        WaterSourceResponse response =
                waterSourceService.getWaterSourceById(id);

        return ResponseEntity.ok(
                ResponseUtil.success(
                        0,
                        "Water source fetched",
                        "Water source fetched successfully",
                        response,
                        ""
                )
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> updateWaterSource(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateWaterSourceRequest request
    ) {
        WaterSourceResponse response =
                waterSourceService.updateWaterSource(
                        id,
                        request
                );

        return ResponseEntity.ok(
                ResponseUtil.success(
                        0,
                        "Water source updated",
                        "Water source updated successfully",
                        response,
                        ""
                )
        );
    }

    @PatchMapping("/{id}/archive")
    public ResponseEntity<ApiResponse> archiveWaterSource(
            @PathVariable UUID id
    ) {
        WaterSourceResponse response =
                waterSourceService.archiveWaterSource(id);

        return ResponseEntity.ok(
                ResponseUtil.success(
                        0,
                        "Water source archived",
                        "Water source archived successfully",
                        response,
                        ""
                )
        );
    }

    @PatchMapping("/{id}/restore")
    public ResponseEntity<ApiResponse> restoreWaterSource(
            @PathVariable UUID id
    ) {
        WaterSourceResponse response =
                waterSourceService.restoreWaterSource(id);

        return ResponseEntity.ok(
                ResponseUtil.success(
                        0,
                        "Water source restored",
                        "Water source restored successfully",
                        response,
                        ""
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteWaterSource(
            @PathVariable UUID id
    ) {
        waterSourceService.deleteWaterSource(id);

        return ResponseEntity.ok(
                ResponseUtil.success(
                        0,
                        "Water source deleted",
                        "Water source permanently deleted successfully",
                        null,
                        ""
                )
        );
    }
}