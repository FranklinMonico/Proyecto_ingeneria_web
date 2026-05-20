package com.proyecto.Studentservices.controller;

import com.proyecto.Studentservices.dto.ApiResponse;
import com.proyecto.Studentservices.dto.DashboardResponse;
import com.proyecto.Studentservices.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<DashboardResponse>> getDashboard() {
        DashboardResponse dashboard = dashboardService.getDashboard();
        return ResponseEntity.ok(new ApiResponse<>(true, "Dashboard obtenido", dashboard));
    }
}