package com.project_x.file.service;

import com.cloudinary.Cloudinary;
import com.project_x.core.security.model.AuthenticationIdentity;
import com.project_x.file.MediaKind;
import com.project_x.file.MediaStatus;
import com.project_x.file.dto.FileUploadResponse;
import com.project_x.file.entity.MediaAsset;
import com.project_x.file.service.impl.CloudinaryServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class CloudinaryServiceImplTests {

    @Test
    void readyIdempotentRetryDoesNotUploadToCloudinaryAgain() {
        Cloudinary cloudinary = mock(Cloudinary.class);
        MediaAssetService mediaAssetService = mock(MediaAssetService.class);
        CloudinaryServiceImpl service = new CloudinaryServiceImpl(cloudinary, mediaAssetService);
        AuthenticationIdentity auth = AuthenticationIdentity.builder().id("user").build();
        String idempotencyKey = UUID.randomUUID().toString();
        MockMultipartFile file = new MockMultipartFile(
                "file", "home.png", "image/png", new byte[]{1, 2, 3});

        MediaAsset asset = new MediaAsset();
        asset.setId(UUID.randomUUID());
        asset.setKind(MediaKind.IMAGE);
        asset.setStatus(MediaStatus.READY);
        FileUploadResponse expected = FileUploadResponse.builder()
                .mediaId(asset.getId())
                .publicId("projectx/users/user/image/home")
                .resourceType("image")
                .build();

        when(mediaAssetService.reserve(eq(auth), eq(MediaKind.IMAGE), eq("properties"),
                eq("home.png"), eq(idempotencyKey), anyString()))
                .thenReturn(new MediaAssetService.Reservation(asset, false));
        when(mediaAssetService.readyResponse(auth, asset.getId())).thenReturn(expected);

        FileUploadResponse actual = service.uploadImage(file, "properties", idempotencyKey, auth);

        assertSame(expected, actual);
        verify(cloudinary, never()).uploader();
    }
}
