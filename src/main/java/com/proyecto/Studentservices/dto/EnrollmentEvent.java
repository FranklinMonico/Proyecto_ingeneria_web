package com.proyecto.Studentservices.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EnrollmentEvent {

    private Long enrollmentId;
    private Long studentId;
    private String studentEmail;
    private Long courseId;
    private String courseName;
    private LocalDateTime activatedAt;
}
