package com.proyecto.Studentservices.repository;


import com.proyecto.Studentservices.model.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CertificateRepository extends JpaRepository<Certificate, String> {
    List<Certificate> findByStudentEmail(String studentEmail);
    Optional<Certificate> findByStudentEmailAndCourseId(String studentEmail, String courseId);
}
