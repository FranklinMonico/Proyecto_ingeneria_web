package com.proyecto.Studentservices.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class ModuleProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String studentEmail;
    private String courseId;
    private String moduleId;
}
