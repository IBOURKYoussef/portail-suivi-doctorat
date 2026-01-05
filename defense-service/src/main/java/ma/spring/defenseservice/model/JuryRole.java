// ============= JuryRole.java =============
package ma.spring.defenseservice.model;

public enum JuryRole {
    PRESIDENT("Président du jury"),
    EXAMINER("Examinateur"),
    INVITED("Invité");

    private final String description;

    JuryRole(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
