package com.project_x.listing.dto.request;

import com.project_x.listing.enums.*;
import com.project_x.listing.houseowners.enums.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Builder
public record CreateListingRequest(

        @NotNull
        ListingRelationshipType relationshipType,

        @NotNull
        PropertyType propertyType,

        @NotNull
        @Min(0)
        Integer bedroomCount,

        @NotNull
        @Min(0)
        Integer bathroomCount,

        @NotNull
        @Min(0)
        Integer toiletCount,

        @NotNull
        PropertyCondition propertyCondition,

        @NotNull
        @Min(1)
        Integer unitCount,

        @NotBlank
        String description,

        @NotNull
        WaterSource waterSource,

        @NotNull
        Boolean parkingAvailable,

        @NotNull
        Boolean fencedOrGated,

        @NotNull
        Boolean renovated,

        @NotNull
        FurnishingStatus furnishingStatus,

        @NotNull
        UUID stateId,

        @NotNull
        UUID lgaId,

        @NotBlank
        @Size(max = 500)
        String addressLine,

        String landmark,

        Double latitude,
        Double longitude,
        String placeId,

        @NotNull
        Boolean shareAddressWithSeekers,

        @NotNull
        @DecimalMin(value = "0.0", inclusive = false)
        BigDecimal rentAmount,

        @NotNull
        RentPaymentFrequency rentPaymentFrequency,

        @DecimalMin(value = "0.0")
        BigDecimal agencyFee,

        @DecimalMin(value = "0.0")
        BigDecimal legalAgreementFee,

        @DecimalMin(value = "0.0")
        BigDecimal cautionFee,

        @DecimalMin(value = "0.0")
        BigDecimal serviceCharge,

        @NotBlank
        String proofOfOwnershipUrl,

        Set<UUID> amenityIds,

        @NotNull
        @Size(min = 6)
        List<@Valid ImageRequest> images,

        @NotBlank
        String videoUrl

) {}