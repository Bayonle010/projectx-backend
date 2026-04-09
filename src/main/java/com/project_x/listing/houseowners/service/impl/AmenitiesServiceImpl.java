package com.project_x.listing.houseowners.service.impl;

import com.project_x.listing.houseowners.builder.AmenitiesResponseBuilder;
import com.project_x.listing.houseowners.dto.request.AmenitiesRequest;
import com.project_x.listing.houseowners.dto.response.AmenitiesResponse;
import com.project_x.listing.houseowners.entity.Amenities;
import com.project_x.listing.houseowners.repository.AmenitiesRepository;
import com.project_x.listing.houseowners.service.AmenitiesService;
import org.springframework.stereotype.Service;

@Service
public class AmenitiesServiceImpl implements AmenitiesService {

    private final AmenitiesRepository amenitiesRepository;

    public AmenitiesServiceImpl(AmenitiesRepository amenitiesRepository) {
        this.amenitiesRepository = amenitiesRepository;
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
}
