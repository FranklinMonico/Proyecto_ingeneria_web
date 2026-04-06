package com.proyecto.Studentservices.service;


import com.proyecto.Studentservices.dto.CourseProgressDTO;
import com.proyecto.Studentservices.dto.DashboardResponse;
import com.proyecto.Studentservices.model.Enrollment;
import com.proyecto.Studentservices.model.Student;
import com.proyecto.Studentservices.repository.EnrollmentRepository;
import com.proyecto.Studentservices.repository.StudentRepository;
import com.proyecto.Studentservices.util.SecurityUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DashboardService {

    private final StudentRepository studentRepository;
    private final EnrollmentRepository enrollmentRepository;

    public DashboardService(EnrollmentRepository enrollmentRepository,StudentRepository studentRepository) {
        this.enrollmentRepository = enrollmentRepository;
        this.studentRepository = studentRepository;
    }

    public DashboardResponse getDashboard() {

        String email = SecurityUtils.getCurrentUserEmail();

        Student student = studentRepository.findByEmail(email)
                .orElseThrow();

        List<Enrollment> enrollments = enrollmentRepository.findByStudentEmail(email);

        List<CourseProgressDTO> courses = enrollments.stream()
                .map(e -> CourseProgressDTO.builder()
                        .courseId(e.getCourseId())
                        .progress(e.getProgress())
                        .build())
                .toList();

        return DashboardResponse.builder()
                .name(student.getName())
                .email(student.getEmail())
                .courses(courses)
                .build();
    }
}
