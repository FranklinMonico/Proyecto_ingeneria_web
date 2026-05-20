package com.proyecto.Studentservices.repository;

import com.proyecto.Studentservices.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, String> {
    Optional<Student> findByEmail(String email);


    Optional<Student> findByVerificationToken(String token);

    Optional<Student> findByResetToken(String token);
}
