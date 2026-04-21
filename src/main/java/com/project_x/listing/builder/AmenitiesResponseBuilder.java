package com.project_x.listing.builder;

import com.project_x.listing.dto.response.AmenitiesResponse;
import com.project_x.listing.entity.Amenity;

public class AmenitiesResponseBuilder {
    public static AmenitiesResponse toDto(Amenity amenity){

        return AmenitiesResponse.builder()
                .id(amenity.getId())
                .name(amenity.getName())
                .imageUrl(amenity.getImageUrl())
                .imagePublicId(amenity.getImagePublicId())
                .build();

    }
}
