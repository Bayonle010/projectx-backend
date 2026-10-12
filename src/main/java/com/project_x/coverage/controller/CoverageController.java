package com.project_x.coverage.controller;

import com.project_x.core.response.ApiResponse;
import com.project_x.core.response.ResponseUtil;
import com.project_x.coverage.dto.CoverageStateResponse;
import com.project_x.coverage.service.CoverageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/coverage")
@RequiredArgsConstructor
public class CoverageController {

    private final CoverageService coverageService;

    @GetMapping("/states")
    public ResponseEntity<ApiResponse> getCoveredStates() {
        List<CoverageStateResponse> response =
                coverageService.getCoveredStates();

        return ResponseEntity.ok(
                ResponseUtil.success(
                        0,
                        "Coverage states retrieved",
                        "States with available properties retrieved successfully",
                        response,
                        null
                )
        );
    }
}
