package com.proyecto.Studentservices.service;

import com.proyecto.Studentservices.client.EspoCrmClient;
import com.proyecto.Studentservices.dto.UpdateProfileRequest;
import com.proyecto.Studentservices.model.Student;
import com.proyecto.Studentservices.repository.StudentRepository;
import com.proyecto.Studentservices.util.SecurityUtils;
import org.springframework.stereotype.Service;

@Service
public class ProfileService {

    private final StudentRepository studentRepository;
    private final EspoCrmClient espoCrmClient;

    public ProfileService(StudentRepository studentRepository,
                          EspoCrmClient espoCrmClient) {
        this.studentRepository = studentRepository;
        this.espoCrmClient = espoCrmClient;
    }

    public Student getProfile() {
        String email = SecurityUtils.getCurrentUserEmail();
        return studentRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Estudiante no encontrado"));
    }

    public Student updateProfile(UpdateProfileRequest request) {
        String email = SecurityUtils.getCurrentUserEmail();
        Student student = studentRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Estudiante no encontrado"));

        student.setName(request.getName());
        student.setLastName(request.getLastName());

        if (request.getProfilePicture() != null && !request.getProfilePicture().isBlank()) {
            student.setProfilePicture(request.getProfilePicture());
        }

        studentRepository.save(student);

        // Actualizar contacto en EspoCRM
        try {
            String contactId = espoCrmClient.findContactIdByEmail(email);
            if (contactId != null) {
                espoCrmClient.updateContact(contactId, request.getName(), request.getLastName(), email);            } else {
                // Si no existe lo crea
                espoCrmClient.createContact(request.getName(), request.getLastName(), email);
            }
        } catch (Exception e) {
            System.out.println("Error actualizando EspoCRM: " + e.getMessage());
        }

        return student;
    }
}