package com.proyecto.Studentservices.client;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;

@Component
public class LearningEngineClient {

    private final WebClient webClient;

    public LearningEngineClient(
            @Value("${learning-engine.url}") String baseUrl,
            WebClient.Builder builder) {
        this.webClient = builder
                .baseUrl(baseUrl)
                .build();
    }

    // ─── 1. Total de módulos de un curso ─────────────────────────────────────
    // Se llama al activar inscripción para guardar totalModules en BD
    public int getTotalModulesByCourse(String courseId) {
        try {
            JsonNode response = webClient.get()
                    .uri("/api/courses/{id}", courseId)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            if (response != null && response.has("data")) {
                JsonNode data = response.get("data");
                if (data.has("totalModules")) {
                    return data.get("totalModules").asInt();
                }
            }
        } catch (Exception e) {
            System.out.println("Error obteniendo totalModules: " + e.getMessage());
        }
        return 0;
    }

    // ─── 2. Cursos disponibles del catálogo ───────────────────────────────────
    // Se llama en el dashboard para mostrar cursos disponibles vs inscritos
    public List<JsonNode> getAvailableCourses() {
        List<JsonNode> courses = new ArrayList<>();
        try {
            System.out.println("Llamando al Grupo A para obtener cursos: " +
                    webClient + " → /api/courses");

            JsonNode response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/courses")
                            .queryParam("page", 0)
                            .queryParam("size", 100)
                            .build())
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            System.out.println("Respuesta del Grupo A: " + response);

            if (response != null && response.has("data")) {
                JsonNode data = response.get("data");
                if (data.has("content")) {
                    data.get("content").forEach(courses::add);
                }
            }
        } catch (Exception e) {
            System.out.println("Error obteniendo cursos del Grupo A: " + e.getMessage());
        }
        return courses;
    }

    // ─── 3. Verificar si inscripción está COMPLETED ───────────────────────────
    // Se llama antes de emitir el certificado
    public boolean hasActiveEnrollment(String studentEmail, String courseId) {
        try {
            JsonNode response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/enrollments/verify")
                            .queryParam("studentEmail", studentEmail)
                            .queryParam("courseId", courseId)
                            .build())
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                            clientResponse -> {
                                System.out.println("Grupo A retornó error "
                                        + clientResponse.statusCode()
                                        + " — confiando en cálculo propio");
                                return reactor.core.publisher.Mono.empty();
                            })
                    .bodyToMono(JsonNode.class)
                    .onErrorReturn(null) // cualquier error retorna null
                    .block();

            if (response != null && response.has("data")) {
                return response.get("data").asBoolean();
            }

            // null significa que hubo error o 404 — confiar en cálculo propio
            System.out.println("Sin respuesta válida del Grupo A — confiando en cálculo propio");
            return true;

        } catch (Exception e) {
            System.out.println("Error conectando con Grupo A — confiando en cálculo propio: "
                    + e.getMessage());
            return true; // siempre confiar si falla
        }
    }
}