package ma.spring.registrationservice.model;

public enum RegistrationType {
    INSCRIPTION("Première inscription"),
    REINSCRIPTION("Réinscription annuelle");

    private final String description;

    RegistrationType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
