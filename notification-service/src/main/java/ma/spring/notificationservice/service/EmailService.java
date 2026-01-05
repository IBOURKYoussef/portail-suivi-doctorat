package ma.spring.notificationservice.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Async
    public void sendEmail(String to, String subject, String body) {
        log.info("Tentative envoi email à: {}", to);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true);
            helper.setFrom("portail-doctorat@univ.edu"); // ← Important pour Gmail

            mailSender.send(message);
            log.info("✅ Email envoyé avec succès à: {}", to);

        } catch (MessagingException e) {
            log.error("❌ Erreur MessagingException pour {}: {}", to, e.getMessage());
            throw new RuntimeException("Erreur de format d'email", e);
        } catch (MailException e) {
            log.error("❌ Erreur MailException pour {}: {}", to, e.getMessage());
            // Log plus détaillé pour Gmail
            if (e.getMessage().contains("535")) {
                log.error("❌ ERREUR AUTHENTIFICATION GMAIL - Vérifiez email/mot de passe application");
            } else if (e.getMessage().contains("501")) {
                log.error("❌ ERREUR SYNTAXE EMAIL - Vérifiez le format de l'email");
            }
            throw new RuntimeException("Erreur d'envoi d'email", e);
        }
    }
}


//package ma.spring.notificationservice.service;
//
//import jakarta.mail.MessagingException;
//import jakarta.mail.internet.MimeMessage;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.mail.javamail.JavaMailSender;
//import org.springframework.mail.javamail.MimeMessageHelper;
//import org.springframework.scheduling.annotation.Async;
//import org.springframework.stereotype.Service;
//import org.thymeleaf.TemplateEngine;
//import org.thymeleaf.context.Context;
//
//import java.util.Map;
//
//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class EmailService {
//
//    private final JavaMailSender mailSender;
//    private final TemplateEngine templateEngine;
//
//    @Async
//    public void sendEmail(String to, String subject, String body) {
//        try {
//            MimeMessage message = mailSender.createMimeMessage();
//            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
//
//            helper.setTo(to);
//            helper.setSubject(subject);
//            helper.setText(body, true); // true pour HTML
//
//            mailSender.send(message);
//            log.info("Email envoyé avec succès à: {}", to);
//        } catch (MessagingException e) {
//            log.error("Erreur lors de l'envoi de l'email à {}: {}", to, e.getMessage());
//            throw new RuntimeException("Erreur d'envoi d'email", e);
//        }
//    }
//
//    @Async
//    public void sendTemplateEmail(String to, String subject, String templateName, Map<String, Object> variables) {
//        try {
//            Context context = new Context();
//            context.setVariables(variables);
//
//            String htmlContent = templateEngine.process(templateName, context);
//
//            sendEmail(to, subject, htmlContent);
//        } catch (Exception e) {
//            log.error("Erreur lors de l'envoi de l'email template à {}: {}", to, e.getMessage());
//            throw new RuntimeException("Erreur d'envoi d'email template", e);
//        }
//    }
//}