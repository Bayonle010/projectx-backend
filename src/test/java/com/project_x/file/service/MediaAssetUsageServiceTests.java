package com.project_x.file.service;

import com.project_x.file.MediaEntityType;
import com.project_x.file.MediaUsageType;
import com.project_x.file.entity.MediaAsset;
import com.project_x.file.entity.MediaAssetUsage;
import com.project_x.file.repository.MediaAssetUsageRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class MediaAssetUsageServiceTests {

    @Test
    void replaceRemovesOldUsageAndRegistersDistinctAssets() {
        MediaAssetUsageRepository repository = mock(MediaAssetUsageRepository.class);
        MediaAssetUsageService service = new MediaAssetUsageService(repository);
        UUID listingId = UUID.randomUUID();
        MediaAsset image = new MediaAsset();
        image.setId(UUID.randomUUID());

        service.replace(MediaEntityType.LISTING, listingId, MediaUsageType.IMAGE,
                List.of(image, image));

        verify(repository).deleteByEntityTypeAndEntityIdAndUsageType(
                MediaEntityType.LISTING, listingId, MediaUsageType.IMAGE);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<MediaAssetUsage>> usages = ArgumentCaptor.forClass(List.class);
        verify(repository).saveAll(usages.capture());
        assertEquals(1, usages.getValue().size());
        assertEquals(image.getId(), usages.getValue().getFirst().getMediaAsset().getId());
    }
}
