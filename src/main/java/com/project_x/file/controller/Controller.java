package com.project_x.file.controller;

import com.project_x.core.response.ApiResponse;
import com.project_x.core.response.ResponseUtil;
import com.project_x.file.dto.FileUploadResponse;
import com.project_x.file.service.FileService;
import org.apache.coyote.Response;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/auth")
public class Controller {
    private final FileService fileService;

    public Controller(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping(value = "/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse> uploadImage(
            @RequestPart("file") MultipartFile file,
            @RequestParam("folder") String folderName
    ) {
        FileUploadResponse response = fileService.uploadImage(file, folderName);
        return ResponseEntity.ok(
                ResponseUtil.success(0, "Image uploaded successfully", "", response,  null)
        );
    }
}
