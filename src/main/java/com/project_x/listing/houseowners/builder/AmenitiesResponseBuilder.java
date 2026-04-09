package com.project_x.listing.houseowners.builder;

import com.project_x.listing.houseowners.dto.response.AmenitiesResponse;
import com.project_x.listing.houseowners.entity.Amenities;

public class AmenitiesResponseBuilder {
    public static AmenitiesResponse toDto(Amenities amenities){

        return AmenitiesResponse.builder()
                .id(amenities.getId())
                .name(amenities.getName())
                .imageUrl(amenities.getImageUrl())
                .imagePublicId(amenities.getImagePublicId())
                .build();

    }
}
