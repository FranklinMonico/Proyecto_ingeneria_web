package com.proyecto.Studentservices.repository;

import com.proyecto.Studentservices.model.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<Enrollment, String> {

    List<Enrollment> findByStudentEmail(String studentEmail);
    Optional<Enrollment> findByEnrollmentId(String enrollmentId);
    Optional<Enrollment> findByStudentEmailAndCourseId(String studentEmail, String courseId);
}
