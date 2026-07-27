package com.project_x.listing.builder;

import com.project_x.adress.entity.Lga;
import com.project_x.adress.entity.State;
import com.project_x.listing.dto.response.ImageResponse;
import com.project_x.listing.dto.response.ListingResponse;
import com.project_x.listing.dto.response.WaterSourceResponse;
import com.project_x.listing.entity.Amenity;
import com.project_x.listing.entity.Listing;
import com.project_x.listing.entity.ListingImage;
import com.project_x.listing.entity.PropertyType;
import com.project_x.listing.entity.WaterSource;
import com.project_x.user.entity.User;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ListingResponseBuilder {

    public ListingResponse toResponse(Listing listing) {
        if (listing == null) {
            return null;
        }

        PropertyType propertyType = listing.getPropertyType();
        State state = listing.getState();
        Lga lga = listing.getLga();
        User owner = listing.getOwner();

        return ListingResponse.builder()
                .id(listing.getId())
                .relationshipType(listing.getRelationshipType())

                .propertyTypeId(
                        propertyType != null
                                ? propertyType.getId()
                                : null
                )
                .propertyTypeName(
                        propertyType != null
                                ? propertyType.getName()
                                : null
                )

                .bedroomCount(listing.getBedroomCount())
                .bathroomCount(listing.getBathroomCount())
                .toiletCount(listing.getToiletCount())
                .propertyCondition(listing.getPropertyCondition())
                .unitCount(listing.getUnitCount())
                .description(listing.getDescription())

                .waterSources(
                        toWaterSourceResponses(listing)
                )

                .parkingAvailable(listing.getParkingAvailable())
                .fencedOrGated(listing.getFencedOrGated())
                .renovated(listing.getRenovated())
                .furnishingStatus(listing.getFurnishingStatus())

                .stateId(
                        state != null
                                ? state.getId()
                                : null
                )
                .stateName(
                        state != null
                                ? state.getName()
                                : null
                )

                .lgaId(
                        lga != null
                                ? lga.getId()
                                : null
                )
                .lgaName(
                        lga != null
                                ? lga.getName()
                                : null
                )

                .addressLine(listing.getAddressLine())
                .landmark(listing.getLandmark())
                .latitude(listing.getLatitude())
                .longitude(listing.getLongitude())
                .placeId(listing.getPlaceId())

                .shareAddressWithSeekers(
                        listing.getShareAddressWithSeekers()
                )

                .rentAmount(listing.getRentAmount())
                .rentPaymentFrequency(
                        listing.getRentPaymentFrequency()
                )
                .status(listing.getStatus())

                .agencyFee(listing.getAgencyFee())
                .legalAgreementFee(listing.getLegalAgreementFee())
                .cautionFee(listing.getCautionFee())
                .serviceCharge(listing.getServiceCharge())

                .proofOfOwnershipUrl(
                        listing.getProofOfOwnershipUrl()
                )

                .amenities(
                        toAmenityNames(listing)
                )

                .images(
                        toImageResponses(listing)
                )

                .videoUrl(listing.getVideoUrl())

                .ownerId(
                        owner != null
                                ? owner.getId()
                                : null
                )

                .createdAt(listing.getCreatedAt())
                .updatedAt(listing.getUpdatedAt())
                .build();
    }

    private List<WaterSourceResponse> toWaterSourceResponses(
            Listing listing
    ) {
        Set<WaterSource> waterSources = listing.getWaterSources();

        if (waterSources == null || waterSources.isEmpty()) {
            return List.of();
        }

        return waterSources.stream()
                .filter(waterSource -> waterSource != null)
                .map(this::toWaterSourceResponse)
                .sorted(
                        Comparator.comparing(
                                WaterSourceResponse::name,
                                Comparator.nullsLast(
                                        String.CASE_INSENSITIVE_ORDER
                                )
                        )
                )
                .toList();
    }

    private WaterSourceResponse toWaterSourceResponse(
            WaterSource waterSource
    ) {
        return WaterSourceResponse.builder()
                .id(waterSource.getId())
                .name(waterSource.getName())
                .build();
    }

    private Set<String> toAmenityNames(Listing listing) {
        if (
                listing.getAmenities() == null
                        || listing.getAmenities().isEmpty()
        ) {
            return Set.of();
        }

        return listing.getAmenities()
                .stream()
                .filter(amenity -> amenity != null)
                .map(Amenity::getName)
                .filter(name -> name != null)
                .collect(Collectors.toSet());
    }

    private List<ImageResponse> toImageResponses(
            Listing listing
    ) {
        if (
                listing.getImages() == null
                        || listing.getImages().isEmpty()
        ) {
            return List.of();
        }

        return listing.getImages()
                .stream()
                .filter(image -> image != null)
                .sorted(
                        Comparator.comparing(
                                ListingImage::getPosition,
                                Comparator.nullsLast(
                                        Comparator.naturalOrder()
                                )
                        )
                )
                .map(this::toImageResponse)
                .toList();
    }

    private ImageResponse toImageResponse(
            ListingImage image
    ) {
        return ImageResponse.builder()
                .url(image.getUrl())
                .position(image.getPosition())
                .build();
    }
}