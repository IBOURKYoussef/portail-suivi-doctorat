// ============= DefenseStatus.java =============
package ma.spring.defenseservice.model;

public enum DefenseStatus {
    SUBMITTED("Demande soumise"),
    PREREQUISITES_CHECK("Vérification des prérequis"),
    PREREQUISITES_VALIDATED("Prérequis validés"),
    PREREQUISITES_REJECTED("Prérequis non satisfaits"),
    JURY_PROPOSED("Jury proposé"),
    JURY_VALIDATED("Jury validé"),
    RAPPORTEURS_ASSIGNED("Rapporteurs désignés"),
    AWAITING_REPORTS("En attente des rapports"),
    REPORTS_RECEIVED("Rapports reçus"),
    AUTHORIZED("Autorisation accordée"),
    SCHEDULED("Soutenance planifiée"),
    COMPLETED("Soutenance effectuée"),
    CANCELLED("Annulée");

    private final String description;

    DefenseStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
