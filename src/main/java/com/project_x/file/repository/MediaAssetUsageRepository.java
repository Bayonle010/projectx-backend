package com.project_x.file.repository;

import com.project_x.file.MediaEntityType;
import com.project_x.file.MediaUsageType;
import com.project_x.file.entity.MediaAssetUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface MediaAssetUsageRepository extends JpaRepository<MediaAssetUsage, UUID> {
    boolean existsByMediaAsset_Id(UUID mediaAssetId);

    List<MediaAssetUsage> findByEntityTypeAndEntityId(MediaEntityType entityType, UUID entityId);

    @Modifying(flushAutomatically = true)
    @Query("""
            DELETE FROM MediaAssetUsage usage
            WHERE usage.entityType = :entityType
              AND usage.entityId = :entityId
              AND usage.usageType = :usageType
            """)
    int deleteByEntityTypeAndEntityIdAndUsageType(
            @Param("entityType") MediaEntityType entityType,
            @Param("entityId") UUID entityId,
            @Param("usageType") MediaUsageType usageType
    );

    @Modifying(flushAutomatically = true)
    @Query("""
            DELETE FROM MediaAssetUsage usage
            WHERE usage.entityType = :entityType
              AND usage.entityId = :entityId
            """)
    int deleteByEntityTypeAndEntityId(
            @Param("entityType") MediaEntityType entityType,
            @Param("entityId") UUID entityId
    );
}
