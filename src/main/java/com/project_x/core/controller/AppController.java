package com.project_x.core.controller;

import com.project_x.core.response.ApiResponse;
import com.project_x.core.response.ResponseUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AppController {

    @GetMapping("/")
    public ResponseEntity<ApiResponse> health(){
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseUtil.success(0, "App is up and running", "visit the documentation, happy integration", "", ""));
    }
}
