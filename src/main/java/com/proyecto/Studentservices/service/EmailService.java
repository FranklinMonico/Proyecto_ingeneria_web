package com.proyecto.Studentservices.service;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.nio.charset.StandardCharsets;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final String frontendBaseUrl;
    private final String mailFrom;

    public EmailService(JavaMailSender mailSender,
                        TemplateEngine templateEngine,
                        @Value("${app.frontend-url:http://localhost:3000}") String frontendBaseUrl,
                        @Value("${spring.mail.username}") String mailFrom) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
        this.frontendBaseUrl = trimTrailingSlash(frontendBaseUrl);
        this.mailFrom = mailFrom;
    }

    private static String trimTrailingSlash(String url) {
        if (url == null || url.isEmpty()) return "http://localhost:3000";
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    /** Gmail exige remitente válido; Mailtrap usa usuario que no es email → omitimos setFrom. */
    private void setFromIfEmail(MimeMessageHelper helper) throws Exception {
        if (mailFrom != null && mailFrom.contains("@")) {
            helper.setFrom(mailFrom);
        }
    }

    public void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());

            setFromIfEmail(helper);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);

        } catch (Exception e) {
            throw new RuntimeException("Error enviando correo", e);
        }
    }

    public void sendVerificationEmail(String to, String name, String token) {

        String link = frontendBaseUrl + "/confirm?token=" + token;

        Context context = new Context();
        context.setVariable("name", name);
        context.setVariable("link", link);

        String html = templateEngine.process("verify-email", context);

        sendHtmlEmail(to, "Confirmación de cuenta", html);
    }
    public void sendResetEmail(String to, String name, String token) {

        String link = frontendBaseUrl + "/reset-password?token=" + token;

        Context context = new Context();
        context.setVariable("name", name);
        context.setVariable("link", link);

        String html = templateEngine.process("reset-password-email", context);

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

    public void sendCertificateEmailWithPdf(String to, String studentName, String courseName, byte[] pdfBytes) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
            setFromIfEmail(helper);
            helper.setTo(to);
            helper.setSubject("🎓 Tu certificado de " + courseName);

            // Cuerpo HTML del email
            Context context = new Context();
            context.setVariable("name", studentName);
            context.setVariable("courseName", courseName);
            String html = templateEngine.process("certificate", context);
            helper.setText(html, true);

            // Adjuntar el PDF
            helper.addAttachment(
                    "certificado-" + courseName.replaceAll("[^a-zA-Z0-9]", "-") + ".pdf",
                    new org.springframework.core.io.ByteArrayResource(pdfBytes),
                    "application/pdf"
            );

            mailSender.send(message);
            System.out.println("Certificado con PDF enviado a: " + to);

        } catch (Exception e) {
            throw new RuntimeException("Error enviando certificado con PDF: " + e.getMessage());
        }
    }
}