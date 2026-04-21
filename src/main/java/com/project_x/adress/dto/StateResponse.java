package com.project_x.adress.dto;

import lombok.Builder;

import java.util.UUID;

@Builder
public record StateResponse(
        UUID id,
        String name
) {
}
