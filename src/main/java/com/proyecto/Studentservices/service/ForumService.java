package com.proyecto.Studentservices.service;

import com.proyecto.Studentservices.dto.ForumMessageDTO;
import com.proyecto.Studentservices.model.ForumMessage;
import com.proyecto.Studentservices.repository.ForumMessageRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ForumService {

    private final ForumMessageRepository forumMessageRepository;

    public ForumService(ForumMessageRepository forumMessageRepository) {
        this.forumMessageRepository = forumMessageRepository;
    }

    public ForumMessage saveMessage(String courseId, ForumMessageDTO dto) {
        ForumMessage message = new ForumMessage();
        message.setCourseId(courseId);
        message.setStudentEmail(dto.getStudentEmail());
        message.setStudentName(dto.getStudentName());
        message.setContent(dto.getContent());
        message.setSentAt(LocalDateTime.now());
        return forumMessageRepository.save(message);
    }

    public Page<ForumMessage> getHistory(String courseId, int page, int size) {
        return forumMessageRepository
                .findByCourseIdOrderBySentAtAsc(courseId, PageRequest.of(page, size));
    }

    public ForumMessage editMessage(String messageId, String content, String currentUserEmail) {
        ForumMessage message = forumMessageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Mensaje no encontrado"));

        // Solo el autor puede editar
        if (!message.getStudentEmail().equals(currentUserEmail)) {
            throw new RuntimeException("No puedes editar mensajes de otros usuarios");
        }

        message.setContent(content);
        return forumMessageRepository.save(message);
    }

    public void deleteMessage(String messageId, String currentUserEmail) {
        ForumMessage message = forumMessageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Mensaje no encontrado"));

        // Solo el autor puede eliminar
        if (!message.getStudentEmail().equals(currentUserEmail)) {
            throw new RuntimeException("No puedes eliminar mensajes de otros usuarios");
        }

        forumMessageRepository.deleteById(messageId);
    }
}
