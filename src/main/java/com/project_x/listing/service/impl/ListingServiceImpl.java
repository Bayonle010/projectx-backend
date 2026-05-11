package com.project_x.listing.service.impl;

import com.project_x.core.exception.BadRequestException;
import com.project_x.core.exception.ResourceNotFoundException;
import com.project_x.core.security.model.AuthenticationIdentity;
import com.project_x.adress.entity.Lga;
import com.project_x.adress.entity.State;
import com.project_x.adress.service.LocationService;
import com.project_x.listing.builder.ListingResponseBuilder;
import com.project_x.listing.dto.request.SaveListingRequest;
import com.project_x.listing.dto.request.ImageRequest;
import com.project_x.listing.dto.response.ListingResponse;
import com.project_x.listing.entity.Amenity;
import com.project_x.listing.entity.Listing;
import com.project_x.listing.entity.ListingImage;
import com.project_x.listing.enums.ListingStatus;
import com.project_x.listing.repository.ListingRepository;
import com.project_x.listing.service.ListingService;
import com.project_x.listing.validation.ListingValidator;
import com.project_x.user.entity.User;
import com.project_x.user.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@RequiredArgsConstructor
@Service
public class ListingServiceImpl implements ListingService {
    private final ListingRepository listingRepository;
    private final ListingValidator listingValidator;
    private final UserService userService;
    private final ListingResponseBuilder listingResponseBuilder;
    private final LocationService locationService;

    @Override
    @Transactional
    public ListingResponse save(SaveListingRequest request, AuthenticationIdentity auth) {

        User owner = userService.fetchAuthenticatedUser(auth);

        Listing listing = request.id() == null
                ? createNewDraft(owner)
                : listingRepository.findByIdAndOwnerId(request.id(), owner.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Listing not found"));

        listingValidator.validateForDraftSave(request);

        applyChanges(listing, request);

        Listing saved = listingRepository.save(listing);
        return listingResponseBuilder.toResponse(saved);
    }

    @Override
    public ListingResponse submitForReview(UUID listingId, AuthenticationIdentity authenticationIdentity) {
        User owner = userService.fetchAuthenticatedUser(authenticationIdentity);

        Listing listing = listingRepository.findByIdAndOwnerId(listingId, owner.getId())
                .orElseThrow(()-> new ResourceNotFoundException("Listing not found"));

        if (listing.getStatus() != ListingStatus.DRAFT){
            throw new BadRequestException("Only draft listings can be submitted for review");
        }

        listingValidator.validateForSubmission(listing);

        listing.setStatus(ListingStatus.UNDER_REVIEW);

        Listing saved = listingRepository.save(listing);

        return listingResponseBuilder.toResponse(saved);
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

    private Listing createNewDraft(User owner) {
        return Listing.builder()
                .owner(owner)
                .status(ListingStatus.DRAFT)
                .amenities(new HashSet<>())
                .images(new ArrayList<>())
                .build();
    }


    private void applyChanges(Listing listing, SaveListingRequest request) {
        if (request.relationshipType() != null) {
            listing.setRelationshipType(request.relationshipType());
        }

        if (request.propertyType() != null) {
            listing.setPropertyType(request.propertyType());
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
            listing.setPropertyCondition(request.propertyCondition());
        }

        if (request.unitCount() != null) {
            listing.setUnitCount(request.unitCount());
        }

        if (request.description() != null) {
            listing.setDescription(request.description().trim());
        }

        if (request.waterSource() != null) {
            listing.setWaterSource(request.waterSource());
        }

        if (request.parkingAvailable() != null) {
            listing.setParkingAvailable(request.parkingAvailable());
        }

        if (request.fencedOrGated() != null) {
            listing.setFencedOrGated(request.fencedOrGated());
        }

        if (request.renovated() != null) {
            listing.setRenovated(request.renovated());
        }

        if (request.furnishingStatus() != null) {
            listing.setFurnishingStatus(request.furnishingStatus());
        }

        if (request.stateId() != null) {
            State state = locationService.findState(request.stateId());
            listing.setState(state);
        }

        if (request.lgaId() != null) {
            Lga lga = locationService.findLga(request.lgaId());
            listing.setLga(lga);
        }

        if (listing.getState() != null && listing.getLga() != null) {
            listingValidator.validateLgaBelongsToState(listing.getLga(), listing.getState());
        }

        if (request.addressLine() != null) {
            listing.setAddressLine(request.addressLine().trim());
        }

        if (request.landmark() != null) {
            listing.setLandmark(request.landmark());
        }

        if (request.latitude() != null) {
            listing.setLatitude(request.latitude());
        }

        if (request.longitude() != null) {
            listing.setLongitude(request.longitude());
        }

        if (request.placeId() != null) {
            listing.setPlaceId(request.placeId());
        }

        if (request.shareAddressWithSeekers() != null) {
            listing.setShareAddressWithSeekers(request.shareAddressWithSeekers());
        }

        if (request.rentAmount() != null) {
            listing.setRentAmount(request.rentAmount());
        }

        if (request.rentPaymentFrequency() != null) {
            listing.setRentPaymentFrequency(request.rentPaymentFrequency());
        }

        if (request.agencyFee() != null) {
            listing.setAgencyFee(request.agencyFee());
        }

        if (request.legalAgreementFee() != null) {
            listing.setLegalAgreementFee(request.legalAgreementFee());
        }

        if (request.cautionFee() != null) {
            listing.setCautionFee(request.cautionFee());
        }

        if (request.serviceCharge() != null) {
            listing.setServiceCharge(request.serviceCharge());
        }

        if (request.proofOfOwnershipUrl() != null) {
            listing.setProofOfOwnershipUrl(request.proofOfOwnershipUrl().trim());
        }

        if (request.videoUrl() != null) {
            listing.setVideoUrl(request.videoUrl().trim());
        }

        if (request.videoPublicId() != null) {
            listing.setVideoPublicId(request.videoPublicId());
        }

        if (request.amenityIds() != null) {
            Set<Amenity> amenities = listingValidator.resolveAmenities(request.amenityIds());
            listing.setAmenities(amenities);
        }

        if (request.images() != null) {
            listing.getImages().clear();
            attachImages(listing, request.images());
        }
    }

}
