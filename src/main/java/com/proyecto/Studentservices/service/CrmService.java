package com.proyecto.Studentservices.service;

import com.proyecto.Studentservices.client.EspoCrmClient;
import com.proyecto.Studentservices.model.Student;
import com.proyecto.Studentservices.repository.CertificateRepository;
import com.proyecto.Studentservices.repository.StudentRepository;
import org.springframework.stereotype.Service;

@Service
public class CrmService {

    private final StudentRepository studentRepository;
    private final CertificateRepository certificateRepository;
    private final EspoCrmClient espoCrmClient;

    public CrmService(StudentRepository studentRepository,
                      CertificateRepository certificateRepository,
                      EspoCrmClient espoCrmClient) {
        this.studentRepository = studentRepository;
        this.certificateRepository = certificateRepository;
        this.espoCrmClient = espoCrmClient;
    }

    public void syncStudent(String email) {

        Student student = studentRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Estudiante no encontrado"));

        // Buscar o crear contacto en EspoCRM
        String contactId = espoCrmClient.findContactIdByEmail(email);

        if (contactId == null) {
            // No existe — crear contacto
            espoCrmClient.createContact(student.getName(),student.getLastName(), student.getEmail());
            System.out.println("Contacto creado en EspoCRM: " + email);
        } else {
            System.out.println("Contacto ya existe en EspoCRM: " + email);
        }

        // Actualizar cursos completados
        certificateRepository.findByStudentEmail(email).forEach(cert -> {
            try {
                String id = espoCrmClient.findContactIdByEmail(email);
                if (id != null) {
                    espoCrmClient.updateCompletedCourses(id, cert.getCourseName());
                }
            } catch (Exception e) {
                System.out.println("Error actualizando curso en EspoCRM: " + e.getMessage());
            }
        });
    }
}