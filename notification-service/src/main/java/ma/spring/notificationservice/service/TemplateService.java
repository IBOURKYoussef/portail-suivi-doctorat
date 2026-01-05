package ma.spring.notificationservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.spring.notificationservice.model.EmailTemplate;
import ma.spring.notificationservice.repository.EmailTemplateRepository;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class TemplateService {

    private final EmailTemplateRepository emailTemplateRepository;
    private final TemplateEngine templateEngine; // Injection du TemplateEngine par défaut

    public String generateEmailContent(String templateCode, Object data) {
        try {
            EmailTemplate template = emailTemplateRepository.findByTemplateCodeAndActiveTrue(templateCode)
                    .orElseGet(() -> getDefaultTemplate(templateCode));

            Context context = new Context();
            if (data instanceof Map) {
                context.setVariables((Map<String, Object>) data);
            }

            // Utilisez le template comme une chaîne directement
            return processTemplateString(template.getBody(), context);
        } catch (Exception e) {
            log.error("Erreur lors de la génération du contenu email: {}", e.getMessage());
            return templateCode + " - Notification importante";
        }
    }

    private String processTemplateString(String templateContent, Context context) {
        // Création d'un template à partir de la chaîne
        return templateEngine.process(templateContent, context);
    }

    private EmailTemplate getDefaultTemplate(String templateCode) {
        return EmailTemplate.builder()
                .templateCode(templateCode)
                .subject("Notification - Portail Doctorat")
                .body("""
                    <!DOCTYPE html>
                    <html>
                    <head>
                        <meta charset="UTF-8">
                        <style>
                            body { font-family: Arial, sans-serif; color: #333; }
                            .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                            .header { background-color: #4CAF50; color: white; padding: 10px; text-align: center; }
                            .content { padding: 20px; background-color: #f9f9f9; }
                            .footer { text-align: center; padding: 10px; font-size: 12px; color: #666; }
                        </style>
                    </head>
                    <body>
                        <div class="container">
                            <div class="header">
                                <h1>Portail Doctorat</h1>
                            </div>
                            <div class="content">
                                <p>Bonjour,</p>
                                <p th:text="${message}">Message par défaut</p>
                                <p>Cordialement,<br>Équipe Portail Doctorat</p>
                            </div>
                            <div class="footer">
                                <p>Cet email est envoyé automatiquement, merci de ne pas y répondre.</p>
                            </div>
                        </div>
                    </body>
                    </html>
                    """)
                .active(true)
                .build();
    }
}