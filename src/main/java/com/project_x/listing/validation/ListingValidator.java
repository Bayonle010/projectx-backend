package com.project_x.listing.validation;


import com.project_x.core.exception.BadRequestException;
import com.project_x.core.exception.ResourceNotFoundException;
import com.project_x.adress.entity.Lga;
import com.project_x.adress.entity.State;
import com.project_x.listing.dto.request.ImageRequest;
import com.project_x.listing.dto.request.SaveListingRequest;
import com.project_x.listing.entity.Amenity;
import com.project_x.listing.entity.Listing;
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

    public void validateForDraftSave(SaveListingRequest request) {
        validateCoordinates(request.latitude(), request.longitude());

        if (request.description() != null && wordCount(request.description()) < 100){
            throw new BadRequestException("Description must be at least 100 characters");
        }

        if (request.images() != null){
            validateImages(request.images());
        }


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

    private void validateImages(List<ImageRequest> images) {
        boolean hasInvalidImage = images.stream().anyMatch(
                image ->
                        image ==null ||
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

    public void validateForSubmission(Listing listing) {

        if (listing.getRelationshipType() == null) {
            throw new BadRequestException("Relationship type is required");
        }

        if (listing.getPropertyType() == null) {
            throw new BadRequestException("Property type is required");
        }

        if (listing.getBedroomCount() == null) {
            throw new BadRequestException("Bedroom count is required");
        }

        if (listing.getBathroomCount() == null) {
            throw new BadRequestException("Bathroom count is required");
        }

        if (listing.getToiletCount() == null) {
            throw new BadRequestException("Toilet count is required");
        }

        if (listing.getPropertyCondition() == null) {
            throw new BadRequestException("Property condition is required");
        }

        if (listing.getUnitCount() == null) {
            throw new BadRequestException("Unit count is required");
        }

        validateDescription(listing.getDescription());

        if (listing.getWaterSource() == null) {
            throw new BadRequestException("Water source is required");
        }

        if (listing.getParkingAvailable() == null) {
            throw new BadRequestException("Parking availability is required");
        }

        if (listing.getFencedOrGated() == null) {
            throw new BadRequestException("Fenced or gated status is required");
        }

        if (listing.getRenovated() == null) {
            throw new BadRequestException("Renovation status is required");
        }

        if (listing.getFurnishingStatus() == null) {
            throw new BadRequestException("Furnishing status is required");
        }

        if (listing.getState() == null) {
            throw new BadRequestException("State is required");
        }

        if (listing.getLga() == null) {
            throw new BadRequestException("LGA is required");
        }

        validateLgaBelongsToState(listing.getLga(), listing.getState());

        if (isBlank(listing.getAddressLine())) {
            throw new BadRequestException("Address line is required");
        }

        if (listing.getShareAddressWithSeekers() == null) {
            throw new BadRequestException("Share address with seekers option is required");
        }

        if (listing.getRentAmount() == null || listing.getRentAmount().signum() <= 0) {
            throw new BadRequestException("Rent amount is required and must be greater than zero");
        }

        if (listing.getRentPaymentFrequency() == null) {
            throw new BadRequestException("Rent payment frequency is required");
        }

        if (isBlank(listing.getProofOfOwnershipUrl())) {
            throw new BadRequestException("Proof of ownership is required");
        }

        if (listing.getImages() == null || listing.getImages().size() < 6) {
            throw new BadRequestException("At least 6 property images are required");
        }

        boolean hasInvalidImage = listing.getImages().stream().anyMatch(
                image ->
                        image == null ||
                                isBlank(image.getPublicId()) ||
                                isBlank(image.getUrl())
        );

        if (hasInvalidImage) {
            throw new BadRequestException("Each image must contain publicId and url");
        }

        validateCoordinates(listing.getLatitude(), listing.getLongitude());
    }
}
