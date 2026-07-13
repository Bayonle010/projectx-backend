package com.project_x.listing.controller;

import com.project_x.core.response.ApiResponse;
import com.project_x.core.response.ResponseUtil;
import com.project_x.listing.dto.request.CreatePropertyTypeRequest;
import com.project_x.listing.dto.request.UpdatePropertyTypeRequest;
import com.project_x.listing.dto.response.PropertyTypeResponse;
import com.project_x.listing.service.PropertyTypeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/property-types")
@RequiredArgsConstructor
public class PropertyTypeController {

    private final PropertyTypeService propertyTypeService;

    @PostMapping
    public ResponseEntity<ApiResponse> createPropertyType(
            @Valid @RequestBody CreatePropertyTypeRequest request
    ) {
        PropertyTypeResponse response =
                propertyTypeService.createPropertyType(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(
                ResponseUtil.success(
                        0,
                        "Property type created",
                        "Property type created successfully",
                        response,
                        ""
                )
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse> getAllPropertyTypes(
            @RequestParam(
                    required = false,
                    defaultValue = "false"
            )
            boolean includeInactive
    ) {
        List<PropertyTypeResponse> response =
                propertyTypeService.getAllPropertyTypes(
                        includeInactive
                );

        return ResponseEntity.ok(
                ResponseUtil.success(
                        0,
                        "Property types fetched",
                        "Property types fetched successfully",
                        response,
                        ""
                )
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getPropertyTypeById(
            @PathVariable UUID id
    ) {
        PropertyTypeResponse response =
                propertyTypeService.getPropertyTypeById(id);

        return ResponseEntity.ok(
                ResponseUtil.success(
                        0,
                        "Property type fetched",
                        "Property type fetched successfully",
                        response,
                        ""
                )
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse> updatePropertyType(
            @PathVariable UUID id,
            @Valid @RequestBody UpdatePropertyTypeRequest request
    ) {
        PropertyTypeResponse response =
                propertyTypeService.updatePropertyType(
                        id,
                        request
                );

        return ResponseEntity.ok(
                ResponseUtil.success(
                        0,
                        "Property type updated",
                        "Property type updated successfully",
                        response,
                        ""
                )
        );
    }

    @PatchMapping("/{id}/archive")
    public ResponseEntity<ApiResponse> archivePropertyType(
            @PathVariable UUID id
    ) {
        PropertyTypeResponse response =
                propertyTypeService.archivePropertyType(id);

        return ResponseEntity.ok(
                ResponseUtil.success(
                        0,
                        "Property type archived",
                        "Property type archived successfully",
                        response,
                        ""
                )
        );
    }

    @PatchMapping("/{id}/restore")
    public ResponseEntity<ApiResponse> restorePropertyType(
            @PathVariable UUID id
    ) {
        PropertyTypeResponse response =
                propertyTypeService.restorePropertyType(id);

        return ResponseEntity.ok(
                ResponseUtil.success(
                        0,
                        "Property type restored",
                        "Property type restored successfully",
                        response,
                        ""
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deletePropertyType(
            @PathVariable UUID id
    ) {
        propertyTypeService.deletePropertyType(id);

        return ResponseEntity.ok(
                ResponseUtil.success(
                        0,
                        "Property type deleted",
                        "Property type permanently deleted successfully",
                        null,
                        ""
                )
        );
    }
}