package com.project_x.file.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.project_x.core.exception.BadRequestException;
import com.project_x.core.security.model.AuthenticationIdentity;
import com.project_x.file.FileValidationUtil;
import com.project_x.file.MediaKind;
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
    public FileUploadResponse uploadImage(MultipartFile file, String folderName, AuthenticationIdentity auth) {
        FileValidationUtil.validateImage(file);
        return upload(file, folderName, MediaKind.IMAGE, auth);
    }

    @Override
    public FileUploadResponse uploadVideo(MultipartFile file, String folderName, AuthenticationIdentity auth) {
        FileValidationUtil.validateVideo(file);
        return upload(file, folderName, MediaKind.VIDEO, auth);
    }

    @Override
    public FileUploadResponse uploadDocument(MultipartFile file, String folderName, AuthenticationIdentity auth) {
        FileValidationUtil.validateDocument(file);
        return upload(file, folderName, MediaKind.DOCUMENT, auth);
    }

    @Override
    public void deleteFileByPublicId(String publicId, String resourceType) {
        try {

            log.info("Deleting file: publicId={}, resourceType={}", publicId, resourceType);

            Map<?, ?> result = cloudinary.uploader().destroy(
                    publicId,
                    ObjectUtils.asMap("resource_type", resourceType)
            );

            log.info("Cloudinary delete result for publicId={}: {}", publicId, result);

            Object deleteResult = result.get("result");


            if (!"ok".equals(deleteResult) && !"not found".equals(deleteResult)) {
                throw new BadRequestException("Failed to delete file from Cloudinary");
            }

        } catch (IOException e) {
            log.error("Failed to delete file from Cloudinary. publicId={}", publicId, e);
            throw new RuntimeException("Failed to delete file");
        }
    }


    private FileUploadResponse upload(MultipartFile file, String folderName, MediaKind kind,
                                      AuthenticationIdentity auth) {
        var asset = mediaAssetService.reserve(auth, kind, folderName, file.getOriginalFilename());
        try {
            Map<?, ?> result = cloudinary.uploader().upload(
                    file.getBytes(),
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
            throw new RuntimeException("File upload failed");
        }
    }
}
