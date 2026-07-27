package com.project_x.listing.resolver;

import com.project_x.core.exception.BadRequestException;
import com.project_x.core.exception.ResourceNotFoundException;
import com.project_x.listing.entity.Amenity;
import com.project_x.listing.entity.PropertyType;
import com.project_x.listing.entity.WaterSource;
import com.project_x.listing.repository.AmenityRepository;
import com.project_x.listing.repository.PropertyTypeRepository;
import com.project_x.listing.repository.WaterSourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ListingReferenceResolver {

    private final PropertyTypeRepository propertyTypeRepository;
    private final WaterSourceRepository waterSourceRepository;
    private final AmenityRepository amenityRepository;

    public PropertyType resolvePropertyType(UUID propertyTypeId) {
        return propertyTypeRepository.findByIdAndActiveTrue(propertyTypeId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Property type not found or inactive"
                ));
    }

    public WaterSource resolveWaterSource(UUID waterSourceId) {
        return waterSourceRepository.findByIdAndActiveTrue(waterSourceId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Water source not found or inactive"
                ));
    }

    public Set<Amenity> resolveAmenities(Set<UUID> amenityIds) {
        if (amenityIds == null || amenityIds.isEmpty()) {
            return new HashSet<>();
        }

        List<Amenity> amenities = amenityRepository.findAllByIdIn(amenityIds);

        Set<UUID> foundIds = amenities.stream()
                .map(Amenity::getId)
                .collect(Collectors.toSet());

        Set<UUID> missingIds = amenityIds.stream()
                .filter(id -> !foundIds.contains(id))
                .collect(Collectors.toSet());

        if (!missingIds.isEmpty()) {
            throw new ResourceNotFoundException(
                    "Amenities not found: " + missingIds
            );
        }

        return new HashSet<>(amenities);
    }

    public Set<WaterSource> resolveWaterSources(
            Set<UUID> waterSourceIds
    ) {
        if (waterSourceIds == null || waterSourceIds.isEmpty()) {
            return new HashSet<>();
        }

        if (waterSourceIds.contains(null)) {
            throw new BadRequestException(
                    "Water source IDs cannot contain null values"
            );
        }

        List<WaterSource> waterSources =
                waterSourceRepository.findAllById(waterSourceIds);

        Set<UUID> foundIds = waterSources.stream()
                .map(WaterSource::getId)
                .collect(Collectors.toSet());

        Set<UUID> missingIds = new HashSet<>(waterSourceIds);
        missingIds.removeAll(foundIds);

        if (!missingIds.isEmpty()) {
            throw new BadRequestException(
                    "One or more selected water sources do not exist"
            );
        }

        return new HashSet<>(waterSources);
    }
}