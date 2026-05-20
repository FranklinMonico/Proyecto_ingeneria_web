package com.proyecto.Studentservices.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CourseProgressDTO {
    private String courseId;
    private String courseName;
    private int progress;
    private int totalModules;
    private boolean completed;
}