package com.project_x.listing.dto.response;

import com.project_x.listing.enums.ListingRelationshipType;
import com.project_x.listing.enums.RentPaymentFrequency;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Builder
public record PublicPropertyResponse(
        UUID id,
        String friendlyId,
        ListingRelationshipType relationshipType,
        UUID propertyTypeId,
        String propertyTypeName,
        Integer bedroomCount,
        Integer bathroomCount,
        Integer toiletCount,
        UUID stateId,
        String stateName,
        UUID lgaId,
        String lgaName,
        String neighbourhood,
        BigDecimal rentAmount,
        RentPaymentFrequency rentPaymentFrequency,
        String coverImageUrl,
        PublicPropertyOwnerResponse owner,
        Instant createdAt
) {
}
