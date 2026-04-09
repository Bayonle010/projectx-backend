package com.project_x.listing.houseowners.service.impl;

import com.project_x.core.exception.ResourceNotFoundException;
import com.project_x.file.service.impl.CloudinaryServiceImpl;
import com.project_x.listing.houseowners.builder.AmenitiesResponseBuilder;
import com.project_x.listing.houseowners.dto.request.AmenitiesRequest;
import com.project_x.listing.houseowners.dto.response.AmenitiesResponse;
import com.project_x.listing.houseowners.entity.Amenities;
import com.project_x.listing.houseowners.event.dto.AmenityDeletedEVent;
import com.project_x.listing.houseowners.repository.AmenitiesRepository;
import com.project_x.listing.houseowners.service.AmenitiesService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class AmenitiesServiceImpl implements AmenitiesService {


    private final AmenitiesRepository amenitiesRepository;
    private final CloudinaryServiceImpl cloudinaryService;
    private final ApplicationEventPublisher applicationEventPublisher;

    public AmenitiesServiceImpl(AmenitiesRepository amenitiesRepository, CloudinaryServiceImpl cloudinaryService, ApplicationEventPublisher applicationEventPublisher) {
        this.amenitiesRepository = amenitiesRepository;
        this.cloudinaryService = cloudinaryService;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public AmenitiesResponse createAmenity(AmenitiesRequest request) {

        Amenities newAmenity = Amenities.builder()
                .name(request.name())
                .imageUrl(request.imageUrl())
                .imagePublicId(request.imagePublicId())
                .build();

        Amenities savedAmenity = amenitiesRepository.save(newAmenity);

        return AmenitiesResponseBuilder.toDto(savedAmenity);
    }

    @Override
    public List<AmenitiesResponse> getAllAmenities() {
        return amenitiesRepository.findAll()
                .stream()
                .map(AmenitiesResponseBuilder::toDto)
                .toList();

    }

    @Override
    public AmenitiesResponse getAmenityById(UUID id) {
        Amenities amenity = amenitiesRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Amenity not found with id: " + id));

        return AmenitiesResponseBuilder.toDto(amenity);
    }

    @Override
    @Transactional
    public void deleteAmenity(UUID id) {
        Amenities amenity = amenitiesRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Amenity not found with id: " + id));

        // 2. Delete from DB
        amenitiesRepository.delete(amenity);

        applicationEventPublisher.publishEvent(
                AmenityDeletedEVent.builder()
                        .publicId(amenity.getImagePublicId())
                        .resourceType("image")
                .build());
    }


}
