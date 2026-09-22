package com.project_x.file.entity;

import com.project_x.file.MediaEntityType;
import com.project_x.file.MediaUsageType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "media_asset_usages", indexes = {
        @Index(name = "idx_media_usage_asset", columnList = "media_asset_id"),
        @Index(name = "idx_media_usage_entity", columnList = "entity_type,entity_id")
}, uniqueConstraints = @UniqueConstraint(
        name = "uk_media_asset_usage",
        columnNames = {"media_asset_id", "entity_type", "entity_id", "usage_type"}
))
@Getter
@Setter
public class MediaAssetUsage {
    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "media_asset_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_media_usage_asset"))
    private MediaAsset mediaAsset;

    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", nullable = false, length = 30)
    private MediaEntityType entityType;

    @Column(name = "entity_id", nullable = false)
    private UUID entityId;

    @Enumerated(EnumType.STRING)
    @Column(name = "usage_type", nullable = false, length = 40)
    private MediaUsageType usageType;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
