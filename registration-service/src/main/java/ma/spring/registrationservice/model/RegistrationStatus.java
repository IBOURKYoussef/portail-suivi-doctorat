package ma.spring.registrationservice.model;

public enum RegistrationStatus {
    PENDING("En attente de validation du directeur"),
    APPROVED_BY_DIRECTOR("Approuvé par le directeur de thèse"),
    REJECTED_BY_DIRECTOR("Rejeté par le directeur de thèse"),
    APPROVED_BY_ADMIN("Approuvé par l'administration"),
    REJECTED_BY_ADMIN("Rejeté par l'administration"),
    COMPLETED("Inscription complétée");

    private final String description;

    RegistrationStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

