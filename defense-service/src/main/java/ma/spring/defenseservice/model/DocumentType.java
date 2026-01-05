package ma.spring.defenseservice.model;

public enum DocumentType {
    MANUSCRIPT("Manuscrit de thèse"),
    PLAGIARISM_REPORT("Rapport anti-plagiat"),
    PUBLICATIONS_REPORT("Rapport des publications"),
    TRAINING_CERTIFICATES("Attestations de formation"),
    AUTHORIZATION_REQUEST("Demande d'autorisation de soutenance"),
    RAPPORTEUR_REPORT("Rapport de rapporteur"),
    DEFENSE_PV("Procès-verbal de soutenance"),
    THESIS_PDF("PDF de la thèse"),
    JURY_REPORT("Rapport de jury"),
    DEFENSE_PRESENTATION("Présentation de soutenance"),
    SUPPORTING_DOCUMENT("Document justificatif"),
    CV("Curriculum Vitae"),
    OTHER("Autre document");

    private final String description;

    DocumentType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
