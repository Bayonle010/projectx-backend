package com.project_x.listing.builder;

import com.project_x.adress.entity.Lga;
import com.project_x.adress.entity.State;
import com.project_x.listing.dto.response.PublicPropertyOwnerResponse;
import com.project_x.listing.dto.response.PublicPropertyResponse;
import com.project_x.listing.entity.Listing;
import com.project_x.listing.entity.PropertyType;
import com.project_x.user.entity.User;
import org.springframework.stereotype.Component;

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

    private PublicPropertyOwnerResponse toOwnerResponse(User owner) {
        if (owner == null) {
            return null;
        }

        return PublicPropertyOwnerResponse.builder()
                .id(owner.getId())
                .firstName(owner.getFirstname())
                .lastName(owner.getLastname())
                .avatar(owner.getAvatar())
                .emailVerified(owner.isEmailVerified())
                .build();
    }
}
