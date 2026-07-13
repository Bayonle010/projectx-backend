package com.project_x.listing.builder;

import com.project_x.listing.dto.response.WaterSourceResponse;
import com.project_x.listing.entity.WaterSource;
import org.springframework.stereotype.Component;

@Component
public class WaterSourceResponseBuilder {

    public WaterSourceResponse toResponse(
            WaterSource waterSource
    ) {
        return WaterSourceResponse.builder()
                .id(waterSource.getId())
                .code(waterSource.getCode())
                .name(waterSource.getName())
                .description(waterSource.getDescription())
                .active(waterSource.isActive())
                .createdAt(waterSource.getCreatedAt())
                .updatedAt(waterSource.getUpdatedAt())
                .build();
    }
}