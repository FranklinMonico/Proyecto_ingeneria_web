package com.proyecto.Studentservices.service;

import com.proyecto.Studentservices.dto.CourseProgressDTO;
import com.proyecto.Studentservices.model.Enrollment;
import com.proyecto.Studentservices.repository.EnrollmentRepository;
import com.proyecto.Studentservices.repository.ModuleProgressRepository;
import org.springframework.stereotype.Service;

@Service
public class ProgressService {

    private final EnrollmentRepository enrollmentRepository;
    private final ModuleProgressRepository moduleProgressRepository;

    public ProgressService(EnrollmentRepository enrollmentRepository,
                           ModuleProgressRepository moduleProgressRepository) {
        this.enrollmentRepository = enrollmentRepository;
        this.moduleProgressRepository = moduleProgressRepository;
    }

    public CourseProgressDTO getProgress(String email, String courseId) {

        Enrollment enrollment = enrollmentRepository
                .findByStudentEmailAndCourseId(email, courseId)
                .orElseThrow(() -> new RuntimeException("No estás inscrito en este curso"));

        int modulosCompletados = moduleProgressRepository
                .findByStudentEmailAndCourseId(email, courseId).size();

        return CourseProgressDTO.builder()
                .courseId(enrollment.getCourseId())
                .courseName(enrollment.getCourseName())
                .progress(enrollment.getProgress())
                .totalModules(enrollment.getTotalModules() != null ? enrollment.getTotalModules() : 0)
                .completed(enrollment.getProgress() == 100)
                .build();
    }
}