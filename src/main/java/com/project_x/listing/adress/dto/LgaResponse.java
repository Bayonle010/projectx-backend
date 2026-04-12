package com.project_x.listing.adress.dto;

import lombok.Builder;

import java.util.UUID;

@Builder
public record LgaResponse (
        UUID id,
        String name
){
}
