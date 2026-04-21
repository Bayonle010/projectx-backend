package com.project_x.listing.dto.response;

import com.project_x.listing.enums.*;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Builder
public record ListingResponse(

        UUID id,

        ListingRelationshipType relationshipType,
        PropertyType propertyType,

        Integer bedroomCount,
        Integer bathroomCount,
        Integer toiletCount,

        PropertyCondition propertyCondition,
        Integer unitCount,

        String description,

        WaterSource waterSource,

        Boolean parkingAvailable,
        Boolean fencedOrGated,
        Boolean renovated,

        FurnishingStatus furnishingStatus,

        UUID stateId,
        String stateName,

        UUID lgaId,
        String lgaName,

        String addressLine,
        String landmark,

        Double latitude,
        Double longitude,
        String placeId,

        Boolean shareAddressWithSeekers,

        BigDecimal rentAmount,
        RentPaymentFrequency rentPaymentFrequency,

        BigDecimal agencyFee,
        BigDecimal legalAgreementFee,
        BigDecimal cautionFee,
        BigDecimal serviceCharge,

        String proofOfOwnershipUrl,

        Set<String> amenities,

        List<ImageResponse> images,

        String videoUrl,

        UUID ownerId,

        Instant createdAt,
        Instant updatedAt

) {}