package com.proyecto.Studentservices.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ForumMessageDTO {
    @NotBlank(message = "El courseId es obligatorio")
    private String courseId;

    @NotBlank(message = "El contenido es obligatorio")
    private String content;

    private String studentEmail;
    private String studentName;
}
