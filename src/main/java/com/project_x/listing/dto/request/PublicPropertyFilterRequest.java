package com.project_x.listing.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
public class PublicPropertyFilterRequest {

    private UUID stateId;

    private UUID lgaId;

    private String neighbourhood;

    private Set<UUID> propertyTypeIds = new LinkedHashSet<>();

    private Set<Integer> bedroomCounts = new LinkedHashSet<>();

    private Set<Integer> bathroomCounts = new LinkedHashSet<>();

    private Set<Integer> toiletCounts = new LinkedHashSet<>();

    private BigDecimal minRent;

    private BigDecimal maxRent;

    private Long page = 1L;

    private Long pageSize = 20L;
}
