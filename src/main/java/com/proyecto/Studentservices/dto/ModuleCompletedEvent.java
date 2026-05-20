package com.proyecto.Studentservices.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ModuleCompletedEvent {
    private Long moduleId;
    private String moduleTitle;
    private Long courseId;
    private Long studentId;
    private String studentEmail;
    private LocalDateTime completedAt;
}
