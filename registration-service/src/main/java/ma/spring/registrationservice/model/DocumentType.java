package ma.spring.registrationservice.model;

public enum DocumentType {
    CV("Curriculum Vitae"),
    MOTIVATION_LETTER("Lettre de motivation"),
    DIPLOMA("Diplôme"),
    TRANSCRIPT("Relevé de notes"),
    RESEARCH_PROJECT("Projet de recherche"),
    RECOMMENDATION_LETTER("Lettre de recommandation"),
    ID_CARD("Carte d'identité"),
    BIRTH_CERTIFICATE("Acte de naissance"),
    OTHER("Autre document");

    private final String description;

    DocumentType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
