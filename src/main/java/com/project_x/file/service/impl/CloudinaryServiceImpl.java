package com.project_x.file.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;
import com.project_x.file.FileValidationUtil;
import com.project_x.file.dto.FileUploadResponse;
import com.project_x.file.service.FileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryServiceImpl implements FileService {

    private final Cloudinary cloudinary;

    private static final Logger log = LoggerFactory.getLogger(CloudinaryServiceImpl.class);

    public CloudinaryServiceImpl(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    @Override
    public FileUploadResponse uploadImage(MultipartFile file, String folderName) {
        FileValidationUtil.validateImage(file);
        return upload(file, folderName, "image", true);
    }

    @Override
    public FileUploadResponse uploadVideo(MultipartFile file, String folderName) {
        return null;
    }

    @Override
    public FileUploadResponse uploadDocument(MultipartFile file, String folderName) {
        return null;
    }

    @Override
    public void deleteFileByPublicId(String publicId, String resourceType) {

    }


    private FileUploadResponse upload(MultipartFile file, String folderName, String resourceType, boolean imageOptimized) {
        try {
            Map<?, ?> uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", folderName,
                            "resource_type", resourceType,
                            "use_filename", true,
                            "unique_filename", true
                    )
            );

            String publicId = (String) uploadResult.get("public_id");
            String secureUrl = (String) uploadResult.get("secure_url");
            String format = (String) uploadResult.get("format");

            String optimizedUrl = imageOptimized
                    ? buildOptimizedImageUrl(publicId)
                    : secureUrl;

            return FileUploadResponse.builder()
                    .publicId(publicId)
                    .originalUrl(secureUrl)
                    .optimizedUrl(optimizedUrl)
                    .resourceType(resourceType)
                    .format(format)
                    .build();


        } catch (IOException e) {
            log.error("Cloudinary upload failed for folder={}", folderName, e);
            throw new RuntimeException("File upload failed");
        }
    }

    private String buildOptimizedImageUrl(String publicId) {
        return cloudinary.url()
                .secure(true)
                .transformation(new Transformation()
                        .fetchFormat("auto")
                        .quality("auto"))
                .generate(publicId);
    }
}
