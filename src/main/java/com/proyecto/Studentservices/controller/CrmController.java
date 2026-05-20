package com.proyecto.Studentservices.controller;

import com.proyecto.Studentservices.dto.ApiResponse;
import com.proyecto.Studentservices.service.CrmService;
import com.proyecto.Studentservices.util.SecurityUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/crm")
public class CrmController {

    private final CrmService crmService;

    public CrmController(CrmService crmService) {
        this.crmService = crmService;
    }

    @PostMapping("/sync")
    public ResponseEntity<ApiResponse<String>> sync() {
        String email = SecurityUtils.getCurrentUserEmail();
        crmService.syncStudent(email);
        return ResponseEntity.ok(new ApiResponse<>(true, "Sincronizado con EspoCRM", null));
    }
}