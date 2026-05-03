package com.project_x.listing.dto.request;

import com.project_x.listing.enums.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Builder
public record SaveListingRequest(

        UUID id,

        ListingRelationshipType relationshipType,

        PropertyType propertyType,

        @Min(0)
        Integer bedroomCount,

        @Min(0)
        Integer bathroomCount,

        @Min(0)
        Integer toiletCount,

        PropertyCondition propertyCondition,

        @Min(1)
        Integer unitCount,

        String description,

        WaterSource waterSource,

        Boolean parkingAvailable,

        Boolean fencedOrGated,

        Boolean renovated,

        FurnishingStatus furnishingStatus,

        UUID stateId,

        UUID lgaId,

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

        Set<UUID> amenityIds,

        @Size(min = 6)
        List<@Valid ImageRequest> images,

        String videoUrl,

        String videoPublicId

) {}