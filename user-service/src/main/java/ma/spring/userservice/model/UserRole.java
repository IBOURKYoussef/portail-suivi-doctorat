package ma.spring.userservice.model;

public enum UserRole {
    CANDIDAT("Candidat au doctorat"),
    DOCTORANT("Doctorant inscrit"),
    DIRECTEUR_THESE("Directeur de thèse"),
    ADMINISTRATIF("Personnel administratif"),
    ADMIN("Administrateur système");

    private final String description;

    UserRole(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
    
    /**
     * Retourne le nom du rôle avec le préfixe ROLE_ pour Spring Security
     */
    public String getRoleWithPrefix() {
        return "ROLE_" + this.name();
    }
}