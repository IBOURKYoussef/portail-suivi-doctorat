// ============= MemberStatus.java =============
package ma.spring.defenseservice.model;

public enum MemberStatus {
    INVITED("Invité"),
    ACCEPTED("Accepté"),
    DECLINED("Refusé");

    private final String description;

    MemberStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
