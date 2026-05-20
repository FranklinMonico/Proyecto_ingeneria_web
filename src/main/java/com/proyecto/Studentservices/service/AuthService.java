package com.proyecto.Studentservices.service;

import com.proyecto.Studentservices.client.EspoCrmClient;
import com.proyecto.Studentservices.dto.AuthRequest;
import com.proyecto.Studentservices.dto.RegisterRequest;
import com.proyecto.Studentservices.model.Certificate;
import com.proyecto.Studentservices.model.Student;
import com.proyecto.Studentservices.repository.CertificateRepository;
import com.proyecto.Studentservices.repository.StudentRepository;
import com.proyecto.Studentservices.security.JwtUtil;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AuthService {

    private final StudentRepository repository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;
    private final CertificateRepository certificateRepository;
    private final EspoCrmClient espoCrmClient;
    private final CertificatePdfService certificatePdfService;

    public AuthService(StudentRepository repository,
                       BCryptPasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil,
                       EmailService emailService,
                       EspoCrmClient espoCrmClient,
                       CertificateRepository certificateRepository,
                       CertificatePdfService certificatePdfService) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.emailService = emailService;
        this.espoCrmClient = espoCrmClient;
        this.certificateRepository = certificateRepository;
        this.certificatePdfService = certificatePdfService;
    }

    public void register(RegisterRequest request) {

        if (repository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("El correo ya está registrado");
        }

        String token = UUID.randomUUID().toString();

        Student student = Student.builder()
                .name(request.getName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .profilePicture(request.getProfilePicture())
                .verificationToken(token)
                .enabled(false)
                .build();

        repository.save(student);
        try {
            String existingContactId = espoCrmClient.findContactIdByEmail(request.getEmail());
            if (existingContactId == null) {
                espoCrmClient.createContact(
                        request.getName(),
                        request.getLastName(),
                        request.getEmail()
                );
            } else {
                System.out.println("Contacto ya existe en EspoCRM: " + request.getEmail());
            }
        } catch (Exception e) {
            System.out.println("Error EspoCRM al registrar: " + e.getMessage());
        }


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

    public Map<String, String> login(AuthRequest request) {
        Student student = repository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!student.isEnabled()) {
            throw new RuntimeException("Debes confirmar tu correo primero");
        }

        if (!passwordEncoder.matches(request.getPassword(), student.getPassword())) {
            throw new RuntimeException("Contraseña incorrecta");
        }

        // Generar ambos tokens
        String accessToken = jwtUtil.generateToken(student.getEmail());
        String refreshToken = jwtUtil.generateRefreshToken(student.getEmail());

        // Guardar refreshToken en BD
        student.setRefreshToken(refreshToken);
        repository.save(student);

        Map<String, String> tokens = new HashMap<>();
        tokens.put("accessToken", accessToken);
        tokens.put("refreshToken", refreshToken);
        return tokens;
    }

    public String refresh(String refreshToken) {
        // Validar el refreshToken
        if (!jwtUtil.validateToken(refreshToken)) {
            throw new RuntimeException("RefreshToken inválido o expirado");
        }

        String email = jwtUtil.extractEmail(refreshToken);

        Student student = repository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Verificar que el refreshToken coincide con el guardado en BD
        if (!refreshToken.equals(student.getRefreshToken())) {
            throw new RuntimeException("RefreshToken no válido");
        }

        // Generar nuevo accessToken
        return jwtUtil.generateToken(email);
    }
    public void confirmAccount(String token) {
        Student student = repository.findByVerificationToken(token)
                .orElseThrow(() -> new RuntimeException("Token inválido"));

        student.setEnabled(true);
        student.setVerificationToken(null);
        repository.save(student);

        // Enviar certificados pendientes
        sendPendingCertificates(student);
    }

    private void sendPendingCertificates(Student student) {
        List<Certificate> pendientes = certificateRepository
                .findByStudentEmailAndSentFalse(student.getEmail());

        if (pendientes.isEmpty()) return;

        String studentFullName = student.getName()
                + (student.getLastName() != null ? " " + student.getLastName() : "");

        for (Certificate cert : pendientes) {
            try {
                byte[] pdfBytes = certificatePdfService.generateCertificate(
                        studentFullName,
                        cert.getCourseName(),
                        cert.getIssuedAt()
                );

                if (pdfBytes != null) {
                    emailService.sendCertificateEmailWithPdf(
                            student.getEmail(),
                            studentFullName,
                            cert.getCourseName(),
                            pdfBytes
                    );
                } else {
                    emailService.sendCertificateEmail(
                            student.getEmail(),
                            cert.getCourseName()
                    );
                }

                cert.setSent(true);
                certificateRepository.save(cert);
                System.out.println("Certificado pendiente enviado con PDF: " + cert.getCourseName());

            } catch (Exception e) {
                System.out.println("Error enviando certificado pendiente: " + e.getMessage());
            }

            try {
                String contactId = espoCrmClient.findContactIdByEmail(student.getEmail());
                if (contactId != null) {
                    espoCrmClient.updateCompletedCourses(contactId, cert.getCourseName());
                }
            } catch (Exception e) {
                System.out.println("Error actualizando EspoCRM: " + e.getMessage());
            }
        }
    }
    public void forgotPassword(String email) {
        Student student = repository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Email no encontrado"));

        String token = UUID.randomUUID().toString();
        student.setResetToken(token);
        student.setResetTokenExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 30)); // 30 minutos
        repository.save(student);

        try {
            emailService.sendResetEmail(
                    student.getEmail(),
                    student.getName(),
                    token
            );
        } catch (Exception e) {
            System.out.println("Error enviando email de reset: " + e.getMessage());
        }
    }

    public void resetPassword(String token, String newPassword) {
        Student student = repository.findByResetToken(token)
                .orElseThrow(() -> new RuntimeException("Token inválido"));

        // Verificar que el token no haya expirado
        if (student.getResetTokenExpiration().before(new Date())) {
            throw new RuntimeException("El token ha expirado");
        }

        student.setPassword(passwordEncoder.encode(newPassword));
        student.setResetToken(null);
        student.setResetTokenExpiration(null);
        repository.save(student);
    }
}