package com.proyecto.Studentservices.client;

import com.proyecto.Studentservices.dto.CourseModulesResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class LearningEngineClient {

    private final WebClient webClient;

    public LearningEngineClient(WebClient.Builder builder) {
        this.webClient = builder
                .baseUrl("http://localhost:8081") // URL del Grupo A
                .build();
    }

    public int getTotalModules(String courseId, String studentId) {
        try {
            CourseModulesResponse response = webClient.get()
                    .uri("/api/courses/{id}/modules?studentId={studentId}", courseId, studentId)
                    .retrieve()
                    .bodyToMono(CourseModulesResponse.class)
                    .block();

            if (response != null && response.getData() != null) {
                return response.getData().size();
            }
        } catch (Exception e) {
            System.out.println("Error consultando módulos al Grupo A: " + e.getMessage());
        }
        return 0;
    }
}