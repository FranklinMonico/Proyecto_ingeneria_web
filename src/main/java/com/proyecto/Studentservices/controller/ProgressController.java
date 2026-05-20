package com.proyecto.Studentservices.controller;

import com.proyecto.Studentservices.dto.ApiResponse;
import com.proyecto.Studentservices.dto.CourseProgressDTO;
import com.proyecto.Studentservices.service.ProgressService;
import com.proyecto.Studentservices.util.SecurityUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/progress")
public class ProgressController {

    private final ProgressService progressService;

    public ProgressController(ProgressService progressService) {
        this.progressService = progressService;
    }

    @GetMapping("/{courseId}")
    public ResponseEntity<ApiResponse<CourseProgressDTO>> getProgress(
            @PathVariable String courseId) {
        String email = SecurityUtils.getCurrentUserEmail();
        CourseProgressDTO progress = progressService.getProgress(email, courseId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Progreso obtenido", progress));
    }
}