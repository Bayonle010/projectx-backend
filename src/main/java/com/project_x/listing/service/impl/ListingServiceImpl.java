package com.project_x.listing.service.impl;

import com.project_x.core.exception.BadRequestException;
import com.project_x.core.exception.ResourceNotFoundException;
import com.project_x.core.paginationhelper.PaginationAdapters;
import com.project_x.core.security.model.AuthenticationIdentity;
import com.project_x.adress.entity.Lga;
import com.project_x.adress.entity.State;
import com.project_x.adress.service.LocationService;
import com.project_x.listing.builder.ListingResponseBuilder;
import com.project_x.listing.dto.request.SaveListingRequest;
import com.project_x.listing.dto.request.ImageRequest;
import com.project_x.listing.dto.response.ListingResponse;
import com.project_x.listing.dto.response.GeneratedListingDescriptionResponse;
import com.project_x.listing.entity.Amenity;
import com.project_x.listing.entity.Listing;
import com.project_x.listing.entity.ListingImage;
import com.project_x.listing.entity.WaterSource;
import com.project_x.listing.enums.ListingStatus;
import com.project_x.listing.repository.ListingRepository;
import com.project_x.listing.resolver.ListingReferenceResolver;
import com.project_x.listing.service.ListingService;
import com.project_x.listing.service.ListingDescriptionGenerator;
import com.project_x.listing.service.ListingFriendlyIdGenerator;
import com.project_x.listing.validation.ListingValidator;
import com.project_x.user.entity.User;
import com.project_x.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@RequiredArgsConstructor
@Service
public class ListingServiceImpl implements ListingService {
    private static final int FRIENDLY_ID_ALLOCATION_ATTEMPTS = 10;

    private final ListingRepository listingRepository;
    private final ListingValidator listingValidator;
    private final UserService userService;
    private final ListingResponseBuilder listingResponseBuilder;
    private final LocationService locationService;
    private final ListingReferenceResolver listingReferenceResolver;
    private final ListingDescriptionGenerator listingDescriptionGenerator;
    private final ListingFriendlyIdGenerator listingFriendlyIdGenerator;

    @Override
    @Transactional
    public ListingResponse  save(
            SaveListingRequest request,
            AuthenticationIdentity authenticationIdentity
    ) {
        User owner = userService.fetchAuthenticatedUser(
                authenticationIdentity
        );

        Listing listing = request.id() == null
                ? createNewDraft(owner)
                : listingRepository.findByIdAndOwnerId(
                request.id(),
                owner.getId()
        ).orElseThrow(() ->
                      new ResourceNotFoundException("Listing not found")
        );

        if (listing.getStatus() != ListingStatus.DRAFT) {
            throw new BadRequestException(
                    "Only draft listings can be edited"
            );
        }

        listingValidator.validateForDraftSave(request);

        applyChanges(listing, request);

        Listing savedListing = listingRepository.save(listing);

        return listingResponseBuilder.toResponse(savedListing);
    }

    @Override
    @Transactional
    public ListingResponse submitForReview(
            UUID listingId,
            AuthenticationIdentity authenticationIdentity
    ) {
        User owner = userService.fetchAuthenticatedUser(
                authenticationIdentity
        );

        Listing listing = listingRepository
                .findWithDetailsByIdAndOwnerId(
                        listingId,
                        owner.getId()
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException("Listing not found")
                );

        if (listing.getStatus() != ListingStatus.DRAFT) {
            throw new BadRequestException(
                    "Only draft listings can be submitted for review"
            );
        }

        /*
         * This is where property type and water source
         * become mandatory.
         */
        listingValidator.validateForSubmission(listing);

        listing.setStatus(ListingStatus.UNDER_REVIEW);

        Listing savedListing = listingRepository.save(listing);

        return listingResponseBuilder.toResponse(savedListing);
    }

    @Override
    public GeneratedListingDescriptionResponse generateDescription(
            UUID listingId,
            AuthenticationIdentity authenticationIdentity
    ) {
        User owner = userService.fetchAuthenticatedUser(authenticationIdentity);

        Listing listing = listingRepository
                .findWithDetailsByIdAndOwnerId(listingId, owner.getId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Listing not found")
                );

        if (listing.getStatus() != ListingStatus.DRAFT) {
            throw new BadRequestException(
                    "Descriptions can only be generated for draft listings"
            );
        }

        String description = listingDescriptionGenerator.generate(listing);

        return new GeneratedListingDescriptionResponse(
                listing.getId(),
                description
        );
    }

