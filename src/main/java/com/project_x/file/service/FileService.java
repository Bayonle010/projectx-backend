package com.project_x.file.service;

import com.project_x.core.security.model.AuthenticationIdentity;
import com.project_x.file.dto.FileUploadResponse;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {
    FileUploadResponse uploadImage(MultipartFile file, String folderName, AuthenticationIdentity auth);

    FileUploadResponse uploadVideo(MultipartFile file, String folderName, AuthenticationIdentity auth);

    FileUploadResponse uploadDocument(MultipartFile file, String folderName, AuthenticationIdentity auth);

    void deleteFileByPublicId(String publicId, String resourceType);


}
