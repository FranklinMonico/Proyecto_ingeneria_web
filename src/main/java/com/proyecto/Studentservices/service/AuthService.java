package com.proyecto.Studentservices.service;

import com.proyecto.Studentservices.client.EspoCrmClient;
import com.proyecto.Studentservices.dto.AuthRequest;
import com.proyecto.Studentservices.dto.RegisterRequest;
import com.proyecto.Studentservices.model.Student;
import com.proyecto.Studentservices.repository.StudentRepository;
import com.proyecto.Studentservices.security.JwtUtil;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;

@Service
public class AuthService {

    private final StudentRepository repository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;
    private final EspoCrmClient espoCrmClient;

    public AuthService(StudentRepository repository,
                       BCryptPasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil,
                       EmailService emailService,
                       EspoCrmClient espoCrmClient) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.emailService = emailService;
        this.espoCrmClient = espoCrmClient;
    }

    public void register(RegisterRequest request) {

        if (repository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("El correo ya está registrado");
        }

        String token = UUID.randomUUID().toString();

        Student student = Student.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .verificationToken(token)
                .enabled(false)
                .build();

        repository.save(student);
        try {
            espoCrmClient.createContact(
                    request.getName(),
                    request.getEmail()
            );
        } catch (Exception e) {
            System.out.println("Error EspoCRM al registrar: " + e.getMessage());
        }

        // 🔥 PROTEGER EMAIL
        try {
            emailService.sendVerificationEmail(
                    student.getEmail(),
                    student.getName(),
                    token
            );
        } catch (Exception e) {
            System.out.println("Error enviando correo: " + e.getMessage());
        }
    }

    public String login(AuthRequest request) {

        Student student = repository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!student.isEnabled()) {
            throw new RuntimeException("Debes confirmar tu correo primero");
        }

        if (!passwordEncoder.matches(request.getPassword(), student.getPassword())) {
            throw new RuntimeException("Contraseña incorrecta");
        }

        return jwtUtil.generateToken(student.getEmail());
    }
    public void confirmAccount(String token) {
        Student student = repository.findByVerificationToken(token)
                .orElseThrow(() -> new RuntimeException("Token inválido"));

        student.setEnabled(true);
        student.setVerificationToken(null);

        repository.save(student);
    }
}