package com.proyecto.Studentservices.controller;

import com.proyecto.Studentservices.dto.ForumMessageDTO;
import com.proyecto.Studentservices.model.ForumMessage;
import com.proyecto.Studentservices.service.ForumService;
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

    // ─── WebSocket ────────────────────────────────────────────────────────────

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
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(forumService.getHistory(id, page, size));
    }

    // ─── REST: publicar mensaje ───────────────────────────────────────────────

    @PostMapping("/api/courses/{id}/forum")
    public ResponseEntity<ForumMessage> postMessage(
            @PathVariable String id,
            @RequestBody ForumMessageDTO dto) {
        return ResponseEntity.ok(forumService.saveMessage(id, dto));
    }
}
