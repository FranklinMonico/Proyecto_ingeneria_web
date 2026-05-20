package com.proyecto.Studentservices.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DashboardResponse {

    private String name;
    private String email;
    private List<CourseProgressDTO> enrolledCourses;   // cursos inscritos con progreso
    private List<AvailableCourseDTO> availableCourses; // cursos disponibles del catálogo
    private int totalCertificates;
}
