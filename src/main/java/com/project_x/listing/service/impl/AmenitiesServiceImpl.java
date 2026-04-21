package com.project_x.listing.service.impl;

import com.project_x.core.exception.ResourceNotFoundException;
import com.project_x.file.service.impl.CloudinaryServiceImpl;
import com.project_x.listing.builder.AmenitiesResponseBuilder;
import com.project_x.listing.dto.request.AmenitiesRequest;
import com.project_x.listing.dto.response.AmenitiesResponse;
import com.project_x.listing.entity.Amenity;
import com.project_x.listing.event.dto.AmenityDeletedEVent;
import com.project_x.listing.repository.AmenityRepository;
import com.project_x.listing.service.AmenitiesService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class AmenitiesServiceImpl implements AmenitiesService {


    private final AmenityRepository amenityRepository;
    private final CloudinaryServiceImpl cloudinaryService;
    private final ApplicationEventPublisher applicationEventPublisher;

    public AmenitiesServiceImpl(AmenityRepository amenityRepository, CloudinaryServiceImpl cloudinaryService, ApplicationEventPublisher applicationEventPublisher) {
        this.amenityRepository = amenityRepository;
        this.cloudinaryService = cloudinaryService;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public AmenitiesResponse createAmenity(AmenitiesRequest request) {

        Amenity newAmenity = Amenity.builder()
                .name(request.name())
                .imageUrl(request.imageUrl())
                .imagePublicId(request.imagePublicId())
                .build();

        Amenity savedAmenity = amenityRepository.save(newAmenity);

        return AmenitiesResponseBuilder.toDto(savedAmenity);
    }

    @Override
    public List<AmenitiesResponse> getAllAmenities() {
        return amenityRepository.findAll()
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

        // 2. Delete from DB
        amenityRepository.delete(amenity);

        applicationEventPublisher.publishEvent(
                AmenityDeletedEVent.builder()
                        .publicId(amenity.getImagePublicId())
                        .resourceType("image")
                .build());
    }


}
