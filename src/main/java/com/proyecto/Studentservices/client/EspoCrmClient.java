package com.proyecto.Studentservices.client;

import com.proyecto.Studentservices.dto.EspoContactRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class EspoCrmClient {

    private final WebClient webClient;

    public EspoCrmClient(
            @Value("${espocrm.url}") String baseUrl,
            @Value("${espocrm.api-key}") String apiKey,
            WebClient.Builder builder) {
        this.webClient = builder
                .baseUrl(baseUrl)
                .defaultHeader("X-Api-Key", apiKey)
                .build();
    }

    // ─── Crear contacto al registrarse ───────────────────────────────────────
    public String createContact(String name, String email) {
        try {
            // Separar nombre y apellido
            String[] parts = name.trim().split(" ", 2);
            String firstName = parts[0];
            String lastName = parts.length > 1 ? parts[1] : "";

            EspoContactRequest request = new EspoContactRequest();
            request.setFirstName(firstName);
            request.setLastName(lastName);
            request.setEmailAddress(email);

            String response = webClient.post()
                    .uri("/api/v1/Contact")
                    .header("Content-Type", "application/json")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            System.out.println("Contacto creado en EspoCRM: " + email);
            return response;

        } catch (Exception e) {
            System.out.println("Error creando contacto en EspoCRM: " + e.getMessage());
            return null;
        }
    }

    // ─── Actualizar cursos completados al terminar un curso ───────────────────

    public void updateCompletedCourses(String contactId, String courseName) {
        try {
            // Primero buscamos el contacto por email para obtener su ID en EspoCRM
            String body = """
                    {
                        "cursosCompletados": "%s"
                    }
                    """.formatted(courseName);

            webClient.put()
                    .uri("/api/v1/Contact/" + contactId)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            System.out.println("EspoCRM actualizado con curso completado: " + courseName);

        } catch (Exception e) {
            System.out.println("Error actualizando EspoCRM: " + e.getMessage());
        }
    }

    // ─── Buscar contacto por email para obtener su ID ─────────────────────────

    public String findContactIdByEmail(String email) {
        try {
            com.fasterxml.jackson.databind.JsonNode response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/v1/Contact")
                            .queryParam("where[0][type]", "equals")
                            .queryParam("where[0][attribute]", "emailAddress")
                            .queryParam("where[0][value]", email)
                            .build())
                    .retrieve()
                    .bodyToMono(com.fasterxml.jackson.databind.JsonNode.class)
                    .block();

            if (response != null && response.has("list") && response.get("list").size() > 0) {
                return response.get("list").get(0).get("id").asText();
            }

        } catch (Exception e) {
            System.out.println("Error buscando contacto en EspoCRM: " + e.getMessage());
        }
        return null;
    }
}