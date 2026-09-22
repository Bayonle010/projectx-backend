package com.project_x.file.repository;

import com.project_x.file.MediaEntityType;
import com.project_x.file.MediaUsageType;
import com.project_x.file.entity.MediaAssetUsage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MediaAssetUsageRepository extends JpaRepository<MediaAssetUsage, UUID> {
    boolean existsByMediaAsset_Id(UUID mediaAssetId);

    List<MediaAssetUsage> findByEntityTypeAndEntityId(MediaEntityType entityType, UUID entityId);

    void deleteByEntityTypeAndEntityIdAndUsageType(
            MediaEntityType entityType, UUID entityId, MediaUsageType usageType);

    void deleteByEntityTypeAndEntityId(MediaEntityType entityType, UUID entityId);
}
