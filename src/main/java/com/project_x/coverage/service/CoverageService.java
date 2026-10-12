package com.project_x.coverage.service;

import com.project_x.coverage.dto.CoverageStateResponse;
import com.project_x.coverage.entity.StateCoverageMetadata;
import com.project_x.coverage.projection.StateCoverageCountProjection;
import com.project_x.coverage.repository.StateCoverageMetadataRepository;
import com.project_x.listing.enums.ListingStatus;
import com.project_x.listing.repository.ListingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CoverageService {

    private final ListingRepository listingRepository;
    private final StateCoverageMetadataRepository metadataRepository;

    public List<CoverageStateResponse> getCoveredStates() {
        List<StateCoverageCountProjection> coverageCounts =
                listingRepository.findStateCoverageByStatus(
                        ListingStatus.PUBLISHED
                );

        if (coverageCounts.isEmpty()) {
            return List.of();
        }

        List<UUID> stateIds = coverageCounts.stream()
                .map(StateCoverageCountProjection::getStateId)
                .toList();

        Map<UUID, StateCoverageMetadata> metadataByStateId =
                metadataRepository.findByState_IdIn(stateIds)
                        .stream()
                        .collect(Collectors.toMap(
                                metadata -> metadata.getState().getId(),
                                Function.identity()
                        ));

        return coverageCounts.stream()
                .map(count -> toCoverageEntry(
                        count,
                        metadataByStateId.get(count.getStateId())
                ))
                .sorted(Comparator
                        .comparingInt(CoverageEntry::displayOrder)
                        .thenComparing(
                                entry -> entry.response().displayName(),
                                String.CASE_INSENSITIVE_ORDER
                        ))
                .map(CoverageEntry::response)
                .toList();
    }

    private CoverageEntry toCoverageEntry(
            StateCoverageCountProjection count,
            StateCoverageMetadata metadata
    ) {
        String stateName = count.getStateName();
        String displayName = metadata == null
                ? stateName
                : valueOrFallback(metadata.getDisplayName(), stateName);
        String mapKey = metadata == null
                ? buildDefaultMapKey(stateName)
                : valueOrFallback(
                        metadata.getMapKey(),
                        buildDefaultMapKey(stateName)
                );
        String imageUrl = metadata == null
                ? null
                : normalizeOptional(metadata.getImageUrl());
        int displayOrder = metadata == null
                || metadata.getDisplayOrder() == null
                ? Integer.MAX_VALUE
                : metadata.getDisplayOrder();

        long availableListingCount =
                count.getAvailableListingCount() == null
                        ? 0L
                        : count.getAvailableListingCount();

        return new CoverageEntry(
                new CoverageStateResponse(
                        count.getStateId(),
                        stateName,
                        displayName,
                        mapKey,
                        imageUrl,
                        availableListingCount
                ),
                displayOrder
        );
    }

    private String valueOrFallback(String value, String fallback) {
        String normalized = normalizeOptional(value);
        return normalized == null ? fallback : normalized;
    }

    private String normalizeOptional(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private String buildDefaultMapKey(String stateName) {
        return stateName.trim()
                .toUpperCase(Locale.ROOT)
                .replaceAll("[^A-Z0-9]+", "_")
                .replaceAll("^_+|_+$", "");
    }

    private record CoverageEntry(
            CoverageStateResponse response,
            int displayOrder
    ) {
    }
}
