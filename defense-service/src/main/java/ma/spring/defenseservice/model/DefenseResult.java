// ============= DefenseResult.java =============
package ma.spring.defenseservice.model;

public enum DefenseResult {
    ACCEPTED("Admis"),
    ACCEPTED_WITH_CORRECTIONS("Admis avec corrections"),
    REJECTED("Ajourné"),
    POSTPONED("Reporté");

    private final String description;

    DefenseResult(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
