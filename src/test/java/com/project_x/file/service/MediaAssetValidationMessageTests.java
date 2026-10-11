package com.project_x.file.service;

import com.project_x.core.exception.BadRequestException;
import com.project_x.file.MediaKind;
import com.project_x.file.repository.MediaAssetRepository;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MediaAssetValidationMessageTests {

    @Test
    void unavailableMediaReturnsAUserFriendlyMessage() {
        MediaAssetRepository assets = (MediaAssetRepository)
                Proxy.newProxyInstance(
                        MediaAssetRepository.class.getClassLoader(),
                        new Class<?>[]{MediaAssetRepository.class},
                        (proxy, method, arguments) ->
                                method.getName().equals(
                                        "findByPublicIdAndOwnerId"
                                )
                                        ? Optional.empty()
                                        : null
                );
        MediaAssetService service = new MediaAssetService(
                null,
                assets,
                null,
                null,
                null,
                null,
                null
        );

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> service.requireOwnedReady(
                        UUID.randomUUID(),
                        "unavailable-image",
                        MediaKind.IMAGE
                )
        );

        assertEquals(
                "This media cannot be used. Please select a completed upload "
                        + "of the required type from your account",
                exception.getMessage()
        );
    }
}
