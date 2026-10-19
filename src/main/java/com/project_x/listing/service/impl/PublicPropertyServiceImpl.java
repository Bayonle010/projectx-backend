package com.project_x.listing.service.impl;

import com.project_x.core.paginationhelper.PaginationAdapters;
import com.project_x.listing.builder.PublicPropertyResponseBuilder;
import com.project_x.listing.dto.response.PublicPropertyResponse;
import com.project_x.listing.entity.Listing;
import com.project_x.listing.entity.ListingImage;
import com.project_x.listing.enums.ListingStatus;
import com.project_x.listing.repository.ListingImageRepository;
import com.project_x.listing.repository.ListingRepository;
import com.project_x.listing.service.PublicPropertyService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PublicPropertyServiceImpl implements PublicPropertyService {

    private static final int COVER_IMAGE_POSITION = 0;

    private final ListingRepository listingRepository;
    private final ListingImageRepository listingImageRepository;
    private final PublicPropertyResponseBuilder responseBuilder;

    @Override
    @Transactional(readOnly = true)
    public Page<PublicPropertyResponse> fetchPublishedProperties(
            Long page,
            Long pageSize
    ) {
        Pageable pageable = PaginationAdapters.createPageRequestWithRecentFirstsSortOrder(page, pageSize);

        Page<Listing> listings = listingRepository.findByStatus(ListingStatus.PUBLISHED, pageable);

        Map<UUID, String> coverImageUrls = fetchCoverImageUrls(listings.getContent());

        return listings.map(listing -> responseBuilder.toResponse(listing, coverImageUrls.get(listing.getId())));
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
