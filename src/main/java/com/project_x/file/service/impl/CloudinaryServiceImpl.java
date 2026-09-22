package com.project_x.file.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.project_x.core.exception.BadRequestException;
import com.project_x.core.security.model.AuthenticationIdentity;
import com.project_x.file.FileValidationUtil;
import com.project_x.file.MediaKind;
import com.project_x.file.MediaStatus;
import com.project_x.file.UploadIdempotency;
import com.project_x.file.dto.FileUploadResponse;
import com.project_x.file.service.FileService;
import com.project_x.file.service.MediaAssetService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryServiceImpl implements FileService {

    private final Cloudinary cloudinary;
    private final MediaAssetService mediaAssetService;

    private static final Logger log = LoggerFactory.getLogger(CloudinaryServiceImpl.class);

    public CloudinaryServiceImpl(Cloudinary cloudinary, MediaAssetService mediaAssetService) {
        this.cloudinary = cloudinary;
        this.mediaAssetService = mediaAssetService;
    }

    @Override
    public FileUploadResponse uploadImage(MultipartFile file, String folderName, String idempotencyKey,
                                          AuthenticationIdentity auth) {
        FileValidationUtil.validateImage(file);
        return upload(file, folderName, MediaKind.IMAGE, idempotencyKey, auth);
    }

    @Override
    public FileUploadResponse uploadVideo(MultipartFile file, String folderName, String idempotencyKey,
                                          AuthenticationIdentity auth) {
        FileValidationUtil.validateVideo(file);
        return upload(file, folderName, MediaKind.VIDEO, idempotencyKey, auth);
    }

    @Override
    public FileUploadResponse uploadDocument(MultipartFile file, String folderName, String idempotencyKey,
                                             AuthenticationIdentity auth) {
        FileValidationUtil.validateDocument(file);
        return upload(file, folderName, MediaKind.DOCUMENT, idempotencyKey, auth);
    }

    private FileUploadResponse upload(MultipartFile file, String folderName, MediaKind kind,
                                      String idempotencyKey, AuthenticationIdentity auth) {
        byte[] fileBytes;
        try {
            fileBytes = file.getBytes();
        } catch (IOException exception) {
            throw new RuntimeException("Could not read uploaded file", exception);
        }

        String requestFingerprint = UploadIdempotency.fingerprint(
                fileBytes,
                "PROXY",
                kind.name(),
                folderName,
                file.getOriginalFilename(),
                file.getContentType(),
                Long.toString(file.getSize())
        );
        var reservation = mediaAssetService.reserve(auth, kind, folderName, file.getOriginalFilename(),
                idempotencyKey, requestFingerprint);
        var asset = reservation.asset();
        if (!reservation.created() && asset.getStatus() == MediaStatus.READY) {
            return mediaAssetService.readyResponse(auth, asset.getId());
        }

        try {
            Map<?, ?> result = cloudinary.uploader().upload(
                    fileBytes,
                    ObjectUtils.asMap(
                            "public_id", asset.getPublicId(),
                            "resource_type", kind.resourceType(),
                            "filename", file.getOriginalFilename(),
                            "overwrite", false
                    )
            );
            if (kind == MediaKind.VIDEO && !(result.get("duration") instanceof Number)) {
                return mediaAssetService.complete(auth, asset.getId());
            }
            return mediaAssetService.completeFromUploadResponse(auth, asset.getId(), result);
        } catch (IOException e) {
            log.error("Cloudinary upload failed for folder={}", folderName, e);
            try {
                return mediaAssetService.complete(auth, asset.getId());
            } catch (BadRequestException incompleteUpload) {
                e.addSuppressed(incompleteUpload);
            }
            throw new RuntimeException("File upload failed", e);
        }
    }
}
