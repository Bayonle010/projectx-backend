package com.project_x.listing.validation;

import com.project_x.adress.entity.Lga;
import com.project_x.adress.entity.State;
import com.project_x.core.exception.BadRequestException;
import com.project_x.listing.dto.request.ImageRequest;
import com.project_x.listing.dto.request.SaveListingRequest;
import com.project_x.listing.entity.Listing;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Slf4j
@Component
public class ListingValidator {

    public void validateForDraftSave(SaveListingRequest request) {
        validateCoordinates(
                request.latitude(),
                request.longitude()
        );

        /*
         * Description is optional while saving a draft.
         * However, if supplied, it must already be valid.
         */
        if (request.description() != null) {
            validateDescription(request.description());
        }

        /*
         * Images are optional while saving a draft.
         * However, supplied image objects must be valid.
         */
        if (request.images() != null) {
            validateRequestImages(request.images());
        }
    }

    public void validateForSubmission(Listing listing) {
        require(
                listing.getRelationshipType() != null,
                "Relationship type is required"
        );

        require(
                listing.getPropertyType() != null,
                "Property type is required"
        );

        if (listing.getPropertyType() != null) {
            require(
                    listing.getPropertyType().isActive(),
                    "Selected property type is no longer available"
            );
        }

        require(
                listing.getBedroomCount() != null,
                "Bedroom count is required"
        );

        require(
                listing.getBathroomCount() != null,
                "Bathroom count is required"
        );

        require(
                listing.getToiletCount() != null,
                "Toilet count is required"
        );

        require(
                listing.getPropertyCondition() != null,
                "Property condition is required"
        );

        require(
                listing.getUnitCount() != null,
                "Unit count is required"
        );

        validateDescription(listing.getDescription());

        require(
                listing.getWaterSource() != null,
                "Water source is required"
        );

        if (listing.getWaterSource() != null) {
            require(
                    listing.getWaterSource().isActive(),
                    "Selected water source is no longer available"
            );
        }

        require(
                listing.getParkingAvailable() != null,
                "Parking availability is required"
        );

        require(
                listing.getFencedOrGated() != null,
                "Fenced or gated status is required"
        );

        require(
                listing.getRenovated() != null,
                "Renovation status is required"
        );

        require(
                listing.getFurnishingStatus() != null,
                "Furnishing status is required"
        );

        require(
                listing.getState() != null,
                "State is required"
        );

        require(
                listing.getLga() != null,
                "LGA is required"
        );

        if (listing.getState() != null && listing.getLga() != null) {
            validateLgaBelongsToState(
                    listing.getLga(),
                    listing.getState()
            );
        }

        require(
                !isBlank(listing.getAddressLine()),
                "Address line is required"
        );

        require(
                listing.getShareAddressWithSeekers() != null,
                "Share address with seekers option is required"
        );

        require(
                listing.getRentAmount() != null
                        && listing.getRentAmount().signum() > 0,
                "Rent amount is required and must be greater than zero"
        );

        require(
                listing.getRentPaymentFrequency() != null,
                "Rent payment frequency is required"
        );

        require(
                !isBlank(listing.getProofOfOwnershipUrl()),
                "Proof of ownership is required"
        );

        require(
                listing.getImages() != null
                        && listing.getImages().size() >= 6,
                "At least 6 property images are required"
        );

        if (listing.getImages() != null) {
            validatePersistedImages(listing);
        }

        validateCoordinates(
                listing.getLatitude(),
                listing.getLongitude()
        );
    }

    public void validateLgaBelongsToState(Lga lga, State state) {
        if (
                lga.getState() == null
                        || !Objects.equals(
                        lga.getState().getId(),
                        state.getId()
                )
        ) {
            throw new BadRequestException(
                    "Selected LGA does not belong to the selected state"
            );
        }
    }

    private void validateDescription(String description) {
        int count = wordCount(description);

        log.info("Listing description word count: {}", count);

        if (count < 100) {
            throw new BadRequestException(
                    "Description must be at least 100 words"
            );
        }
    }

    private void validateRequestImages(List<ImageRequest> images) {
        boolean hasInvalidImage = images.stream()
                .anyMatch(image ->
                        image == null
                                || isBlank(image.publicId())
                                || isBlank(image.optimizedUrl())
                );

        if (hasInvalidImage) {
            throw new BadRequestException(
                    "Each image must contain publicId and optimizedUrl"
            );
        }
    }

    private void validatePersistedImages(Listing listing) {
        boolean hasInvalidImage = listing.getImages().stream()
                .anyMatch(image ->
                        image == null
                                || isBlank(image.getPublicId())
                                || isBlank(image.getUrl())
                );

        if (hasInvalidImage) {
            throw new BadRequestException(
                    "Each image must contain publicId and url"
            );
        }
    }

    private void validateCoordinates(
            Double latitude,
            Double longitude
    ) {
        if (
                latitude != null
                        && (latitude < -90 || latitude > 90)
        ) {
            throw new BadRequestException(
                    "Latitude must be between -90 and 90"
            );
        }

        if (
                longitude != null
                        && (longitude < -180 || longitude > 180)
        ) {
            throw new BadRequestException(
                    "Longitude must be between -180 and 180"
            );
        }
    }

    private int wordCount(String text) {
        if (text == null || text.isBlank()) {
            return 0;
        }

        return text.trim().split("\\s+").length;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private void require(boolean condition, String message) {
        if (!condition) {
            throw new BadRequestException(message);
        }
    }
}