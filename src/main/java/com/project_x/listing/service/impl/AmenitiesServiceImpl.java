package com.project_x.listing.service.impl;

import com.project_x.core.exception.ResourceNotFoundException;
import com.project_x.core.security.model.AuthenticationIdentity;
import com.project_x.file.MediaEntityType;
import com.project_x.file.MediaKind;
import com.project_x.file.MediaUsageType;
import com.project_x.file.entity.MediaAsset;
import com.project_x.file.service.MediaAssetService;
import com.project_x.file.service.MediaAssetUsageService;
import com.project_x.listing.builder.AmenitiesResponseBuilder;
import com.project_x.listing.dto.request.AmenitiesRequest;
import com.project_x.listing.dto.response.AmenitiesResponse;
import com.project_x.listing.entity.Amenity;
import com.project_x.listing.repository.AmenityRepository;
import com.project_x.listing.service.AmenitiesService;
import com.project_x.user.entity.User;
import com.project_x.user.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class AmenitiesServiceImpl implements AmenitiesService {


    private final AmenityRepository amenityRepository;
    private final UserService userService;
    private final MediaAssetService mediaAssetService;
    private final MediaAssetUsageService mediaAssetUsageService;

    public AmenitiesServiceImpl(AmenityRepository amenityRepository,
                                UserService userService,
                                MediaAssetService mediaAssetService,
                                MediaAssetUsageService mediaAssetUsageService) {
        this.amenityRepository = amenityRepository;
        this.userService = userService;
        this.mediaAssetService = mediaAssetService;
        this.mediaAssetUsageService = mediaAssetUsageService;
    }

    @Override
    @Transactional
    public AmenitiesResponse createAmenity(AmenitiesRequest request, AuthenticationIdentity auth) {
        User owner = userService.fetchAuthenticatedUser(auth);
        MediaAsset image = mediaAssetService.requireOwnedReady(
                owner.getId(), request.imagePublicId(), MediaKind.IMAGE);

        Amenity newAmenity = Amenity.builder()
                .name(request.name())
                .imageUrl(image.getOptimizedUrl())
                .imagePublicId(image.getPublicId())
                .active(true)
                .build();

        Amenity savedAmenity = amenityRepository.save(newAmenity);
        mediaAssetUsageService.replace(MediaEntityType.AMENITY, savedAmenity.getId(),
                MediaUsageType.IMAGE, List.of(image));

        return AmenitiesResponseBuilder.toDto(savedAmenity);
    }

    @Override
    public List<AmenitiesResponse> getAllAmenities() {
        return amenityRepository.findAllByActiveTrue()
                .stream()
                .map(AmenitiesResponseBuilder::toDto)
                .toList();

    }

    @Override
    public AmenitiesResponse getAmenityById(UUID id) {
        Amenity amenity = amenityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Amenity not found with id: " + id));

        return AmenitiesResponseBuilder.toDto(amenity);
    }

    @Override
    @Transactional
    public void deleteAmenity(UUID id) {
        Amenity amenity = amenityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Amenity not found with id: " + id));

        amenity.setActive(false);
        amenityRepository.save(amenity);
    }


}
