package com.project_x.listing.service;

import com.project_x.listing.dto.request.CreatePropertyTypeRequest;
import com.project_x.listing.dto.request.UpdatePropertyTypeRequest;
import com.project_x.listing.dto.response.PropertyTypeResponse;

import java.util.List;
import java.util.UUID;

public interface PropertyTypeService {

    PropertyTypeResponse createPropertyType(
            CreatePropertyTypeRequest request
    );

    List<PropertyTypeResponse> getAllPropertyTypes(
            boolean includeInactive
    );

    PropertyTypeResponse getPropertyTypeById(UUID id);

    PropertyTypeResponse updatePropertyType(
            UUID id,
            UpdatePropertyTypeRequest request
    );

    PropertyTypeResponse archivePropertyType(UUID id);

    PropertyTypeResponse restorePropertyType(UUID id);
}