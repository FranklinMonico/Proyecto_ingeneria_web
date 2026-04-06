package com.proyecto.Studentservices.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
public class Certificate {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String studentEmail;
    private String courseId;
    private String courseName;
    private LocalDateTime issuedAt;
}
