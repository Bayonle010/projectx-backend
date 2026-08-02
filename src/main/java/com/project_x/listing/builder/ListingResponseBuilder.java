package com.project_x.listing.builder;

import com.project_x.adress.entity.Lga;
import com.project_x.adress.entity.State;
import com.project_x.listing.dto.response.*;
import com.project_x.listing.entity.Listing;
import com.project_x.listing.entity.ListingImage;
import com.project_x.listing.entity.PropertyType;
import com.project_x.listing.entity.WaterSource;
import com.project_x.user.entity.User;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
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

                .neighbourhood(listing.getNeighbourhood())

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
                        toAmenityResponses(listing)
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

    private List<ListingWaterSourceResponse> toWaterSourceResponses(
            Listing listing
    ) {
        Set<WaterSource> waterSources = listing.getWaterSources();

        if (waterSources == null || waterSources.isEmpty()) {
            return List.of();
        }

        return waterSources.stream()
                .filter(Objects::nonNull)
                .map(this::toWaterSourceResponse)
                .sorted(
                        Comparator.comparing(
                                ListingWaterSourceResponse::name,
                                Comparator.nullsLast(
                                        String.CASE_INSENSITIVE_ORDER
                                )
                        )
                )
                .toList();
    }


    private ListingWaterSourceResponse toWaterSourceResponse(
            WaterSource waterSource
    ) {
        return ListingWaterSourceResponse.builder()
                .id(waterSource.getId())
                .name(waterSource.getName())
                .code(waterSource.getCode())
                .build();
    }

    private Set<ListingAmenityResponse> toAmenityResponses(
            Listing listing
    ) {
        if (
                listing.getAmenities() == null
                        || listing.getAmenities().isEmpty()
        ) {
            return Set.of();
        }

        return listing.getAmenities()
                .stream()
                .filter(Objects::nonNull)
                .map(amenity ->
                        ListingAmenityResponse.builder()
                                .id(amenity.getId())
                                .name(amenity.getName())
                                .build()
                )
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
                .publicId(image.getPublicId())
                .position(image.getPosition())
                .format(image.getFormat())
                .build();
    }
}