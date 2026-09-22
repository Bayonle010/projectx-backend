package com.project_x.file.service;

import com.project_x.file.MediaEntityType;
import com.project_x.file.MediaUsageType;
import com.project_x.file.entity.MediaAsset;
import com.project_x.file.entity.MediaAssetUsage;
import com.project_x.file.repository.MediaAssetUsageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MediaAssetUsageService {
    private final MediaAssetUsageRepository mediaAssetUsageRepository;

    @Transactional
    public void replace(MediaEntityType entityType, UUID entityId, MediaUsageType usageType,
                        Collection<MediaAsset> assets) {
        mediaAssetUsageRepository.deleteByEntityTypeAndEntityIdAndUsageType(
                entityType, entityId, usageType);

        LinkedHashMap<UUID, MediaAsset> distinctAssets = assets.stream()
                .collect(Collectors.toMap(
                        MediaAsset::getId,
                        asset -> asset,
                        (first, ignored) -> first,
                        LinkedHashMap::new
                ));

        mediaAssetUsageRepository.saveAll(distinctAssets.values().stream()
                .map(asset -> usage(asset, entityType, entityId, usageType))
                .toList());
    }

    @Transactional
    public Set<UUID> detachAll(MediaEntityType entityType, UUID entityId) {
        Set<UUID> mediaIds = mediaAssetUsageRepository
                .findByEntityTypeAndEntityId(entityType, entityId)
                .stream()
                .map(usage -> usage.getMediaAsset().getId())
                .collect(Collectors.toSet());
        mediaAssetUsageRepository.deleteByEntityTypeAndEntityId(entityType, entityId);
        return mediaIds;
    }

    @Transactional(readOnly = true)
    public boolean isAttached(UUID mediaAssetId) {
        return mediaAssetUsageRepository.existsByMediaAsset_Id(mediaAssetId);
    }

    private MediaAssetUsage usage(MediaAsset asset, MediaEntityType entityType,
                                  UUID entityId, MediaUsageType usageType) {
        MediaAssetUsage usage = new MediaAssetUsage();
        usage.setId(UUID.randomUUID());
        usage.setMediaAsset(asset);
        usage.setEntityType(entityType);
        usage.setEntityId(entityId);
        usage.setUsageType(usageType);
        return usage;
    }
}
