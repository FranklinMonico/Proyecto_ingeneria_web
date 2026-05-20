package com.proyecto.Studentservices.controller;

import com.proyecto.Studentservices.model.Certificate;
import com.proyecto.Studentservices.model.Student;
import com.proyecto.Studentservices.repository.CertificateRepository;
import com.proyecto.Studentservices.repository.StudentRepository;
import com.proyecto.Studentservices.service.CertificatePdfService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/certificates")
public class CertificateController {

    private final CertificateRepository certificateRepository;
    private final StudentRepository studentRepository;
    private final CertificatePdfService certificatePdfService;

    public CertificateController(CertificateRepository certificateRepository,
                                 StudentRepository studentRepository,
                                 CertificatePdfService certificatePdfService) {
        this.certificateRepository = certificateRepository;
        this.studentRepository = studentRepository;
        this.certificatePdfService = certificatePdfService;
    }

    @GetMapping("/{studentEmail}")
    public ResponseEntity<List<Certificate>> getCertificates(
            @PathVariable String studentEmail) {
        return ResponseEntity.ok(
                certificateRepository.findByStudentEmail(studentEmail)
        );
    }
    // Descargar certificado como PDF
    @GetMapping("/{certificateId}/download")
    public ResponseEntity<byte[]> downloadCertificate(
            @PathVariable String certificateId) {

        Certificate cert = certificateRepository.findById(certificateId)
                .orElseThrow(() -> new RuntimeException("Certificado no encontrado"));

        // Obtener nombre del estudiante
        Student student = studentRepository.findByEmail(cert.getStudentEmail())
                .orElse(null);

        String studentName = student != null
                ? student.getName() + " " + (student.getLastName() != null ? student.getLastName() : "")
                : cert.getStudentEmail();

        byte[] pdf = certificatePdfService.generateCertificate(
                studentName,
                cert.getCourseName(),
                cert.getIssuedAt()
        );

        if (pdf == null) {
            return ResponseEntity.internalServerError().build();
        }

        String filename = "certificado-" + cert.getCourseName()
                .replaceAll("[^a-zA-Z0-9]", "-") + ".pdf";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}