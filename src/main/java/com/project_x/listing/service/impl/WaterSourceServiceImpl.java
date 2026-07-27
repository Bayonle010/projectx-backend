package com.project_x.listing.service.impl;

import com.project_x.core.exception.BadRequestException;
import com.project_x.core.exception.ResourceNotFoundException;
import com.project_x.listing.builder.WaterSourceResponseBuilder;
import com.project_x.listing.dto.request.CreateWaterSourceRequest;
import com.project_x.listing.dto.request.UpdateWaterSourceRequest;
import com.project_x.listing.dto.response.WaterSourceResponse;
import com.project_x.listing.entity.WaterSource;
import com.project_x.listing.repository.ListingRepository;
import com.project_x.listing.repository.WaterSourceRepository;
import com.project_x.listing.service.WaterSourceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WaterSourceServiceImpl
        implements WaterSourceService {

    private final WaterSourceRepository waterSourceRepository;
    private final ListingRepository listingRepository;
    private final WaterSourceResponseBuilder responseBuilder;

    @Override
    @Transactional
    public WaterSourceResponse createWaterSource(
            CreateWaterSourceRequest request
    ) {
        String name = normalizeName(request.name());

        if (waterSourceRepository.existsByNameIgnoreCase(name)) {
            throw new BadRequestException(
                    "Water source already exists"
            );
        }

        String code = generateCode(name);

        if (waterSourceRepository.existsByCodeIgnoreCase(code)) {
            throw new BadRequestException(
                    "A water source with the generated code already exists"
            );
        }

        WaterSource waterSource = WaterSource.builder()
                .name(name)
                .code(code)
                .description(
                        normalizeDescription(request.description())
                )
                .active(true)
                .build();

        WaterSource savedWaterSource =
                waterSourceRepository.save(waterSource);

        return responseBuilder.toResponse(savedWaterSource);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WaterSourceResponse> getAllWaterSources(
            boolean includeInactive
    ) {
        List<WaterSource> waterSources = includeInactive
                ? waterSourceRepository.findAllByOrderByNameAsc()
                : waterSourceRepository
                .findAllByActiveTrueOrderByNameAsc();

        return waterSources.stream()
                .map(responseBuilder::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public WaterSourceResponse getWaterSourceById(UUID id) {
        WaterSource waterSource = findWaterSource(id);

        return responseBuilder.toResponse(waterSource);
    }

    @Override
    @Transactional
    public WaterSourceResponse updateWaterSource(
            UUID id,
            UpdateWaterSourceRequest request
    ) {
        WaterSource waterSource = findWaterSource(id);

        String name = normalizeName(request.name());

        boolean nameAlreadyExists =
                waterSourceRepository
                        .existsByNameIgnoreCaseAndIdNot(name, id);

        if (nameAlreadyExists) {
            throw new BadRequestException(
                    "Another water source already uses this name"
            );
        }

        waterSource.setName(name);
        waterSource.setDescription(
                normalizeDescription(request.description())
        );

        /*
         * Do not regenerate the code.
         * The code remains stable even if the display name changes.
         */
        WaterSource updatedWaterSource =
                waterSourceRepository.save(waterSource);

        return responseBuilder.toResponse(updatedWaterSource);
    }

    @Override
    @Transactional
    public WaterSourceResponse archiveWaterSource(UUID id) {
        WaterSource waterSource = findWaterSource(id);

        /*
         * Idempotent:
         * archiving an already archived water source still succeeds.
         */
        if (waterSource.isActive()) {
            waterSource.setActive(false);
            waterSourceRepository.save(waterSource);
        }

        return responseBuilder.toResponse(waterSource);
    }

    @Override
    @Transactional
    public WaterSourceResponse restoreWaterSource(UUID id) {
        WaterSource waterSource = findWaterSource(id);

        /*
         * Idempotent:
         * restoring an already active water source still succeeds.
         */
        if (!waterSource.isActive()) {
            waterSource.setActive(true);
            waterSourceRepository.save(waterSource);
        }

        return responseBuilder.toResponse(waterSource);
    }

    @Override
    @Transactional
    public void deleteWaterSource(UUID id) {
        WaterSource waterSource = findWaterSource(id);

        if (waterSource.isActive()) {
            throw new BadRequestException(
                    "Water source must be archived before it can be permanently deleted"
            );
        }

        boolean usedByListing =
                listingRepository.existsByWaterSources_Id(id);

        if (usedByListing) {
            throw new BadRequestException(
                    "Water source cannot be permanently deleted because it is used by one or more listings"
            );
        }

        waterSourceRepository.delete(waterSource);
    }

    private WaterSource findWaterSource(UUID id) {
        return waterSourceRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Water source not found"
                        )
                );
    }

    private String normalizeName(String name) {
        return name.trim()
                .replaceAll("\\s+", " ");
    }

    private String normalizeDescription(String description) {
        if (description == null || description.isBlank()) {
            return null;
        }

        return description.trim();
    }

    private String generateCode(String name) {
        return name.trim()
                .toUpperCase(Locale.ROOT)
                .replaceAll("[^A-Z0-9]+", "_")
                .replaceAll("^_+|_+$", "");
    }
}