package com.project_x.listing.resolver;

import com.project_x.core.exception.BadRequestException;
import com.project_x.listing.entity.Amenity;
import com.project_x.listing.repository.AmenityRepository;
import com.project_x.listing.repository.PropertyTypeRepository;
import com.project_x.listing.repository.WaterSourceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ListingReferenceResolverTests {
    private AmenityRepository amenities;
    private ListingReferenceResolver resolver;

    @BeforeEach
    void setUp() {
        amenities = mock(AmenityRepository.class);
        resolver = new ListingReferenceResolver(
                mock(PropertyTypeRepository.class),
                mock(WaterSourceRepository.class),
                amenities
        );
    }

    @Test
    void inactiveAmenityCannotBeNewlySelected() {
        UUID amenityId = UUID.randomUUID();
        Amenity inactiveAmenity = Amenity.builder()
                .id(amenityId)
                .name("Pool")
                .active(false)
                .build();
        when(amenities.findAllByIdIn(Set.of(amenityId)))
                .thenReturn(List.of(inactiveAmenity));

        assertThrows(BadRequestException.class,
                () -> resolver.resolveAmenities(Set.of(amenityId), Set.of()));
    }

    @Test
    void inactiveAmenityCanRemainOnExistingListing() {
        UUID amenityId = UUID.randomUUID();
        Amenity inactiveAmenity = Amenity.builder()
                .id(amenityId)
                .name("Pool")
                .active(false)
                .build();
        when(amenities.findAllByIdIn(Set.of(amenityId)))
                .thenReturn(List.of(inactiveAmenity));

        Set<Amenity> resolved = resolver.resolveAmenities(
                Set.of(amenityId),
                Set.of(inactiveAmenity)
        );

        assertEquals(Set.of(inactiveAmenity), resolved);
    }
}
