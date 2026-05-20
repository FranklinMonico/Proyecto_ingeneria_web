package com.proyecto.Studentservices.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.proyecto.Studentservices.client.LearningEngineClient;
import com.proyecto.Studentservices.dto.AvailableCourseDTO;
import com.proyecto.Studentservices.dto.CourseProgressDTO;
import com.proyecto.Studentservices.dto.DashboardResponse;
import com.proyecto.Studentservices.model.Enrollment;
import com.proyecto.Studentservices.model.Student;
import com.proyecto.Studentservices.repository.CertificateRepository;
import com.proyecto.Studentservices.repository.EnrollmentRepository;
import com.proyecto.Studentservices.repository.StudentRepository;
import com.proyecto.Studentservices.util.SecurityUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final StudentRepository studentRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final CertificateRepository certificateRepository;
    private final LearningEngineClient learningEngineClient;

    public DashboardService(StudentRepository studentRepository,
                            EnrollmentRepository enrollmentRepository,
                            CertificateRepository certificateRepository,
                            LearningEngineClient learningEngineClient) {
        this.studentRepository = studentRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.certificateRepository = certificateRepository;
        this.learningEngineClient = learningEngineClient;
    }

    public DashboardResponse getDashboard() {

        // Email del token JWT — no del Grupo A
        String email = SecurityUtils.getCurrentUserEmail();
        Student student = studentRepository.findByEmail(email).orElseThrow();

        // Cursos inscritos de TU BD filtrados por email
        List<Enrollment> enrollments = enrollmentRepository.findByStudentEmail(email);

        List<CourseProgressDTO> enrolledCourses = enrollments.stream()
                .map(e -> CourseProgressDTO.builder()
                        .courseId(e.getCourseId())
                        .courseName(e.getCourseName())
                        .progress(e.getProgress())
                        .totalModules(e.getTotalModules() != null ? e.getTotalModules() : 0)
                        .completed(e.getProgress() == 100)
                        .build())
                .toList();
// IDs de cursos inscritos para marcar cuáles ya tiene
        Set<String> enrolledIds = enrollments.stream()
                .map(Enrollment::getCourseId)
                .collect(Collectors.toSet());

        List<AvailableCourseDTO> availableCourses = learningEngineClient
                .getAvailableCourses()
                .stream()
                .map(node -> AvailableCourseDTO.builder()
                        .courseId(node.path("id").asText())
                        .title(node.path("title").asText())
                        .description(node.path("description").asText())
                        .instructor(node.path("instructor").asText())
                        .imageUrl(node.path("imageUrl").asText())
                        .price(node.path("price").asDouble())
                        .totalModules(node.path("totalModules").asInt())
                        .totalLessons(node.path("totalLessons").asInt())
                        .enrolled(enrolledIds.contains(node.path("id").asText()))
                        .build())
                .toList();

        int totalCertificates = certificateRepository
                .findByStudentEmail(email).size();

        return DashboardResponse.builder()
                .name(student.getName())
                .email(student.getEmail())
                .enrolledCourses(enrolledCourses)
                .availableCourses(availableCourses)
                .totalCertificates(totalCertificates)
                .build();
    }
}