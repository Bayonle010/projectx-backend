package com.project_x.listing.houseowners.builder;

import com.project_x.listing.houseowners.dto.response.ImageResponse;
import com.project_x.listing.houseowners.dto.response.ListingResponse;
import com.project_x.listing.houseowners.entity.Amenity;
import com.project_x.listing.houseowners.entity.Listing;
import com.project_x.listing.houseowners.entity.ListingImage;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class ListingResponseBuilder {

    public ListingResponse toResponse(Listing listing) {
        return ListingResponse.builder()
                .id(listing.getId())
                .relationshipType(listing.getRelationshipType())
                .propertyType(listing.getPropertyType())
                .bedroomCount(listing.getBedroomCount())
                .bathroomCount(listing.getBathroomCount())
                .toiletCount(listing.getToiletCount())
                .propertyCondition(listing.getPropertyCondition())
                .unitCount(listing.getUnitCount())
                .description(listing.getDescription())
                .waterSource(listing.getWaterSource())
                .parkingAvailable(listing.getParkingAvailable())
                .fencedOrGated(listing.getFencedOrGated())
                .renovated(listing.getRenovated())
                .furnishingStatus(listing.getFurnishingStatus())
                .stateId(listing.getState().getId())
                .stateName(listing.getState().getName())
                .lgaId(listing.getLga().getId())
                .lgaName(listing.getLga().getName())
                .addressLine(listing.getAddressLine())
                .landmark(listing.getLandmark())
                .latitude(listing.getLatitude())
                .longitude(listing.getLongitude())
                .placeId(listing.getPlaceId())
                .shareAddressWithSeekers(listing.getShareAddressWithSeekers())
                .rentAmount(listing.getRentAmount())
                .rentPaymentFrequency(listing.getRentPaymentFrequency())
                .agencyFee(listing.getAgencyFee())
                .legalAgreementFee(listing.getLegalAgreementFee())
                .cautionFee(listing.getCautionFee())
                .serviceCharge(listing.getServiceCharge())
                .proofOfOwnershipUrl(listing.getProofOfOwnershipUrl())
                .amenities(
                        listing.getAmenities().stream()
                                .map(Amenity::getName)
                                .collect(Collectors.toSet())
                )
                .images(
                        listing.getImages().stream()
                                .map(this::toImageResponse)
                                .toList()
                )
                .videoUrl(listing.getVideoUrl())
                .ownerId(listing.getOwner().getId())
                .createdAt(listing.getCreatedAt())
                .updatedAt(listing.getUpdatedAt())
                .build();
    }

    public ImageResponse toImageResponse(ListingImage image) {
        return ImageResponse.builder()
                .url(image.getUrl())
                .position(image.getPosition())
                .build();
    }
}
