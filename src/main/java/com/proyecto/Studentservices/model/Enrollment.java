package com.proyecto.Studentservices.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class Enrollment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String enrollmentId; // ID que viene del Grupo A
    private String studentEmail;
    private String courseId;
    private String courseName;   // nuevo
    private int progress;
    private LocalDateTime enrolledAt; // nuevo
    private String learningStudentId; // ID del estudiante en el sistema del Grupo A
}
