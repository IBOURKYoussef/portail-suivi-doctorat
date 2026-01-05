package ma.spring.notificationservice.repository;

import ma.spring.notificationservice.model.EmailTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmailTemplateRepository extends JpaRepository<EmailTemplate, Long> {
    Optional<EmailTemplate> findByTemplateCode(String templateCode);
    Optional<EmailTemplate> findByTemplateCodeAndActiveTrue(String templateCode);
}