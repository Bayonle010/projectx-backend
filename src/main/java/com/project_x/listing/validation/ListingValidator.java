package com.project_x.listing.validation;


import com.project_x.core.exception.BadRequestException;
import com.project_x.core.exception.ResourceNotFoundException;
import com.project_x.adress.entity.Lga;
import com.project_x.adress.entity.State;
import com.project_x.listing.dto.request.CreateListingRequest;
import com.project_x.listing.entity.Amenity;
import com.project_x.listing.repository.AmenityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ListingValidator {

    private final AmenityRepository amenityRepository;

    public void validateForCreate(CreateListingRequest request) {
        validateDescription(request.description());
        validateImages(request);
        validateCoordinates(request.latitude(), request.longitude());
    }


    public void validateLgaBelongsToState(Lga lga, State state) {
        if (lga.getState() == null || !Objects.equals(lga.getState().getId(), state.getId())) {
            throw new BadRequestException("Selected LGA does not belong to the selected state");
        }
    }

    public Set<Amenity> resolveAmenities(Set<UUID> amenityIds) {
        if (amenityIds == null || amenityIds.isEmpty()) {
            return new HashSet<>();
        }

        List<Amenity> amenities = amenityRepository.findAllByIdIn(amenityIds);

        if (amenities.size() != amenityIds.size()) {
            Set<UUID> foundIds = amenities.stream()
                    .map(Amenity::getId)
                    .collect(Collectors.toSet());

            Set<UUID> missingIds = amenityIds.stream()
                    .filter(id -> !foundIds.contains(id))
                    .collect(Collectors.toSet());

            throw new ResourceNotFoundException("Amenities not found: " + missingIds);
        }

        return new HashSet<>(amenities);
    }

    private void validateDescription(String description) {
        int count = wordCount(description);
        log.info("Description word count: {}", count);

        if (description == null || wordCount(description) < 100) {
            throw new BadRequestException("Description must be at least 100 words");
        }
    }

    private void validateImages(CreateListingRequest request) {
        if (request.images() == null || request.images().size() < 6) {
            throw new BadRequestException("At least 6 property images are required");
        }

        boolean hasInvalidImage = request.images().stream().anyMatch(image ->
                image == null ||
                        isBlank(image.publicId()) ||
                        isBlank(image.optimizedUrl())
        );

        if (hasInvalidImage) {
            throw new BadRequestException("Each image must contain publicId and optimizedUrl");
        }
    }

    private void validateCoordinates(Double latitude, Double longitude) {
        if (latitude != null && (latitude < -90 || latitude > 90)) {
            throw new BadRequestException("Latitude must be between -90 and 90");
        }

        if (longitude != null && (longitude < -180 || longitude > 180)) {
            throw new BadRequestException("Longitude must be between -180 and 180");
        }
    }

    private int wordCount(String text) {
        String trimmed = text == null ? "" : text.trim();
        if (trimmed.isEmpty()) {
            return 0;
        }
        return trimmed.split("\\s+").length;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isBlank();
    }
}
