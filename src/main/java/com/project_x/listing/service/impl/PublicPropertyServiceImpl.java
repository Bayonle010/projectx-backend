package com.project_x.listing.service.impl;

import com.project_x.core.paginationhelper.PaginationAdapters;
import com.project_x.core.exception.ResourceNotFoundException;
import com.project_x.listing.builder.PublicPropertyResponseBuilder;
import com.project_x.listing.dto.request.PublicPropertyFilterRequest;
import com.project_x.listing.dto.response.PublicPropertyDetailsResponse;
import com.project_x.listing.dto.response.PublicPropertyResponse;
import com.project_x.listing.entity.Listing;
import com.project_x.listing.entity.ListingImage;
import com.project_x.listing.enums.ListingStatus;
import com.project_x.listing.repository.ListingImageRepository;
import com.project_x.listing.repository.ListingRepository;
import com.project_x.listing.repository.PublicPropertyLocationRepository;
import com.project_x.listing.repository.PublicPropertySearchHit;
import com.project_x.listing.service.PublicPropertyService;
import com.project_x.listing.service.NearbyLocationContextResolver;
import com.project_x.listing.specification.PublicPropertySpecification;
import com.project_x.listing.validation.PublicPropertyFilterValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PublicPropertyServiceImpl implements PublicPropertyService {

    private static final int COVER_IMAGE_POSITION = 0;

    private final ListingRepository listingRepository;
    private final ListingImageRepository listingImageRepository;
    private final PublicPropertyResponseBuilder responseBuilder;
    private final PublicPropertyFilterValidator filterValidator;
    private final PublicPropertyLocationRepository locationRepository;
    private final NearbyLocationContextResolver locationContextResolver;

    @Override
    public Page<PublicPropertyResponse> fetchPublishedProperties(
            PublicPropertyFilterRequest filter
    ) {
        filterValidator.validateAndNormalize(filter);
        resolveMissingLocationContext(filter);

        Pageable pageable = PaginationAdapters
                .createPageRequestWithRecentFirstsSortOrder(
                        filter.getPage(),
                        filter.getPageSize()
                );

        if (hasLocationContext(filter)) {
            return fetchByLocation(filter, pageable);
        }

        Page<Listing> listings = listingRepository.findAll(
                    PublicPropertySpecification.matching(filter),
                    pageable
            );

        Map<UUID, String> coverImageUrls = fetchCoverImageUrls(listings.getContent());

        return listings.map(listing -> responseBuilder.toResponse(listing, coverImageUrls.get(listing.getId())));
    }

    private Page<PublicPropertyResponse> fetchByLocation(
            PublicPropertyFilterRequest filter,
            Pageable pageable
    ) {
        Page<PublicPropertySearchHit> hitPage = locationRepository.search(filter, pageable);

        List<UUID> listingIds = hitPage.getContent().stream()
                .map(PublicPropertySearchHit::listingId)
                .toList();

        Map<UUID, Listing> listingsById = listingIds.isEmpty()
                ? Map.of()
                : listingRepository.findByIdIn(listingIds).stream()
                .collect(Collectors.toMap(Listing::getId, listing -> listing));

        List<Listing> orderedListings = listingIds.stream()
                .map(listingsById::get)
                .filter(java.util.Objects::nonNull)
                .toList();

        Map<UUID, String> coverImageUrls = fetchCoverImageUrls(orderedListings);

        List<PublicPropertyResponse> responses = hitPage.getContent().stream()
                .map(hit -> {
                    Listing listing = listingsById.get(hit.listingId());
                    if (listing == null) {
                        return null;
                    }
                    return responseBuilder.toResponse(
                            listing,
                            coverImageUrls.get(listing.getId()),
                            hit.distanceMeters(),
                            hit.locationMatchLevel()
                    );
                })
                .filter(java.util.Objects::nonNull)
                .toList();

        return new PageImpl<>(responses, pageable, hitPage.getTotalElements());
    }

    private boolean hasLocationContext(PublicPropertyFilterRequest filter) {
        return filter.getNearLatitude() != null
                || filter.getNearStateId() != null
                || filter.getNearLgaId() != null;
    }

    private void resolveMissingLocationContext(
            PublicPropertyFilterRequest filter
    ) {
        if (filter.getNearLatitude() == null || (filter.getNearStateId() != null && filter.getNearLgaId() != null)) {
            return;
        }

        NearbyLocationContextResolver.LocationContext context = locationContextResolver.resolve(filter.getNearLatitude(), filter.getNearLongitude());

        if (filter.getNearStateId() == null) {
            filter.setNearStateId(context.stateId());
        }

        UUID effectiveStateId = filter.getNearStateId();
        if (filter.getNearLgaId() == null
                && effectiveStateId != null
                && effectiveStateId.equals(context.stateId())) {
            filter.setNearLgaId(context.lgaId());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PublicPropertyDetailsResponse fetchPublishedProperty(
            UUID propertyId
    ) {
        Listing listing = listingRepository.findByIdAndStatus(propertyId, ListingStatus.PUBLISHED)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found"));

        return responseBuilder.toDetailsResponse(listing);
    }

    private Map<UUID, String> fetchCoverImageUrls(List<Listing> listings) {
        List<UUID> listingIds = listings.stream().map(Listing::getId).toList();

        if (listingIds.isEmpty()) {
            return Map.of();
        }

        return listingImageRepository.findByListing_IdInAndPosition(listingIds, COVER_IMAGE_POSITION)
                .stream()
                .collect(Collectors.toMap(
                        image -> image.getListing().getId(),
                        ListingImage::getUrl,
                        (first, ignored) -> first,
                        LinkedHashMap::new
                ));
    }
}
