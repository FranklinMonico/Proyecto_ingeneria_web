package com.proyecto.Studentservices.repository;

import com.proyecto.Studentservices.model.ModuleProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ModuleProgressRepository extends JpaRepository<ModuleProgress, String> {
    List<ModuleProgress> findByStudentEmailAndCourseId(String studentEmail, String courseId);
}
