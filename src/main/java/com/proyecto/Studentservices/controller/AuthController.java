package com.proyecto.Studentservices.controller;

import com.proyecto.Studentservices.dto.AuthRequest;
import com.proyecto.Studentservices.dto.RegisterRequest;
import com.proyecto.Studentservices.model.Student;
import com.proyecto.Studentservices.repository.StudentRepository;
import com.proyecto.Studentservices.security.JwtUtil;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final StudentRepository repo;
    private final BCryptPasswordEncoder encoder;
    private final JwtUtil jwtUtil;

    public AuthController(StudentRepository repo, BCryptPasswordEncoder encoder, JwtUtil jwtUtil) {
        this.repo = repo;
        this.encoder = encoder;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    public String register(@RequestBody RegisterRequest request) {

        Student student = Student.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(encoder.encode(request.getPassword()))
                .build();

        repo.save(student);

        return "Usuario registrado";
    }

    @PostMapping("/login")
    public String login(@RequestBody AuthRequest request) {

        Student student = repo.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!encoder.matches(request.getPassword(), student.getPassword())) {
            throw new RuntimeException("Contraseña incorrecta");
        }

        return jwtUtil.generateToken(student.getEmail());
    }
}
