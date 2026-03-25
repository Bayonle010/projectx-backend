package com.project_x.file.service;

import com.project_x.file.dto.FileUploadResponse;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {
    FileUploadResponse uploadImage(MultipartFile file, String folderName);

    FileUploadResponse uploadVideo(MultipartFile file, String folderName);

    FileUploadResponse uploadDocument(MultipartFile file, String folderName);

    void deleteFileByPublicId(String publicId, String resourceType);


}
