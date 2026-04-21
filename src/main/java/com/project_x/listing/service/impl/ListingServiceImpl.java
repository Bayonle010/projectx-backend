package com.project_x.listing.houseowners.service.impl;

import com.project_x.core.security.model.AuthenticationIdentity;
import com.project_x.listing.adress.entity.Lga;
import com.project_x.listing.adress.entity.State;
import com.project_x.listing.adress.repository.StateRepository;
import com.project_x.listing.adress.service.LocationService;
import com.project_x.listing.houseowners.builder.ListingResponseBuilder;
import com.project_x.listing.houseowners.dto.request.CreateListingRequest;
import com.project_x.listing.houseowners.dto.request.ImageRequest;
import com.project_x.listing.houseowners.dto.response.ListingResponse;
import com.project_x.listing.houseowners.entity.Amenity;
import com.project_x.listing.houseowners.entity.Listing;
import com.project_x.listing.houseowners.entity.ListingImage;
import com.project_x.listing.houseowners.repository.ListingRepository;
import com.project_x.listing.houseowners.service.ListingService;
import com.project_x.listing.houseowners.validation.ListingValidator;
import com.project_x.user.entity.User;
import com.project_x.user.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
    public ListingResponse create(CreateListingRequest request, AuthenticationIdentity auth) {
        listingValidator.validateForCreate(request);

        User owner = userService.fetchAuthenticatedUser(auth);
        State state = locationService.findState(request.stateId());
        Lga lga = locationService.findLga(request.lgaId());

        listingValidator.validateLgaBelongsToState(lga, state);
        Set<Amenity> amenities = listingValidator.resolveAmenities(request.amenityIds());

        Listing listing = buildListing(request, owner, state, lga, amenities);
        attachImages(listing, request.images());

        Listing saved = listingRepository.save(listing);
        return listingResponseBuilder.toResponse(saved);
    }

    private Listing buildListing(
            CreateListingRequest request,
            User owner,
            State state,
            Lga lga,
            Set<Amenity> amenities
    ) {
        return Listing.builder()
                .relationshipType(request.relationshipType())
                .propertyType(request.propertyType())
                .bedroomCount(request.bedroomCount())
                .bathroomCount(request.bathroomCount())
                .toiletCount(request.toiletCount())
                .propertyCondition(request.propertyCondition())
                .unitCount(request.unitCount())
                .description(request.description().trim())
                .waterSource(request.waterSource())
                .parkingAvailable(request.parkingAvailable())
                .fencedOrGated(request.fencedOrGated())
                .renovated(request.renovated())
                .furnishingStatus(request.furnishingStatus())
                .state(state)
                .lga(lga)
                .addressLine(request.addressLine().trim())
                .landmark(request.landmark())
                .latitude(request.latitude())
                .longitude(request.longitude())
                .placeId(request.placeId())
                .shareAddressWithSeekers(request.shareAddressWithSeekers())
                .rentAmount(request.rentAmount())
                .rentPaymentFrequency(request.rentPaymentFrequency())
                .agencyFee(request.agencyFee())
                .legalAgreementFee(request.legalAgreementFee())
                .cautionFee(request.cautionFee())
                .serviceCharge(request.serviceCharge())
                .proofOfOwnershipUrl(request.proofOfOwnershipUrl().trim())
                .videoUrl(request.videoUrl().trim())
                .owner(owner)
                .amenities(amenities)
                .images(new ArrayList<>())
                .build();
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

}
