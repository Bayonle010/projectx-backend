package com.project_x.file.repository;

import com.project_x.file.entity.MediaAsset;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface MediaAssetRepository extends JpaRepository<MediaAsset, UUID> {
    Optional<MediaAsset> findByOwnerIdAndIdempotencyKey(UUID ownerId, UUID idempotencyKey);
    Optional<MediaAsset> findByPublicIdAndOwnerId(String publicId, UUID ownerId);
    Optional<MediaAsset> findByOriginalUrlAndOwnerId(String originalUrl, UUID ownerId);
}
