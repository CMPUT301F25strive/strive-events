package com.example.eventlottery.data;

import com.example.eventlottery.model.EntrantProfile;
import com.example.eventlottery.model.OrganizerProfile;

import java.util.List;

/**
 * This class holds CRUD for entrant and organizer profiles.
 * This provides search/browse for admin.
 */
public interface ProfileRepository {
    // Entrant
    EntrantProfile findEntrantById(String id);

    /**
     * This saves an entrant profile.
     * @param entrant: entrant profile
     */
    void saveEntrant(EntrantProfile entrant);

    /**
     * This deletes an entrant profile by its unique id.
     * @param id: the value of its unique key
     */
    void deleteEntrant(String id);

    // Organizer
    OrganizerProfile findOrganizerById(String id);

    /**
     * This saves an organizer profile.
     * @param organizer: organizer profile
     */
    void saveOrganizer(OrganizerProfile organizer);

    /**
     * This deletes an organizer profile by its unique id.
     * @param id: the value of its unique key
     */
    void deleteOrganizer(String id);

    // TODO: For admin browse and search
    /**
     * This searches the desired entrant profiles.
     * @param criteria:
     * @return a list of entrant profiles
     */
    //List<EntrantProfile> searchEntrants(String criteria);

    /**
     * This searches the desired organizer profiles.
     * @param cirteria:
     * @return a list of organizer profiles
     */
    //List<OrganizerProfile> searchOrganizers(String criteria);
}