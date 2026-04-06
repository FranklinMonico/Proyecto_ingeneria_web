package com.proyecto.Studentservices.dto;

import lombok.Data;
import java.util.List;

@Data
public class CourseModulesResponse {
    private boolean success;
    private String message;
    private List<ModuleDTO> data;

    @Data
    public static class ModuleDTO {
        private Long id;
        private String title;
        private String description;
        private int orderIndex;
    }
}