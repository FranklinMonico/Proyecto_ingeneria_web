package com.proyecto.Studentservices.dto;

import lombok.Data;


@Data
public class ModuleCompletedEvent {
    private Long enrollmentId;
    private Long moduleId;
    private String moduleName;
}
