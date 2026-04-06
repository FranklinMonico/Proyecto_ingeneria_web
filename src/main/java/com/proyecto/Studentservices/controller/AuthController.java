package com.proyecto.Studentservices.controller;

import com.proyecto.Studentservices.dto.ApiResponse;
import com.proyecto.Studentservices.dto.AuthRequest;
import com.proyecto.Studentservices.dto.RegisterRequest;
import com.proyecto.Studentservices.model.Student;
import com.proyecto.Studentservices.repository.StudentRepository;
import com.proyecto.Studentservices.security.JwtUtil;
import com.proyecto.Studentservices.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final StudentRepository repo;

    public AuthController(AuthService authService,
                          StudentRepository repo) {
        this.authService = authService;
        this.repo = repo;
    }


    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Revisa tu correo", null));

    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        String token = authService.login(request);
        return ResponseEntity.ok(new ApiResponse<>(true, "Login exitoso", token));
    }

    @GetMapping("/confirm")
    public ResponseEntity<?> confirm(@RequestParam String token) {
        authService.confirmAccount(token);
        return ResponseEntity.ok(new ApiResponse<>(true, "Cuenta confirmada", null));
    }
}