    @Transactional(readOnly = true)
    @Override
    public Page<ListingResponse> fetchListings(String status, Long page, Long pageSize, AuthenticationIdentity authenticationIdentity) {
        User owner = userService.fetchAuthenticatedUser(authenticationIdentity);

        Pageable pageable = PaginationAdapters.createPageRequestWithRecentFirstsSortOrder(page, pageSize);

        Page<Listing> listings;

        if (status == null || status.isBlank() || status.equalsIgnoreCase("ALL")) {
            listings = listingRepository.findByOwnerId(owner.getId(), pageable);
        } else {
            ListingStatus listingStatus = resolveListingStatus(status);

            listings = listingRepository.findByOwnerIdAndStatus(
                    owner.getId(),
                    listingStatus,
                    pageable
            );
        }

        return listings.map(listingResponseBuilder::toResponse);
    }

    @Transactional(readOnly = true)
    @Override
    public ListingResponse fetchListingById(
            UUID listingId,
            AuthenticationIdentity authenticationIdentity
    ) {
        User owner = userService.fetchAuthenticatedUser(authenticationIdentity);

        Listing listing = listingRepository.findByIdAndOwnerId(
                listingId,
                owner.getId()
        ).orElseThrow(() -> new ResourceNotFoundException("Listing not found"));

        return listingResponseBuilder.toResponse(listing);
    }

    @Override
    @Transactional
    public ListingResponse archiveProperty(
            UUID listingId,
            AuthenticationIdentity authenticationIdentity
    ) {
        User owner = userService.fetchAuthenticatedUser(authenticationIdentity);

        Listing listing = listingRepository.findByIdAndOwnerId(
                listingId,
                owner.getId()
        ).orElseThrow(() -> new ResourceNotFoundException("Listing not found"));

        if (listing.getStatus() != ListingStatus.ARCHIVED) {
            listing.setStatus(ListingStatus.ARCHIVED);
            listing = listingRepository.save(listing);
        }

        return listingResponseBuilder.toResponse(listing);
    }


    private void attachImages(Listing listing, List<ImageRequest> images) {
        for (int i = 0; i < images.size(); i++) {
            ImageRequest imageRequest = images.get(i);

            ListingImage image = ListingImage.builder()
                    .listing(listing)
                    .publicId(imageRequest.publicId())
                    .url(imageRequest.optimizedUrl())
                    .resourceType(imageRequest.resourceType())
                    .format(imageRequest.format())
                    .position(i)
                    .build();

            listing.getImages().add(image);
        }
    }

    private ListingStatus resolveListingStatus(String status) {
        try {
            return ListingStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new BadRequestException("Invalid listing status: " + status);
        }
    }

    private Listing createNewDraft(User owner) {
        for (int attempt = 0; attempt < FRIENDLY_ID_ALLOCATION_ATTEMPTS; attempt++) {
            UUID listingId = UUID.randomUUID();
            String friendlyId = listingFriendlyIdGenerator.generate();

            if (listingRepository.insertNewDraft(
                    listingId,
                    friendlyId,
                    owner.getId()
            ) == 1) {
                return listingRepository.findByIdAndOwnerId(
                        listingId,
                        owner.getId()
                ).orElseThrow(() ->
                        new IllegalStateException("Created listing could not be loaded")
                );
            }
        }

        throw new IllegalStateException(
                "Could not allocate a unique listing reference"
        );
    }


