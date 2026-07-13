package com.project_x.listing.builder;

import com.project_x.listing.dto.response.PropertyTypeResponse;
import com.project_x.listing.entity.PropertyType;
import org.springframework.stereotype.Component;

@Component
public class PropertyTypeResponseBuilder {

    public static PropertyTypeResponse toResponse(
            PropertyType propertyType
    ) {
        return new PropertyTypeResponse(
                propertyType.getId(),
                propertyType.getCode(),
                propertyType.getName(),
                propertyType.getDescription(),
                propertyType.isActive(),
                propertyType.getCreatedAt(),
                propertyType.getUpdatedAt()
        );
    }
}