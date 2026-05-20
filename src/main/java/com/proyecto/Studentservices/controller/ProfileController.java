package com.proyecto.Studentservices.controller;

import com.proyecto.Studentservices.dto.ApiResponse;
import com.proyecto.Studentservices.dto.UpdateProfileRequest;
import com.proyecto.Studentservices.model.Student;
import com.proyecto.Studentservices.service.ProfileService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Student>> getProfile() {
        Student student = profileService.getProfile();
        return ResponseEntity.ok(new ApiResponse<>(true, "Perfil obtenido", student));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<Student>> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request) {
        Student student = profileService.updateProfile(request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Perfil actualizado", student));
    }
}