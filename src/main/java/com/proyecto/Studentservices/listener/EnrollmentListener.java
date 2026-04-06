package com.proyecto.Studentservices.listener;

import com.proyecto.Studentservices.client.EspoCrmClient;
import com.proyecto.Studentservices.client.LearningEngineClient;
import com.proyecto.Studentservices.dto.EnrollmentEvent;
import com.proyecto.Studentservices.dto.ModuleCompletedEvent;
import com.proyecto.Studentservices.model.Certificate;
import com.proyecto.Studentservices.model.Enrollment;
import com.proyecto.Studentservices.model.ModuleProgress;
import com.proyecto.Studentservices.model.Student;
import com.proyecto.Studentservices.repository.CertificateRepository;
import com.proyecto.Studentservices.repository.EnrollmentRepository;
import com.proyecto.Studentservices.repository.ModuleProgressRepository;
import com.proyecto.Studentservices.repository.StudentRepository;
import com.proyecto.Studentservices.service.EmailService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class EnrollmentListener {

    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;
    private final ModuleProgressRepository moduleProgressRepository;
    private final CertificateRepository certificateRepository;
    private final EmailService emailService;
    private final LearningEngineClient learningEngineClient;
    private final EspoCrmClient espoCrmClient;

    public EnrollmentListener(EnrollmentRepository enrollmentRepository,
                              StudentRepository studentRepository,
                              ModuleProgressRepository moduleProgressRepository,
                              CertificateRepository certificateRepository,
                              EmailService emailService,
                              LearningEngineClient learningEngineClient,
                              EspoCrmClient espoCrmClient) {
        this.enrollmentRepository = enrollmentRepository;
        this.studentRepository = studentRepository;
        this.moduleProgressRepository = moduleProgressRepository;
        this.certificateRepository = certificateRepository;
        this.emailService = emailService;
        this.learningEngineClient = learningEngineClient;
        this.espoCrmClient = espoCrmClient;
    }

    // ─── EVENTO 1: Inscripción activada ───────────────────────────────────────

    @RabbitListener(queues = "enrollment.activated.queue")
    public void handleEnrollment(EnrollmentEvent event) {

        Student student = studentRepository.findByEmail(event.getStudentEmail())
                .orElse(null);

        if (student == null) {
            System.out.println("Usuario no registrado, evento ignorado: " + event.getStudentEmail());
            return;
        }

        // Evitar duplicados
        boolean exists = enrollmentRepository
                .findByStudentEmail(student.getEmail())
                .stream()
                .anyMatch(e -> e.getCourseId().equals(event.getCourseId().toString()));

        if (exists) {
            System.out.println("Enrollment ya existe para curso: " + event.getCourseId());
            return;
        }

        Enrollment enrollment = new Enrollment();
        enrollment.setEnrollmentId(event.getEnrollmentId().toString());
        enrollment.setLearningStudentId(event.getStudentId().toString()); // guardamos el ID numérico
        enrollment.setStudentEmail(student.getEmail());
        enrollment.setCourseId(event.getCourseId().toString());
        enrollment.setCourseName(event.getCourseName());
        enrollment.setProgress(0);
        enrollment.setEnrolledAt(LocalDateTime.now());
        enrollmentRepository.save(enrollment);

        // Email de bienvenida
        try {
            emailService.sendWelcomeEmail(
                    student.getEmail(),
                    student.getName(),
                    event.getCourseName()
            );
        } catch (Exception e) {
            System.out.println("Error enviando email de bienvenida: " + e.getMessage());
        }

        System.out.println("Enrollment creado: " + event.getCourseName() + " para " + student.getEmail());
    }

    // ─── EVENTO 2: Módulo completado ──────────────────────────────────────────

    @RabbitListener(queues = "module.completed.queue")
    public void handleModuleCompleted(ModuleCompletedEvent event) {

        // 1. Buscar el enrollment por enrollmentId
        Enrollment enrollment = enrollmentRepository
                .findByEnrollmentId(event.getEnrollmentId().toString())
                .orElse(null);

        if (enrollment == null) {
            System.out.println("Enrollment no encontrado para id: " + event.getEnrollmentId());
            return;
        }

        // 2. Evitar registrar el mismo módulo dos veces
        boolean moduleAlreadySaved = moduleProgressRepository
                .findByStudentEmailAndCourseId(
                        enrollment.getStudentEmail(),
                        enrollment.getCourseId()
                )
                .stream()
                .anyMatch(m -> m.getModuleId().equals(event.getModuleId().toString()));

        if (!moduleAlreadySaved) {
            ModuleProgress progress = new ModuleProgress();
            progress.setStudentEmail(enrollment.getStudentEmail());
            progress.setCourseId(enrollment.getCourseId());
            progress.setModuleId(event.getModuleId().toString());
            moduleProgressRepository.save(progress);
            System.out.println("Módulo " + event.getModuleId() + " guardado en module_progress");
        }

        // 3. Calcular porcentaje
        //    - Cuántos módulos completó el estudiante (de tu BD)
        int modulosCompletados = moduleProgressRepository
                .findByStudentEmailAndCourseId(
                        enrollment.getStudentEmail(),
                        enrollment.getCourseId()
                ).size();

        //    - Cuántos módulos tiene el curso en total (preguntando al Grupo A)
        int totalModulos = learningEngineClient.getTotalModules(
                enrollment.getCourseId(),
                enrollment.getLearningStudentId() // studentId — ver nota abajo
        );

        if (totalModulos == 0) {
            System.out.println("No se pudo obtener total de módulos del Grupo A");
            return;
        }

        int porcentaje = (modulosCompletados * 100) / totalModulos;
        System.out.println("Progreso: " + modulosCompletados + "/" + totalModulos
                + " módulos = " + porcentaje + "%");

        // 4. Actualizar progreso en el enrollment
        enrollment.setProgress(porcentaje);
        enrollmentRepository.save(enrollment);

        // 5. Si llegó al 100%, emitir certificado
        if (porcentaje == 100) {

            boolean certExists = certificateRepository
                    .findByStudentEmailAndCourseId(
                            enrollment.getStudentEmail(),
                            enrollment.getCourseId()
                    ).isPresent();

            if (certExists) {
                System.out.println("Certificado ya emitido, ignorando.");
                return;
            }

            Certificate cert = new Certificate();
            cert.setStudentEmail(enrollment.getStudentEmail());
            cert.setCourseId(enrollment.getCourseId());
            cert.setCourseName(enrollment.getCourseName());
            cert.setIssuedAt(LocalDateTime.now());
            certificateRepository.save(cert);

            try {
                emailService.sendCertificateEmail(
                        enrollment.getStudentEmail(),
                        enrollment.getCourseName()
                );
            } catch (Exception e) {
                System.out.println("Error enviando certificado: " + e.getMessage());
            }

            System.out.println("Certificado emitido para: " + enrollment.getStudentEmail());
            try {
                String contactId = espoCrmClient.findContactIdByEmail(
                        enrollment.getStudentEmail()
                );
                if (contactId != null) {
                    espoCrmClient.updateCompletedCourses(
                            contactId,
                            enrollment.getCourseName()
                    );
                }
            } catch (Exception e) {
                System.out.println("Error actualizando EspoCRM: " + e.getMessage());
            }
        }
    }
}
