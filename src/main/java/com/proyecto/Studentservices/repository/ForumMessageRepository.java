package com.proyecto.Studentservices.repository;


import com.proyecto.Studentservices.model.ForumMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ForumMessageRepository extends JpaRepository<ForumMessage, String> {
    Page<ForumMessage> findByCourseIdOrderBySentAtAsc(String courseId, Pageable pageable);
}