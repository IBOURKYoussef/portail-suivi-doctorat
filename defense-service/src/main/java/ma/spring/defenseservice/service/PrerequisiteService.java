package ma.spring.defenseservice.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PrerequisiteService {

    @Value("${defense.prerequisites.min-publications:2}")
    private int minPublications;

    @Value("${defense.prerequisites.min-conferences:2}")
    private int minConferences;

    @Value("${defense.prerequisites.min-training-hours:200}")
    private int minTrainingHours;

    public boolean checkPrerequisites(int publicationsCount, int conferencesCount, int trainingHours) {
        return publicationsCount >= minPublications &&
                conferencesCount >= minConferences &&
                trainingHours >= minTrainingHours;
    }

    public String getPrerequisitesDescription() {
        return String.format(
                "Minimum requis: %d publications, %d conférences, %d heures de formation",
                minPublications, minConferences, minTrainingHours
        );
    }
}