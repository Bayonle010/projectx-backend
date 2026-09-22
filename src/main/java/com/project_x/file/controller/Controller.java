package com.project_x.file.controller;

import com.project_x.core.response.ApiResponse;
import com.project_x.core.response.ResponseUtil;
import com.project_x.core.exception.BadRequestException;
import com.project_x.core.security.model.AuthenticationIdentity;
import com.project_x.file.MediaKind;
import com.project_x.file.dto.DirectUploadAuthorization;
import com.project_x.file.dto.FileDeleteRequest;
import com.project_x.file.dto.FileUploadResponse;
import com.project_x.file.service.FileService;
import com.project_x.file.service.MediaAssetService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Locale;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/files")
public class Controller {
    private final FileService fileService;
    private final MediaAssetService mediaAssetService;

    public Controller(FileService fileService, MediaAssetService mediaAssetService) {
        this.fileService = fileService;
        this.mediaAssetService = mediaAssetService;
    }

    @PostMapping(value = "/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse> uploadImage(
            @RequestPart("file") MultipartFile file,
            @RequestParam("folder") String folderName,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @RequestAttribute("AUTH_IDENTITY") AuthenticationIdentity auth
    ) {
        FileUploadResponse response = fileService.uploadImage(file, folderName, idempotencyKey, auth);
        return ResponseEntity.ok(
                ResponseUtil.success(0, "Image uploaded successfully", "", response,  null)
        );
    }

    @PostMapping(value = "/videos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse> uploadVideo(
            @RequestPart("file") MultipartFile file,
            @RequestParam("folder") String folderName,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @RequestAttribute("AUTH_IDENTITY") AuthenticationIdentity auth
    ) {
        FileUploadResponse response = fileService.uploadVideo(file, folderName, idempotencyKey, auth);
        return ResponseEntity.ok(
                ResponseUtil.success(0, "Video uploaded successfully","", response,null)
        );
    }

    @PostMapping(value = "/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse> uploadDocument(
            @RequestPart("file") MultipartFile file,
            @RequestParam("folder") String folderName,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @RequestAttribute("AUTH_IDENTITY") AuthenticationIdentity auth
    ) {
        FileUploadResponse response = fileService.uploadDocument(file, folderName, idempotencyKey, auth);
        return ResponseEntity.ok(
                ResponseUtil.success(0, "File uploaded successfully","", response,null)
        );
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse> deleteFile(@Valid @RequestBody FileDeleteRequest request,
                                                  @RequestAttribute("AUTH_IDENTITY") AuthenticationIdentity auth) {
        mediaAssetService.deleteOwned(auth, request.publicId(), request.resourceType());
        return ResponseEntity.ok(
                ResponseUtil.success(200, "File deleted successfully", "", null, null)
        );
    }

    @PostMapping("/direct/{kind}/authorize")
    public ResponseEntity<ApiResponse> authorizeDirectUpload(@PathVariable String kind,
                                                              @RequestParam("folder") String folder,
                                                              @RequestParam("fileName") String fileName,
                                                              @RequestParam("fileSize") long fileSize,
                                                              @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
                                                              @RequestAttribute("AUTH_IDENTITY") AuthenticationIdentity auth) {
        MediaKind mediaKind;
        try {
            mediaKind = MediaKind.valueOf(kind.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new BadRequestException("Unknown media kind");
        }
        DirectUploadAuthorization authorization = mediaAssetService.authorize(
                auth, mediaKind, folder, fileName, fileSize, idempotencyKey);
        return ResponseEntity.ok(ResponseUtil.success(0, "Upload authorized", "", authorization, null));
    }

    @PostMapping("/direct/{mediaId}/complete")
    public ResponseEntity<ApiResponse> completeDirectUpload(@PathVariable UUID mediaId,
                                                             @RequestAttribute("AUTH_IDENTITY") AuthenticationIdentity auth) {
        FileUploadResponse response = mediaAssetService.complete(auth, mediaId);
        return ResponseEntity.ok(ResponseUtil.success(0, "Upload verified", "", response, null));
    }
}
