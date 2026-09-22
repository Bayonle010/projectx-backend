package com.project_x.listing.service;

import com.project_x.adress.service.LocationService;
import com.project_x.core.exception.BadRequestException;
import com.project_x.core.security.model.AuthenticationIdentity;
import com.project_x.file.MediaKind;
import com.project_x.file.MediaEntityType;
import com.project_x.file.MediaUsageType;
import com.project_x.file.entity.MediaAsset;
import com.project_x.file.service.MediaAssetService;
import com.project_x.file.service.MediaAssetUsageService;
import com.project_x.listing.builder.ListingResponseBuilder;
import com.project_x.listing.dto.request.ImageRequest;
import com.project_x.listing.dto.request.SaveListingRequest;
import com.project_x.listing.entity.Listing;
import com.project_x.listing.repository.ListingRepository;
import com.project_x.listing.resolver.ListingReferenceResolver;
import com.project_x.listing.service.impl.ListingServiceImpl;
import com.project_x.listing.validation.ListingValidator;
import com.project_x.user.entity.User;
import com.project_x.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ListingMediaOwnershipTests {
    private ListingRepository listings;
    private MediaAssetService media;
    private MediaAssetUsageService usages;
    private ListingServiceImpl service;
    private UUID ownerId;
    private UUID listingId;
    private Listing listing;
    private AuthenticationIdentity identity;

    @BeforeEach
    void setUp() {
        listings = mock(ListingRepository.class);
        media = mock(MediaAssetService.class);
        usages = mock(MediaAssetUsageService.class);
        UserService users = mock(UserService.class);
        ownerId = UUID.randomUUID();
        listingId = UUID.randomUUID();
        User owner = new User();
        owner.setId(ownerId);
        identity = AuthenticationIdentity.builder().id(ownerId.toString()).build();
        when(users.fetchAuthenticatedUser(identity)).thenReturn(owner);
        listing = new Listing();
        listing.setId(listingId);
        when(listings.findByIdAndOwnerId(listingId, ownerId)).thenReturn(Optional.of(listing));
        when(listings.save(any(Listing.class))).thenAnswer(invocation -> invocation.getArgument(0));
        service = new ListingServiceImpl(listings, mock(ListingValidator.class), users,
                mock(ListingResponseBuilder.class), mock(LocationService.class),
                mock(ListingReferenceResolver.class), mock(ListingDescriptionGenerator.class),
                mock(ListingFriendlyIdGenerator.class), media, usages);
    }

    @Test
    void refusesAnImageNotOwnedByTheListingOwner() {
        String publicId = "another-user-image";
        when(media.requireOwnedReady(ownerId, publicId, MediaKind.IMAGE))
                .thenThrow(new BadRequestException("Media does not belong to this user"));
        SaveListingRequest request = SaveListingRequest.builder().id(listingId)
                .images(List.of(new ImageRequest(publicId, "https://forged.example/image", "image", "jpg")))
                .build();

        assertThrows(BadRequestException.class, () -> service.save(request, identity));
        verify(listings, never()).save(any());
    }

    @Test
    void ignoresClientSuppliedImageUrlAndPersistsTheVerifiedOne() {
        String publicId = "owned-image";
        MediaAsset asset = new MediaAsset();
        asset.setPublicId(publicId);
        asset.setKind(MediaKind.IMAGE);
        asset.setOptimizedUrl("https://res.cloudinary.com/verified-image");
        asset.setFormat("jpg");
        when(media.requireOwnedReady(ownerId, publicId, MediaKind.IMAGE)).thenReturn(asset);
        SaveListingRequest request = SaveListingRequest.builder().id(listingId)
                .images(List.of(new ImageRequest(publicId, "https://forged.example/image", "image", "jpg")))
                .build();

        service.save(request, identity);

        assertEquals("https://res.cloudinary.com/verified-image", listing.getImages().getFirst().getUrl());
        verify(usages).replace(eq(MediaEntityType.LISTING), eq(listingId),
                eq(MediaUsageType.IMAGE), eq(List.of(asset)));
    }

    @Test
    void doesNotAcceptAVideoUrlWithoutAVerifiedPublicId() {
        SaveListingRequest request = SaveListingRequest.builder().id(listingId)
                .videoUrl("https://forged.example/video.mp4")
                .build();

        assertThrows(BadRequestException.class, () -> service.save(request, identity));
        verify(listings, never()).save(any());
    }
}
