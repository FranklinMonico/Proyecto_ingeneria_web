package com.proyecto.Studentservices.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AvailableCourseDTO {
    private String courseId;
    private String title;
    private String description;
    private String instructor;
    private String imageUrl;
    private double price;
    private int totalModules;
    private int totalLessons;
    private boolean enrolled; // true si el estudiante ya está inscrito
}