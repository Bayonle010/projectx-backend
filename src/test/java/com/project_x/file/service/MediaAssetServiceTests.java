package com.project_x.file.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.project_x.core.exception.BadRequestException;
import com.project_x.core.exception.ConflictException;
import com.project_x.file.MediaKind;
import com.project_x.file.MediaStatus;
import com.project_x.file.entity.MediaAsset;
import com.project_x.file.repository.MediaAssetRepository;
import com.project_x.listing.repository.ListingImageRepository;
import com.project_x.listing.repository.ListingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class MediaAssetServiceTests {
    private MediaAssetRepository assets;
    private MediaAssetService service;

    @BeforeEach
    void setUp() {
        assets = mock(MediaAssetRepository.class);
        Cloudinary cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", "test-cloud", "api_key", "test-key", "api_secret", "test-secret"));
        service = new MediaAssetService(cloudinary, assets,
                mock(ListingRepository.class), mock(ListingImageRepository.class),
                mock(com.project_x.user.service.UserService.class));
        ReflectionTestUtils.setField(service, "maxVideoBytes", 524288000L);
        ReflectionTestUtils.setField(service, "videoUploadPreset", "test-video-preset");
        when(assets.save(any(MediaAsset.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(assets.saveAndFlush(any(MediaAsset.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void authorizationUsesUnpredictableUserScopedPublicId() {
        UUID owner = UUID.randomUUID();
        var authorization = service.authorize(owner, MediaKind.VIDEO, "listings", "tour.mp4",
                100_000_000, UUID.randomUUID().toString());

        assertEquals("video", authorization.resourceType());
        assertEquals(360, authorization.maximumDurationSeconds());
        assertEquals(524288000L, authorization.maximumBytes());
        assertTrue(authorization.publicId().startsWith("projectx/users/" + owner + "/video/f-listings/"));
        assertEquals("false", authorization.overwrite());
        assertEquals("test-video-preset", authorization.uploadPreset());
        assertNotNull(authorization.signature());
        assertFalse(authorization.signature().isBlank());
        verify(assets).saveAndFlush(any(MediaAsset.class));
    }

    @Test
    void refusesLargeVideoAuthorizationWithoutACloudinaryPreset() {
        ReflectionTestUtils.setField(service, "videoUploadPreset", "");
        assertThrows(BadRequestException.class, () -> service.authorize(
                UUID.randomUUID(), MediaKind.VIDEO, "listings", "tour.mp4",
                100_000_000, UUID.randomUUID().toString()));
        verify(assets, never()).saveAndFlush(any());
    }

    @Test
    void rawDocumentPublicIdKeepsAValidatedExtension() {
        var authorization = service.authorize(UUID.randomUUID(), MediaKind.DOCUMENT,
                "ownership", "deed.PDF", 1000, UUID.randomUUID().toString());
        assertTrue(authorization.publicId().endsWith(".pdf"));
        assertThrows(BadRequestException.class, () -> service.authorize(
                UUID.randomUUID(), MediaKind.DOCUMENT, "ownership", "archive.exe",
                1000, UUID.randomUUID().toString()));
    }

    @Test
    void repeatedAuthorizationReusesTheSameReservation() {
        UUID owner = UUID.randomUUID();
        String idempotencyKey = UUID.randomUUID().toString();
        AtomicReference<MediaAsset> saved = new AtomicReference<>();
        when(assets.findByOwnerIdAndIdempotencyKey(owner, UUID.fromString(idempotencyKey)))
                .thenAnswer(invocation -> Optional.ofNullable(saved.get()));
        when(assets.saveAndFlush(any(MediaAsset.class))).thenAnswer(invocation -> {
            MediaAsset asset = invocation.getArgument(0);
            saved.set(asset);
            return asset;
        });

        var first = service.authorize(owner, MediaKind.VIDEO, "listings", "tour.mp4",
                100_000_000, idempotencyKey);
        var retry = service.authorize(owner, MediaKind.VIDEO, "listings", "tour.mp4",
                100_000_000, idempotencyKey);

        assertEquals(first.mediaId(), retry.mediaId());
        assertEquals(first.publicId(), retry.publicId());
        verify(assets, times(1)).saveAndFlush(any(MediaAsset.class));
    }

    @Test
    void refusesIdempotencyKeyReuseForDifferentUpload() {
        UUID owner = UUID.randomUUID();
        String idempotencyKey = UUID.randomUUID().toString();
        AtomicReference<MediaAsset> saved = new AtomicReference<>();
        when(assets.findByOwnerIdAndIdempotencyKey(owner, UUID.fromString(idempotencyKey)))
                .thenAnswer(invocation -> Optional.ofNullable(saved.get()));
        when(assets.saveAndFlush(any(MediaAsset.class))).thenAnswer(invocation -> {
            MediaAsset asset = invocation.getArgument(0);
            saved.set(asset);
            return asset;
        });

        service.authorize(owner, MediaKind.VIDEO, "listings", "tour.mp4",
                100_000_000, idempotencyKey);

        assertThrows(ConflictException.class, () -> service.authorize(
                owner, MediaKind.VIDEO, "listings", "different.mp4",
                120_000_000, idempotencyKey));
    }

    @Test
    void refusesForeignUserCompletion() {
        MediaAsset asset = pendingVideo(UUID.randomUUID());
        when(assets.findById(asset.getId())).thenReturn(Optional.of(asset));

        assertThrows(BadRequestException.class, () -> service.completeFromUploadResponse(
                UUID.randomUUID(), asset.getId(), Map.of()));
        verify(assets, never()).save(any());
    }

    @Test
    void rejectsVideoLongerThanSixMinutes() {
        MediaAsset asset = pendingVideo(UUID.randomUUID());
        when(assets.findById(asset.getId())).thenReturn(Optional.of(asset));

        assertThrows(BadRequestException.class, () -> service.completeFromUploadResponse(
                asset.getOwnerId(), asset.getId(), videoMetadata(asset, 361, 100_000_000)));
        assertEquals(MediaStatus.REJECTED, asset.getStatus());
        verify(assets).save(asset);
    }

    @Test
    void rejectsVideoAboveConfiguredByteLimit() {
        MediaAsset asset = pendingVideo(UUID.randomUUID());
        when(assets.findById(asset.getId())).thenReturn(Optional.of(asset));

        assertThrows(BadRequestException.class, () -> service.completeFromUploadResponse(
                asset.getOwnerId(), asset.getId(), videoMetadata(asset, 300, 524288001L)));
        assertEquals(MediaStatus.REJECTED, asset.getStatus());
    }

    @Test
    void acceptsVerifiedSixMinuteVideoAndRecordsItsOwner() {
        MediaAsset asset = pendingVideo(UUID.randomUUID());
        when(assets.findById(asset.getId())).thenReturn(Optional.of(asset));

        var response = service.completeFromUploadResponse(asset.getOwnerId(), asset.getId(),
                videoMetadata(asset, 360, 100_000_000));

        assertEquals(MediaStatus.READY, asset.getStatus());
        assertEquals(360, asset.getDurationSeconds());
        assertEquals(asset.getPublicId(), response.publicId());
        assertEquals(asset.getId(), response.mediaId());
        assertEquals(100_000_000L, asset.getSizeBytes());
    }

    private MediaAsset pendingVideo(UUID ownerId) {
        MediaAsset asset = new MediaAsset();
        asset.setId(UUID.randomUUID());
        asset.setOwnerId(ownerId);
        asset.setKind(MediaKind.VIDEO);
        asset.setStatus(MediaStatus.PENDING);
        asset.setPublicId("projectx/users/" + ownerId + "/video/listings/" + asset.getId());
        return asset;
    }

    private Map<String, Object> videoMetadata(MediaAsset asset, double duration, long bytes) {
        return Map.of(
                "public_id", asset.getPublicId(),
                "resource_type", "video",
                "bytes", bytes,
                "format", "mp4",
                "duration", duration,
                "secure_url", "https://res.cloudinary.com/test-cloud/video/upload/" + asset.getPublicId() + ".mp4"
        );
    }
}
