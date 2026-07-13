package com.project_x.listing.service.impl;

import com.project_x.core.exception.BadRequestException;
import com.project_x.core.exception.ResourceNotFoundException;
import com.project_x.listing.builder.PropertyTypeResponseBuilder;
import com.project_x.listing.dto.request.CreatePropertyTypeRequest;
import com.project_x.listing.dto.request.UpdatePropertyTypeRequest;
import com.project_x.listing.dto.response.PropertyTypeResponse;
import com.project_x.listing.entity.PropertyType;
import com.project_x.listing.repository.ListingRepository;
import com.project_x.listing.repository.PropertyTypeRepository;
import com.project_x.listing.service.PropertyTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PropertyTypeServiceImpl
        implements PropertyTypeService {

    private final PropertyTypeRepository propertyTypeRepository;
    private final ListingRepository listingRepository;

    @Override
    @Transactional
    public PropertyTypeResponse createPropertyType(
            CreatePropertyTypeRequest request
    ) {
        String name = normalizeName(request.name());

        if (propertyTypeRepository.existsByNameIgnoreCase(name)) {
            throw new BadRequestException(
                    "Property type already exists"
            );
        }

        String code = generateCode(name);

        if (propertyTypeRepository.existsByCodeIgnoreCase(code)) {
            throw new BadRequestException(
                    "A property type with the generated code already exists"
            );
        }

        PropertyType propertyType = PropertyType.builder()
                .name(name)
                .code(code)
                .description(normalizeDescription(request.description()))
                .active(true)
                .build();

        PropertyType savedPropertyType =
                propertyTypeRepository.save(propertyType);

        return PropertyTypeResponseBuilder.toResponse(savedPropertyType);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PropertyTypeResponse> getAllPropertyTypes(
            boolean includeInactive
    ) {
        List<PropertyType> propertyTypes = includeInactive
                ? propertyTypeRepository.findAllByOrderByNameAsc()
                : propertyTypeRepository
                .findAllByActiveTrueOrderByNameAsc();

        return propertyTypes.stream()
                .map(PropertyTypeResponseBuilder::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PropertyTypeResponse getPropertyTypeById(UUID id) {
        PropertyType propertyType = findPropertyType(id);

        return PropertyTypeResponseBuilder.toResponse(propertyType);
    }

    @Override
    @Transactional
    public PropertyTypeResponse updatePropertyType(
            UUID id,
            UpdatePropertyTypeRequest request
    ) {
        PropertyType propertyType = findPropertyType(id);

        String name = normalizeName(request.name());

        boolean nameAlreadyExists =
                propertyTypeRepository
                        .existsByNameIgnoreCaseAndIdNot(name, id);

        if (nameAlreadyExists) {
            throw new BadRequestException(
                    "Another property type already uses this name"
            );
        }

        propertyType.setName(name);
        propertyType.setDescription(
                normalizeDescription(request.description())
        );

        /*
         * Do not regenerate the code when the display name changes.
         * The code should remain a stable identifier.
         */

        PropertyType updatedPropertyType =
                propertyTypeRepository.save(propertyType);

        return PropertyTypeResponseBuilder.toResponse(updatedPropertyType);
    }

    @Override
    @Transactional
    public PropertyTypeResponse archivePropertyType(UUID id) {
        PropertyType propertyType = findPropertyType(id);

        /*
         * Idempotent operation:
         * archiving an already archived type still returns success.
         */
        if (propertyType.isActive()) {
            propertyType.setActive(false);
            propertyTypeRepository.save(propertyType);
        }

        return PropertyTypeResponseBuilder.toResponse(propertyType);
    }

    @Override
    @Transactional
    public PropertyTypeResponse restorePropertyType(UUID id) {
        PropertyType propertyType = findPropertyType(id);

        /*
         * Idempotent operation:
         * restoring an active type still returns success.
         */
        if (!propertyType.isActive()) {
            propertyType.setActive(true);
            propertyTypeRepository.save(propertyType);
        }

        return PropertyTypeResponseBuilder.toResponse(propertyType);
    }

    @Override
    @Transactional
    public void deletePropertyType(UUID id) {
        PropertyType propertyType = propertyTypeRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Property type not found"
                        )
                );

        if (propertyType.isActive()) {
            throw new BadRequestException(
                    "Property type must be archived before it can be permanently deleted"
            );
        }

        boolean usedByListing =
                listingRepository.existsByPropertyType_Id(id);

        if (usedByListing) {
            throw new BadRequestException(
                    "Property type cannot be permanently deleted because it is used by one or more listings"
            );
        }

        propertyTypeRepository.delete(propertyType);
    }

    private PropertyType findPropertyType(UUID id) {
        return propertyTypeRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Property type not found"
                        )
                );
    }

    private String normalizeName(String name) {
        return name.trim()
                .replaceAll("\\s+", " ");
    }

    private String normalizeDescription(String description) {
        if (description == null || description.isBlank()) {
            return null;
        }

        return description.trim();
    }

    private String generateCode(String name) {
        return name.trim()
                .toUpperCase()
                .replaceAll("[^A-Z0-9]+", "_")
                .replaceAll("^_+|_+$", "");
    }
}