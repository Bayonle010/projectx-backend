package com.project_x.listing.service;

import com.project_x.core.security.model.AuthenticationIdentity;
import com.project_x.file.MediaEntityType;
import com.project_x.file.MediaKind;
import com.project_x.file.MediaUsageType;
import com.project_x.file.entity.MediaAsset;
import com.project_x.file.service.MediaAssetService;
import com.project_x.file.service.MediaAssetUsageService;
import com.project_x.listing.dto.request.AmenitiesRequest;
import com.project_x.listing.entity.Amenity;
import com.project_x.listing.repository.AmenityRepository;
import com.project_x.listing.service.impl.AmenitiesServiceImpl;
import com.project_x.user.entity.User;
import com.project_x.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AmenityMediaLifecycleTests {
    private AmenityRepository amenities;
    private UserService users;
    private MediaAssetService media;
    private MediaAssetUsageService usages;
    private AmenitiesServiceImpl service;

    @BeforeEach
    void setUp() {
        amenities = mock(AmenityRepository.class);
        users = mock(UserService.class);
        media = mock(MediaAssetService.class);
        usages = mock(MediaAssetUsageService.class);
        service = new AmenitiesServiceImpl(amenities, users, media, usages);
    }

    @Test
    void createUsesVerifiedCanonicalImageAndRecordsUsage() {
        UUID ownerId = UUID.randomUUID();
        UUID mediaId = UUID.randomUUID();
        UUID amenityId = UUID.randomUUID();
        AuthenticationIdentity auth = AuthenticationIdentity.builder().id(ownerId.toString()).build();
        User owner = new User();
        owner.setId(ownerId);
        MediaAsset image = new MediaAsset();
        image.setId(mediaId);
        image.setKind(MediaKind.IMAGE);
        image.setPublicId("verified-public-id");
        image.setOptimizedUrl("https://res.cloudinary.com/verified-image");
        when(users.fetchAuthenticatedUser(auth)).thenReturn(owner);
        when(media.requireOwnedReady(ownerId, "verified-public-id", MediaKind.IMAGE)).thenReturn(image);
        when(amenities.save(any(Amenity.class))).thenAnswer(invocation -> {
            Amenity amenity = invocation.getArgument(0);
            amenity.setId(amenityId);
            return amenity;
        });

        var response = service.createAmenity(
                new AmenitiesRequest("Pool", "https://forged.example/image", "verified-public-id"), auth);

        assertEquals("https://res.cloudinary.com/verified-image", response.imageUrl());
        verify(usages).replace(MediaEntityType.AMENITY, amenityId,
                MediaUsageType.IMAGE, List.of(image));
    }

    @Test
    void deleteDeactivatesAmenityWithoutRemovingSharedImageOrUsage() {
        UUID amenityId = UUID.randomUUID();
        Amenity amenity = Amenity.builder()
                .id(amenityId)
                .name("Pool")
                .imagePublicId("managed-public-id")
                .imageUrl("https://res.cloudinary.com/image")
                .active(true)
                .build();
        when(amenities.findById(amenityId)).thenReturn(Optional.of(amenity));

        service.deleteAmenity(amenityId);

        assertFalse(amenity.isActive());
        verify(amenities).save(amenity);
        verifyNoInteractions(usages);
        verifyNoMoreInteractions(media);
    }

    @Test
    void getAllReturnsOnlyActiveAmenities() {
        Amenity activeAmenity = Amenity.builder()
                .id(UUID.randomUUID())
                .name("Pool")
                .active(true)
                .build();
        when(amenities.findAllByActiveTrue()).thenReturn(List.of(activeAmenity));

        var response = service.getAllAmenities();

        assertEquals(1, response.size());
        assertEquals(activeAmenity.getId(), response.get(0).id());
        verify(amenities).findAllByActiveTrue();
        verify(amenities, never()).findAll();
    }
}
