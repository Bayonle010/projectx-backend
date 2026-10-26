package com.project_x.listing.builder;

import com.project_x.adress.entity.Lga;
import com.project_x.adress.entity.State;
import com.project_x.listing.dto.response.ListingAmenityResponse;
import com.project_x.listing.dto.response.ListingWaterSourceResponse;
import com.project_x.listing.dto.response.PublicPropertyDetailsResponse;
import com.project_x.listing.dto.response.PublicPropertyImageResponse;
import com.project_x.listing.dto.response.PublicPropertyOwnerResponse;
import com.project_x.listing.dto.response.PublicPropertyResponse;
import com.project_x.listing.entity.Amenity;
import com.project_x.listing.entity.Listing;
import com.project_x.listing.entity.ListingImage;
import com.project_x.listing.entity.PropertyType;
import com.project_x.listing.entity.WaterSource;
import com.project_x.user.entity.User;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Component
public class PublicPropertyResponseBuilder {

    public PublicPropertyResponse toResponse(
            Listing listing,
            String coverImageUrl
    ) {
        PropertyType propertyType = listing.getPropertyType();
        State state = listing.getState();
        Lga lga = listing.getLga();

        return PublicPropertyResponse.builder()
                .id(listing.getId())
                .friendlyId(listing.getFriendlyId())
                .relationshipType(listing.getRelationshipType())
                .propertyTypeId(propertyType == null ? null : propertyType.getId())
                .propertyTypeName(propertyType == null ? null : propertyType.getName())
                .bedroomCount(listing.getBedroomCount())
                .bathroomCount(listing.getBathroomCount())
                .toiletCount(listing.getToiletCount())
                .stateId(state == null ? null : state.getId())
                .stateName(state == null ? null : state.getName())
                .lgaId(lga == null ? null : lga.getId())
                .lgaName(lga == null ? null : lga.getName())
                .neighbourhood(listing.getNeighbourhood())
                .rentAmount(listing.getRentAmount())
                .rentPaymentFrequency(listing.getRentPaymentFrequency())
                .coverImageUrl(coverImageUrl)
                .owner(toOwnerResponse(listing.getOwner()))
                .createdAt(listing.getCreatedAt())
                .build();
    }

    public PublicPropertyDetailsResponse toDetailsResponse(Listing listing) {
        PropertyType propertyType = listing.getPropertyType();
        State state = listing.getState();
        Lga lga = listing.getLga();
        boolean exactAddressShared =
                Boolean.TRUE.equals(listing.getShareAddressWithSeekers());

        return PublicPropertyDetailsResponse.builder()
                .id(listing.getId())
                .friendlyId(listing.getFriendlyId())
                .relationshipType(listing.getRelationshipType())
                .propertyTypeId(propertyType == null ? null : propertyType.getId())
                .propertyTypeName(propertyType == null ? null : propertyType.getName())
                .bedroomCount(listing.getBedroomCount())
                .bathroomCount(listing.getBathroomCount())
                .toiletCount(listing.getToiletCount())
                .propertyCondition(listing.getPropertyCondition())
                .unitCount(listing.getUnitCount())
                .description(listing.getDescription())
                .waterSources(toWaterSources(listing.getWaterSources()))
                .parkingAvailable(listing.getParkingAvailable())
                .fencedOrGated(listing.getFencedOrGated())
                .renovated(listing.getRenovated())
                .furnishingStatus(listing.getFurnishingStatus())
                .stateId(state == null ? null : state.getId())
                .stateName(state == null ? null : state.getName())
                .lgaId(lga == null ? null : lga.getId())
                .lgaName(lga == null ? null : lga.getName())
                .neighbourhood(listing.getNeighbourhood())
                .exactAddressShared(exactAddressShared)
                .addressLine(exactAddressShared ? listing.getAddressLine() : null)
                .landmark(exactAddressShared ? listing.getLandmark() : null)
                .latitude(exactAddressShared ? listing.getLatitude() : null)
                .longitude(exactAddressShared ? listing.getLongitude() : null)
                .rentAmount(listing.getRentAmount())
                .rentPaymentFrequency(listing.getRentPaymentFrequency())
                .agencyFee(listing.getAgencyFee())
                .legalAgreementFee(listing.getLegalAgreementFee())
                .cautionFee(listing.getCautionFee())
                .serviceCharge(listing.getServiceCharge())
                .amenities(toAmenities(listing.getAmenities()))
                .images(toImages(listing.getImages()))
                .videoUrl(listing.getVideoUrl())
                .owner(toOwnerResponse(listing.getOwner()))
                .createdAt(listing.getCreatedAt())
                .updatedAt(listing.getUpdatedAt())
                .build();
    }

    private List<ListingWaterSourceResponse> toWaterSources(
            Set<WaterSource> waterSources
    ) {
        if (waterSources == null) {
            return List.of();
        }

        return waterSources.stream()
                .sorted(Comparator.comparing(WaterSource::getName))
                .map(waterSource -> ListingWaterSourceResponse.builder()
                        .id(waterSource.getId())
                        .name(waterSource.getName())
                        .code(waterSource.getCode())
                        .build())
                .toList();
    }

    private Set<ListingAmenityResponse> toAmenities(Set<Amenity> amenities) {
        if (amenities == null) {
            return Set.of();
        }

        return amenities.stream()
                .sorted(Comparator.comparing(Amenity::getName))
                .map(amenity -> ListingAmenityResponse.builder()
                        .id(amenity.getId())
                        .name(amenity.getName())
                        .build())
                .collect(java.util.stream.Collectors.toCollection(
                        LinkedHashSet::new
                ));
    }

    private List<PublicPropertyImageResponse> toImages(
            List<ListingImage> images
    ) {
        if (images == null) {
            return List.of();
        }

        return images.stream()
                .sorted(Comparator.comparing(ListingImage::getPosition))
                .map(image -> PublicPropertyImageResponse.builder()
                        .url(image.getUrl())
                        .position(image.getPosition())
                        .format(image.getFormat())
                        .build())
                .toList();
    }

    private PublicPropertyOwnerResponse toOwnerResponse(User owner) {
        if (owner == null) {
            return null;
        }

        return PublicPropertyOwnerResponse.builder()
                .id(owner.getId())
                .firstName(owner.getFirstname())
                .lastName(owner.getLastname())
                .avatar(owner.getAvatar())
                .build();
    }
}
