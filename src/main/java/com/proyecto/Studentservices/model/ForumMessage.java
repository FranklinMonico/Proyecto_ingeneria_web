package com.proyecto.Studentservices.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "forum_messages")
public class ForumMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String courseId;
    private String studentEmail;
    private String studentName;
    private String content;
    private LocalDateTime sentAt;
}
