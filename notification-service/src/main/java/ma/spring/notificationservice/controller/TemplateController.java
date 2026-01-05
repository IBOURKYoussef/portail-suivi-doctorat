package ma.spring.notificationservice.controller;

import lombok.RequiredArgsConstructor;
import ma.spring.notificationservice.dto.EmailTemplateRequest;
import ma.spring.notificationservice.model.EmailTemplate;
import ma.spring.notificationservice.repository.EmailTemplateRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/templates")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TemplateController {

    private final EmailTemplateRepository templateRepository;

    @GetMapping
    public ResponseEntity<List<EmailTemplate>> getAllTemplates() {
        return ResponseEntity.ok(templateRepository.findAll());
    }

    @PostMapping
    public ResponseEntity<EmailTemplate> createTemplate(@RequestBody EmailTemplateRequest request) {
        EmailTemplate template = EmailTemplate.builder()
                .templateCode(request.getTemplateCode())
                .subject(request.getSubject())
                .body(request.getBody())
                .active(request.getActive() != null ? request.getActive() : true)
                .build();

        return ResponseEntity.ok(templateRepository.save(template));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmailTemplate> updateTemplate(
            @PathVariable Long id,
            @RequestBody EmailTemplateRequest request) {

        Optional<EmailTemplate> existing = templateRepository.findById(id);
        if (existing.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        EmailTemplate template = existing.get();
        template.setSubject(request.getSubject());
        template.setBody(request.getBody());
        if (request.getActive() != null) {
            template.setActive(request.getActive());
        }

        return ResponseEntity.ok(templateRepository.save(template));
    }
}