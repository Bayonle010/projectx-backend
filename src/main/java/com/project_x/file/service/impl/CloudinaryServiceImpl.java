package com.project_x.file.service.impl;

import com.project_x.file.dto.FileUploadResponse;
import com.project_x.file.service.FileService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class CloudinaryServiceImpl implements FileService {
    @Override
    public FileUploadResponse uploadImage(MultipartFile file, String folderName) {
        return null;
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
}
