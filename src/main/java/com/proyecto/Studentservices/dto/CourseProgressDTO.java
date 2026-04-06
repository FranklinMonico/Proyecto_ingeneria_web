package com.proyecto.Studentservices.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CourseProgressDTO {

    private String courseId;
    private int progress;
}
