package com.proyecto.Studentservices.dto;

import lombok.Data;

@Data
public class ForumMessageDTO {
    private String courseId;
    private String studentEmail;
    private String studentName;
    private String content;
}
