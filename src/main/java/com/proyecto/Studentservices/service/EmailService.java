package com.proyecto.Studentservices.service;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    public EmailService(JavaMailSender mailSender,
                        TemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }
    public void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);

        } catch (Exception e) {
            throw new RuntimeException("Error enviando correo", e);
        }
    }

    public void sendVerificationEmail(String to, String name, String token) {

        String link = "http://localhost:8081/api/auth/confirm?token=" + token;

        Context context = new Context();
        context.setVariable("name", name);
        context.setVariable("link", link);

        String html = templateEngine.process("verify-email", context);

        sendHtmlEmail(to, "Confirmación de cuenta", html);
    }
    public void sendResetEmail(String to, String name, String token) {

        String link = "http://localhost:8081/api/auth/reset-password?token=" + token;

        Context context = new Context();
        context.setVariable("name", name);
        context.setVariable("link", link);

        String html = templateEngine.process("reset-password", context);

        sendHtmlEmail(to, "Recuperar contraseña", html);
    }
    public void sendWelcomeEmail(String to, String name, String courseName) {
        Context context = new Context();
        context.setVariable("name", name);
        context.setVariable("courseName", courseName);
        String html = templateEngine.process("welcome-enrollment", context);
        sendHtmlEmail(to, "¡Bienvenido al curso " + courseName + "!", html);
    }

    // ─── Certificado al completar curso ──────────────────────────────────────

    public void sendCertificateEmail(String to, String courseName) {
        Context context = new Context();
        context.setVariable("courseName", courseName);
        String html = templateEngine.process("certificate", context);
        sendHtmlEmail(to, "Tu certificado de " + courseName, html);
    }
}