package com.proyecto.Studentservices.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class EnrollmentEvent {
    private Long enrollmentId;
    private String studentEmail;
    private String studentName;
    private Long courseId;
    private String courseTitle;
    private int courseTotalModules;
    private LocalDateTime activatedAt;
}