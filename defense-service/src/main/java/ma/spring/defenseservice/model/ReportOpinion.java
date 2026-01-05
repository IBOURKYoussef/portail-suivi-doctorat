// ============= ReportOpinion.java =============
package ma.spring.defenseservice.model;

public enum ReportOpinion {
    FAVORABLE("Avis favorable"),
    FAVORABLE_WITH_RESERVES("Favorable avec réserves"),
    UNFAVORABLE("Avis défavorable");

    private final String description;

    ReportOpinion(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}