package com.proyecto.Studentservices.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DashboardResponse {

    private String name;
    private String email;

    private List<CourseProgressDTO> courses;
}
