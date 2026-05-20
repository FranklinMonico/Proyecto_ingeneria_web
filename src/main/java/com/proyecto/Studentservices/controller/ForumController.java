package com.proyecto.Studentservices.controller;

import com.proyecto.Studentservices.dto.ApiResponse;
import com.proyecto.Studentservices.dto.ForumMessageDTO;
import com.proyecto.Studentservices.model.ForumMessage;
import com.proyecto.Studentservices.service.ForumService;
import com.proyecto.Studentservices.util.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.*;

@RestController
public class ForumController {

    private final ForumService forumService;

    public ForumController(ForumService forumService) {
        this.forumService = forumService;
    }

    // ─── WebSocket: recibe y hace broadcast al canal del curso ────────────────
    @MessageMapping("/forum/{courseId}")
    @SendTo("/topic/course/{courseId}/forum")
    public ForumMessage sendMessage(
            @DestinationVariable String courseId,
            ForumMessageDTO dto) {
        return forumService.saveMessage(courseId, dto);
    }

    // ─── REST: historial paginado ─────────────────────────────────────────────
    @GetMapping("/api/courses/{id}/forum")
    public ResponseEntity<Page<ForumMessage>> getForumHistory(
            @PathVariable String id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ResponseEntity.ok(forumService.getHistory(id, page, size));
    }

    // ─── REST: publicar mensaje ───────────────────────────────────────────────
    @PostMapping("/api/courses/{id}/forum")
    public ResponseEntity<ForumMessage> postMessage(
            @PathVariable String id,
            @Valid @RequestBody ForumMessageDTO dto) {
        String email = SecurityUtils.getCurrentUserEmail();
        dto.setStudentEmail(email);
        return ResponseEntity.ok(forumService.saveMessage(id, dto));
    }
    @PutMapping("/api/forum/messages/{messageId}")
    public ResponseEntity<ApiResponse<ForumMessage>> editMessage(
            @PathVariable String messageId,
            @RequestBody ForumMessageDTO dto) {
        String email = SecurityUtils.getCurrentUserEmail();
        ForumMessage updated = forumService.editMessage(messageId, dto.getContent(), email);
        return ResponseEntity.ok(new ApiResponse<>(true, "Mensaje editado", updated));
    }

    @DeleteMapping("/api/forum/messages/{messageId}")
    public ResponseEntity<ApiResponse<String>> deleteMessage(
            @PathVariable String messageId) {
        String email = SecurityUtils.getCurrentUserEmail();
        forumService.deleteMessage(messageId, email);
        return ResponseEntity.ok(new ApiResponse<>(true, "Mensaje eliminado", null));
    }



}