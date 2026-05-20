package com.proyecto.Studentservices.listener;

import com.proyecto.Studentservices.client.EspoCrmClient;
import com.proyecto.Studentservices.client.LearningEngineClient;
import com.proyecto.Studentservices.dto.EnrollmentEvent;
import com.proyecto.Studentservices.dto.ModuleCompletedEvent;
import com.proyecto.Studentservices.model.Certificate;
import com.proyecto.Studentservices.model.Enrollment;
import com.proyecto.Studentservices.model.ModuleProgress;
import com.proyecto.Studentservices.repository.CertificateRepository;
import com.proyecto.Studentservices.repository.EnrollmentRepository;
import com.proyecto.Studentservices.repository.ModuleProgressRepository;
import com.proyecto.Studentservices.repository.StudentRepository;
import com.proyecto.Studentservices.service.CertificatePdfService;
import com.proyecto.Studentservices.service.EmailService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class EnrollmentListener {

    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;
    private final ModuleProgressRepository moduleProgressRepository;
    private final CertificateRepository certificateRepository;
    private final EmailService emailService;
    private final LearningEngineClient learningEngineClient;
    private final EspoCrmClient espoCrmClient;
    private final CertificatePdfService certificatePdfService;

    public EnrollmentListener(EnrollmentRepository enrollmentRepository,
                              StudentRepository studentRepository,
                              ModuleProgressRepository moduleProgressRepository,
                              CertificateRepository certificateRepository,
                              EmailService emailService,
                              LearningEngineClient learningEngineClient,
                              EspoCrmClient espoCrmClient,
                              CertificatePdfService certificatePdfService) {
        this.enrollmentRepository = enrollmentRepository;
        this.studentRepository = studentRepository;
        this.moduleProgressRepository = moduleProgressRepository;
        this.certificateRepository = certificateRepository;
        this.emailService = emailService;
        this.learningEngineClient = learningEngineClient;
        this.espoCrmClient = espoCrmClient;
        this.certificatePdfService = certificatePdfService;
    }

    // ─── EVENTO 1: Inscripción activada ───────────────────────────────────────

    @RabbitListener(queues = "enrollment.activated.queue")
    public void handleEnrollment(EnrollmentEvent event) {

        // 1. Evitar duplicados — guardamos aunque el estudiante no exista aún
        boolean exists = enrollmentRepository
                .findByStudentEmailAndCourseId(
                        event.getStudentEmail(),
                        event.getCourseId().toString()
                ).isPresent();

        if (exists) {
            System.out.println("Enrollment ya existe para curso: " + event.getCourseId());
            return;
        }

        // 2. Obtener total de módulos del curso desde el Grupo A
        //int totalModulos = learningEngineClient.getTotalModulesByCourse(event.getCourseId().toString());
        int totalModulos = event.getCourseTotalModules();
        if (totalModulos == 0) {
            System.out.println("Advertencia: no se pudo obtener totalModules para curso "
                    + event.getCourseId());
        }

        // 3. Guardar enrollment siempre — aunque el estudiante no esté registrado aún
        Enrollment enrollment = new Enrollment();
        enrollment.setEnrollmentId(event.getEnrollmentId().toString());

        enrollment.setStudentEmail(event.getStudentEmail());
        enrollment.setCourseId(event.getCourseId().toString());
        enrollment.setCourseName(event.getCourseTitle());
        enrollment.setTotalModules(totalModulos);
        enrollment.setProgress(0);
        enrollment.setEnrolledAt(LocalDateTime.now());
        enrollmentRepository.save(enrollment);

        // 4. Email de bienvenida solo si el estudiante ya existe en nuestra BD
        studentRepository.findByEmail(event.getStudentEmail()).ifPresent(student -> {
            try {
                emailService.sendWelcomeEmail(
                        student.getEmail(),
                        student.getName(),
                        event.getCourseTitle()
                );
            } catch (Exception e) {
                System.out.println("Error enviando email de bienvenida: " + e.getMessage());
            }
        });

        System.out.println("Enrollment guardado: " + event.getCourseTitle()
                + " para " + event.getStudentEmail()
                + " | Total módulos: " + totalModulos);
    }

    // ─── EVENTO 2: Módulo completado ──────────────────────────────────────────

    @RabbitListener(queues = "module.completed.queue")
    public void handleModuleCompleted(ModuleCompletedEvent event) {

        // 1. Buscar enrollment por studentEmail y courseId
        Enrollment enrollment = enrollmentRepository
                .findByStudentEmailAndCourseId(
                        event.getStudentEmail(),
                        event.getCourseId().toString()
                ).orElse(null);

        if (enrollment == null) {
            System.out.println("Enrollment no encontrado para: "
                    + event.getStudentEmail() + " curso: " + event.getCourseId());
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

        // 3. Calcular porcentaje usando totalModules guardado en enrollment
        int modulosCompletados = moduleProgressRepository
                .findByStudentEmailAndCourseId(
                        enrollment.getStudentEmail(),
                        enrollment.getCourseId()
                ).size();

        int totalModulos = enrollment.getTotalModules();

        if (totalModulos == 0) {
            System.out.println("Total de módulos es 0, no se puede calcular porcentaje");
            return;
        }

        int porcentaje = (modulosCompletados * 100) / totalModulos;
        System.out.println("Progreso: " + modulosCompletados + "/" + totalModulos
                + " módulos = " + porcentaje + "%");

        // 4. Actualizar progreso en el enrollment
        enrollment.setProgress(porcentaje);
        enrollmentRepository.save(enrollment);

        // 5. Si llegó al 100% emitir certificado
        if (porcentaje == 100) {

            // Verificar con Grupo A que inscripción esté activa
            boolean isActive = learningEngineClient.hasActiveEnrollment(
                    enrollment.getStudentEmail(),
                    enrollment.getCourseId()
            );

            if (!isActive) {
                System.out.println("Inscripción no activa en Grupo A, no se emite certificado");
                return;
            }

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
            cert.setSent(false);
            certificateRepository.save(cert);

            // Enviar email con PDF adjunto
            studentRepository.findByEmail(enrollment.getStudentEmail()).ifPresent(student -> {
                try {
                    // Generar el PDF
                    String studentFullName = student.getName()
                            + (student.getLastName() != null ? " " + student.getLastName() : "");

                    byte[] pdfBytes = certificatePdfService.generateCertificate(
                            studentFullName,
                            enrollment.getCourseName(),
                            cert.getIssuedAt()
                    );

                    // Mandar email con PDF adjunto
                    if (pdfBytes != null) {
                        emailService.sendCertificateEmailWithPdf(
                                student.getEmail(),
                                studentFullName,
                                enrollment.getCourseName(),
                                pdfBytes
                        );
                    } else {
                        // Si falla el PDF mandar email normal
                        emailService.sendCertificateEmail(
                                student.getEmail(),
                                enrollment.getCourseName()
                        );
                    }
                } catch (Exception e) {
                    System.out.println("Error enviando certificado: " + e.getMessage());
                }

                // Actualizar EspoCRM
                try {
                    String contactId = espoCrmClient.findContactIdByEmail(student.getEmail());
                    if (contactId != null) {
                        espoCrmClient.updateCompletedCourses(contactId, enrollment.getCourseName());
                    }
                } catch (Exception e) {
                    System.out.println("Error actualizando EspoCRM: " + e.getMessage());
                }
            });

            if (!cert.isSent()) {
                System.out.println("Certificado guardado pendiente para: "
                        + enrollment.getStudentEmail()
                        + " — se enviará cuando se registre");
            }
        }
    }
}