    private void applyChanges(
            Listing listing,
            SaveListingRequest request
    ) {
        if (request.relationshipType() != null) {
            listing.setRelationshipType(
                    request.relationshipType()
            );
        }

        if (request.propertyTypeId() != null) {
            listing.setPropertyType(
                    listingReferenceResolver.resolvePropertyType(
                            request.propertyTypeId()
                    )
            );
        }

        if (request.bedroomCount() != null) {
            listing.setBedroomCount(request.bedroomCount());
        }

        if (request.bathroomCount() != null) {
            listing.setBathroomCount(request.bathroomCount());
        }

        if (request.toiletCount() != null) {
            listing.setToiletCount(request.toiletCount());
        }

        if (request.propertyCondition() != null) {
            listing.setPropertyCondition(
                    request.propertyCondition()
            );
        }

        if (request.unitCount() != null) {
            listing.setUnitCount(request.unitCount());
        }

        if (request.description() != null) {
            listing.setDescription(
                    request.description().trim()
            );
        }

        if (request.waterSourceIds() != null) {
            Set<WaterSource> resolvedWaterSources =
                    listingReferenceResolver.resolveWaterSources(
                            request.waterSourceIds()
                    );

            listing.getWaterSources().clear();
            listing.getWaterSources().addAll(resolvedWaterSources);
        }

        if (request.parkingAvailable() != null) {
            listing.setParkingAvailable(
                    request.parkingAvailable()
            );
        }

        if (request.fencedOrGated() != null) {
            listing.setFencedOrGated(
                    request.fencedOrGated()
            );
        }

        if (request.renovated() != null) {
            listing.setRenovated(request.renovated());
        }

        if (request.furnishingStatus() != null) {
            listing.setFurnishingStatus(
                    request.furnishingStatus()
            );
        }

        if (request.stateId() != null) {
            State state = locationService.findState(
                    request.stateId()
            );

            listing.setState(state);
        }

        if (request.lgaId() != null) {
            Lga lga = locationService.findLga(
                    request.lgaId()
            );

            listing.setLga(lga);
        }

        if (
                listing.getState() != null
                        && listing.getLga() != null
        ) {
            listingValidator.validateLgaBelongsToState(
                    listing.getLga(),
                    listing.getState()
            );
        }

        if (request.neighbourhood() != null) {
            String neighbourhood = request.neighbourhood().trim();

            listing.setNeighbourhood(
                    neighbourhood.isBlank()
                            ? null
                            : neighbourhood
            );
        }

        if (request.addressLine() != null) {
            listing.setAddressLine(
                    request.addressLine().trim()
            );
        }

        if (request.landmark() != null) {
            listing.setLandmark(
                    request.landmark().trim()
            );
        }

        if (request.latitude() != null) {
            listing.setLatitude(request.latitude());
        }

        if (request.longitude() != null) {
            listing.setLongitude(request.longitude());
        }

        if (request.placeId() != null) {
            listing.setPlaceId(
                    request.placeId().trim()
            );
        }

        if (request.shareAddressWithSeekers() != null) {
            listing.setShareAddressWithSeekers(
                    request.shareAddressWithSeekers()
            );
        }

        if (request.rentAmount() != null) {
            listing.setRentAmount(request.rentAmount());
        }

        if (request.rentPaymentFrequency() != null) {
            listing.setRentPaymentFrequency(
                    request.rentPaymentFrequency()
            );
        }

        if (request.agencyFee() != null) {
            listing.setAgencyFee(request.agencyFee());
        }

        if (request.legalAgreementFee() != null) {
            listing.setLegalAgreementFee(
                    request.legalAgreementFee()
            );
        }

        if (request.cautionFee() != null) {
            listing.setCautionFee(request.cautionFee());
        }

        if (request.serviceCharge() != null) {
            listing.setServiceCharge(
                    request.serviceCharge()
            );
        }

        if (request.proofOfOwnershipUrl() != null) {
            listing.setProofOfOwnershipUrl(
                    request.proofOfOwnershipUrl().trim()
            );
        }

        if (request.videoUrl() != null) {
            listing.setVideoUrl(
                    request.videoUrl().trim()
            );
        }

        if (request.videoPublicId() != null) {
            listing.setVideoPublicId(
                    request.videoPublicId().trim()
            );
        }

        if (request.amenityIds() != null) {
            Set<Amenity> amenities =
                    listingReferenceResolver.resolveAmenities(
                            request.amenityIds()
                    );

            listing.setAmenities(amenities);
        }

        if (request.images() != null) {
            listing.getImages().clear();
            attachImages(listing, request.images());
        }
    }

}
