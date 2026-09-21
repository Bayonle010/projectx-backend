package com.project_x.file.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;
import com.project_x.core.exception.BadRequestException;
import com.project_x.core.security.model.AuthenticationIdentity;
import com.project_x.file.MediaKind;
import com.project_x.file.MediaStatus;
import com.project_x.file.dto.DirectUploadAuthorization;
import com.project_x.file.dto.FileUploadResponse;
import com.project_x.file.entity.MediaAsset;
import com.project_x.file.repository.MediaAssetRepository;
import com.project_x.listing.repository.ListingImageRepository;
import com.project_x.listing.repository.ListingRepository;
import com.project_x.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.Instant;
import java.util.Locale;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MediaAssetService {
    private static final int MAX_VIDEO_DURATION_SECONDS = 360;
    private static final Set<String> VIDEO_FORMATS = Set.of("mp4", "mov", "mpeg", "mpg", "webm");
    private static final Set<String> IMAGE_FORMATS = Set.of("jpg", "jpeg", "png", "webp");
    private static final Set<String> DOCUMENT_FORMATS = Set.of("pdf", "doc", "docx", "xls", "xlsx", "txt");

    private final Cloudinary cloudinary;
    private final MediaAssetRepository mediaAssetRepository;
    private final ListingRepository listingRepository;
    private final ListingImageRepository listingImageRepository;
    private final UserService userService;

    @Value("${app.media.max-video-bytes:524288000}")
    private long maxVideoBytes;

    @Value("${app.media.video-upload-preset:}")
    private String videoUploadPreset;

    @Transactional
    public DirectUploadAuthorization authorize(AuthenticationIdentity auth, MediaKind kind,
                                                String folder, String fileName) {
        return authorize(ownerId(auth), kind, folder, fileName);
    }

    @Transactional
    public DirectUploadAuthorization authorize(UUID ownerId, MediaKind kind, String folder, String fileName) {
        if (kind == MediaKind.VIDEO && (videoUploadPreset == null || videoUploadPreset.isBlank())) {
            throw new BadRequestException("Cloudinary video upload preset is not configured");
        }
        MediaAsset asset = reserve(ownerId, kind, folder, fileName);
        long timestamp = Instant.now().getEpochSecond();
        String overwrite = "false";

        Map<String, Object> signedParameters = new HashMap<>(Map.of(
                "timestamp", timestamp, "public_id", asset.getPublicId(), "overwrite", overwrite));

        String uploadPreset = kind == MediaKind.VIDEO ? videoUploadPreset : null;
        if (uploadPreset != null) {
            signedParameters.put("upload_preset", uploadPreset);
        }
        String signature = cloudinary.apiSignRequest(
                signedParameters,
                cloudinary.config.apiSecret
        );
        return new DirectUploadAuthorization(
                asset.getId(),
                cloudinary.cloudinaryApiUrl("upload", ObjectUtils.asMap("resource_type", kind.resourceType())),
                cloudinary.config.cloudName,
                cloudinary.config.apiKey,
                timestamp,
                asset.getPublicId(),
                uploadPreset,
                overwrite,
                signature,
                kind.resourceType(),
                maximumBytes(kind),
                kind == MediaKind.VIDEO ? MAX_VIDEO_DURATION_SECONDS : 0
        );
    }

    @Transactional
    public MediaAsset reserve(AuthenticationIdentity auth, MediaKind kind, String folder, String fileName) {
        return reserve(ownerId(auth), kind, folder, fileName);
    }

    @Transactional
    public MediaAsset reserve(UUID ownerId, MediaKind kind, String folder, String fileName) {
        if (folder == null || !folder.matches("[A-Za-z0-9_-]{1,50}(?:/[A-Za-z0-9_-]{1,50}){0,3}")) {
            throw new BadRequestException("Folder must use up to four safe path segments");
        }
        MediaAsset asset = new MediaAsset();
        asset.setId(UUID.randomUUID());
        asset.setOwnerId(ownerId);
        asset.setKind(kind);
        asset.setStatus(MediaStatus.PENDING);
        String extension = kind == MediaKind.DOCUMENT ? documentExtension(fileName) : "";
        String safeFolder = "f-" + folder.replace("/", "/f-");
        asset.setPublicId("projectx/users/" + ownerId + "/" + kind.name().toLowerCase(Locale.ROOT)
                + "/" + safeFolder + "/" + asset.getId() + extension);
        return mediaAssetRepository.save(asset);
    }

    @Transactional(noRollbackFor = BadRequestException.class)
    public FileUploadResponse complete(AuthenticationIdentity auth, UUID mediaId) {
        return complete(ownerId(auth), mediaId);
    }

    @Transactional(noRollbackFor = BadRequestException.class)
    public FileUploadResponse complete(UUID ownerId, UUID mediaId) {
        MediaAsset asset = pendingOwned(ownerId, mediaId);
        if (asset.getStatus() == MediaStatus.READY) {
            return response(asset);
        }
        if (asset.getStatus() != MediaStatus.PENDING) {
            throw new BadRequestException("Upload was rejected");
        }

        Map<?, ?> metadata;
        try {
            metadata = cloudinary.api().resource(asset.getPublicId(), ObjectUtils.asMap(
                    "resource_type", asset.getKind().resourceType(),
                    "media_metadata", true
            ));
        } catch (Exception exception) {
            throw new BadRequestException("Upload has not completed on cloud");
        }

        return markReady(asset, metadata);
    }

    @Transactional(noRollbackFor = BadRequestException.class)
    public FileUploadResponse completeFromUploadResponse(AuthenticationIdentity auth, UUID mediaId,
                                                         Map<?, ?> uploadResponse) {
        return completeFromUploadResponse(ownerId(auth), mediaId, uploadResponse);
    }

    @Transactional(noRollbackFor = BadRequestException.class)
    public FileUploadResponse completeFromUploadResponse(UUID ownerId, UUID mediaId, Map<?, ?> uploadResponse) {
        MediaAsset asset = pendingOwned(ownerId, mediaId);
        if (asset.getStatus() != MediaStatus.PENDING) {
            throw new BadRequestException("Upload is not pending");
        }
        return markReady(asset, uploadResponse);
    }

    private FileUploadResponse markReady(MediaAsset asset, Map<?, ?> metadata) {
        try {
            validateCloudinaryMetadata(asset, metadata);
        } catch (BadRequestException exception) {
            asset.setStatus(MediaStatus.REJECTED);
            mediaAssetRepository.save(asset);
            throw exception;
        }
        asset.setSizeBytes(((Number) metadata.get("bytes")).longValue());
        asset.setFormat(metadata.get("format") == null ? null : metadata.get("format").toString());
        asset.setOriginalUrl(metadata.get("secure_url").toString());
        asset.setOptimizedUrl(asset.getKind() == MediaKind.IMAGE
                ? cloudinary.url().secure(true).transformation(new Transformation()
                        .fetchFormat("auto").quality("auto")).generate(asset.getPublicId())
                : asset.getOriginalUrl());
        if (asset.getKind() == MediaKind.VIDEO) {
            asset.setDurationSeconds(((Number) metadata.get("duration")).doubleValue());
        }
        asset.setStatus(MediaStatus.READY);
        return response(mediaAssetRepository.save(asset));
    }

    private MediaAsset pendingOwned(UUID ownerId, UUID mediaId) {
        return mediaAssetRepository.findById(mediaId)
                .filter(media -> media.getOwnerId().equals(ownerId))
                .orElseThrow(() -> new BadRequestException("Upload not found"));
    }

    @Transactional(readOnly = true)
    public MediaAsset requireOwnedReady(UUID ownerId, String publicId, MediaKind kind) {
        return mediaAssetRepository.findByPublicIdAndOwnerId(publicId, ownerId)
                .filter(asset -> asset.getStatus() == MediaStatus.READY && asset.getKind() == kind)
                .orElseThrow(() -> new BadRequestException("Media does not belong to this user or is not ready"));
    }

    @Transactional(readOnly = true)
    public MediaAsset requireOwnedDocumentUrl(UUID ownerId, String url) {
        return mediaAssetRepository.findByOriginalUrlAndOwnerId(url, ownerId)
                .filter(asset -> asset.getKind() == MediaKind.DOCUMENT && asset.getStatus() == MediaStatus.READY)
                .orElseThrow(() -> new BadRequestException("Document does not belong to this user"));
    }

    @Transactional
    public void deleteOwned(AuthenticationIdentity auth, String publicId, String resourceType) {
        deleteOwned(ownerId(auth), publicId, resourceType);
    }

    @Transactional
    public void deleteOwned(UUID ownerId, String publicId, String resourceType) {
        MediaAsset asset = mediaAssetRepository.findByPublicIdAndOwnerId(publicId, ownerId)
                .orElseThrow(() -> new BadRequestException("Media does not belong to this user"));
        if (!asset.getKind().resourceType().equals(resourceType)) {
            throw new BadRequestException("Incorrect resource type");
        }
        if (asset.getStatus() == MediaStatus.PENDING) {
            throw new BadRequestException("Upload is still pending");
        }
        if (listingImageRepository.existsByPublicId(publicId)
                || listingRepository.existsByVideoPublicId(publicId)
                || (asset.getOriginalUrl() != null
                    && listingRepository.existsByProofOfOwnershipUrl(asset.getOriginalUrl()))) {
            throw new BadRequestException("Remove this media from the listing before deleting it");
        }
        try {
            Map<?, ?> result = cloudinary.uploader().destroy(publicId,
                    ObjectUtils.asMap("resource_type", resourceType));
            if (!"ok".equals(result.get("result")) && !"not found".equals(result.get("result"))) {
                throw new BadRequestException("Failed to delete file from Cloudinary");
            }
        } catch (IOException exception) {
            throw new BadRequestException("Failed to delete file from Cloudinary");
        }
        mediaAssetRepository.delete(asset);
    }

    private void validateCloudinaryMetadata(MediaAsset asset, Map<?, ?> metadata) {
        if (!asset.getPublicId().equals(metadata.get("public_id"))
                || !asset.getKind().resourceType().equals(metadata.get("resource_type"))
                || !(metadata.get("bytes") instanceof Number bytes)
                || bytes.longValue() <= 0 || bytes.longValue() > maximumBytes(asset.getKind())
                || !(metadata.get("secure_url") instanceof String url)
                || !url.startsWith("https://")) {
            throw new BadRequestException("Uploaded media violates the upload policy");
        }
        String format = metadata.get("format") == null ? "" : metadata.get("format").toString().toLowerCase(Locale.ROOT);
        if (asset.getKind() == MediaKind.IMAGE && !IMAGE_FORMATS.contains(format)) {
            throw new BadRequestException("Unsupported image format");
        }
        if (asset.getKind() == MediaKind.DOCUMENT
                && !format.isEmpty()
                && !asset.getPublicId().endsWith("." + format)) {
            throw new BadRequestException("Unsupported document format");
        }
        if (asset.getKind() == MediaKind.VIDEO && (!VIDEO_FORMATS.contains(format)
                || !(metadata.get("duration") instanceof Number duration)
                || duration.doubleValue() <= 0 || duration.doubleValue() > MAX_VIDEO_DURATION_SECONDS)) {
            throw new BadRequestException("Video must be a supported format and at most 6 minutes long");
        }
    }

    private long maximumBytes(MediaKind kind) {
        return kind == MediaKind.VIDEO ? maxVideoBytes : kind.maximumBytes();
    }

    private String documentExtension(String fileName) {
        if (fileName == null) {
            throw new BadRequestException("Document fileName is required");
        }
        int dot = fileName.lastIndexOf('.');
        if (dot < 0) {
            throw new BadRequestException("Document file extension is required");
        }
        String extension = fileName.substring(dot + 1).toLowerCase(Locale.ROOT);
        if (!DOCUMENT_FORMATS.contains(extension)) {
            throw new BadRequestException("Unsupported document format");
        }
        return "." + extension;
    }

    private FileUploadResponse response(MediaAsset asset) {
        return FileUploadResponse.builder()
                .mediaId(asset.getId())
                .publicId(asset.getPublicId())
                .originalUrl(asset.getOriginalUrl())
                .optimizedUrl(asset.getOptimizedUrl())
                .resourceType(asset.getKind().resourceType())
                .format(asset.getFormat())
                .build();
    }

    private UUID ownerId(AuthenticationIdentity auth) {
        return userService.fetchAuthenticatedUser(auth).getId();
    }
}
