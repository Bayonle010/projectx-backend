package com.project_x.file;

import com.project_x.core.exception.BadRequestException;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

public class FileValidationUtil {

    private static final Set<String> IMAGE_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    private static final Set<String> VIDEO_TYPES = Set.of(
            "video/mp4",
            "video/mpeg",
            "video/quicktime"
    );

    private static final Set<String> DOCUMENT_TYPES = Set.of(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "text/plain"
    );

    private static final long IMAGE_MAX_SIZE = 5 * 1024 * 1024;      // 5MB
    private static final long VIDEO_MAX_SIZE = 50 * 1024 * 1024;     // 50MB
    private static final long DOCUMENT_MAX_SIZE = 10 * 1024 * 1024;  // 10MB

    public static void validateImage(MultipartFile file) {
        validate(file, IMAGE_TYPES, IMAGE_MAX_SIZE,
                "Invalid image type. Allowed types: JPEG, PNG, WEBP");
    }

    public static void validateVideo(MultipartFile file) {
        validate(file, VIDEO_TYPES, VIDEO_MAX_SIZE,
                "Invalid video type. Allowed types: MP4, MPEG, MOV, AVI, WEBM");
    }

    public static void validateDocument(MultipartFile file) {
        validate(file, DOCUMENT_TYPES, DOCUMENT_MAX_SIZE,
                "Invalid document type. Allowed types: PDF, DOC, DOCX, XLS, XLSX, TXT");
    }

    private static void validate(MultipartFile file, Set<String> allowedTypes, long maxSize, String invalidTypeMessage) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File must not be empty");
        }

        String contentType = file.getContentType();
        if (contentType == null || !allowedTypes.contains(contentType)) {
            throw new BadRequestException(invalidTypeMessage);
        }

        if (file.getSize() > maxSize) {
            throw new BadRequestException("File size exceeds allowed limit");
        }
    }
}
