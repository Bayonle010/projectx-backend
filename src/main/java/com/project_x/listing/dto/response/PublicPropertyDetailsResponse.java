package com.project_x.listing.dto.response;

import com.project_x.listing.enums.FurnishingStatus;
import com.project_x.listing.enums.ListingRelationshipType;
import com.project_x.listing.enums.PropertyCondition;
import com.project_x.listing.enums.RentPaymentFrequency;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Builder
public record PublicPropertyDetailsResponse(
        UUID id,
        String friendlyId,
        ListingRelationshipType relationshipType,
        UUID propertyTypeId,
        String propertyTypeName,
        Integer bedroomCount,
        Integer bathroomCount,
        Integer toiletCount,
        PropertyCondition propertyCondition,
        Integer unitCount,
        String description,
        List<ListingWaterSourceResponse> waterSources,
        Boolean parkingAvailable,
        Boolean fencedOrGated,
        Boolean renovated,
        FurnishingStatus furnishingStatus,
        UUID stateId,
        String stateName,
        UUID lgaId,
        String lgaName,
        String neighbourhood,
        boolean exactAddressShared,
        String addressLine,
        String landmark,
        Double latitude,
        Double longitude,
        BigDecimal rentAmount,
        RentPaymentFrequency rentPaymentFrequency,
        BigDecimal agencyFee,
        BigDecimal legalAgreementFee,
        BigDecimal cautionFee,
        BigDecimal serviceCharge,
        Set<ListingAmenityResponse> amenities,
        List<PublicPropertyImageResponse> images,
        String videoUrl,
        PublicPropertyOwnerResponse owner,
        Instant createdAt,
        Instant updatedAt
) {
}
