package com.example.eventlottery;

/**
 * This class removes events/profiles/images
 * Review logs and data
 * Moderate violations
 * It delegates deletion tasks to the ProfileRepository.
 */
public class AdminController {
    private ProfileRepository profileRepository;    // Repository responsible for CRUD operations on profiles

    /**
     * This constructs an AdminController with the given ProfileRepository.
     * @param profileRepository: the repository used to manage profiles
     */
    public AdminController(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    /**
     * This method finds an entrant profile identified by the given ID.
     * @param entrantId: the unique identifier of the entrant to be removed
     */
    public void deleteEntrantProfile(String entrantId) {
        profileRepository.deleteEntrant(entrantId);
    }

    /**
     * This method deletes an organizer profile identified by the given ID.
     * @param organizerId the unique identifier of the organizer to be removed
     */
    public void deleteOrganizerProfile(String organizerId) {
        profileRepository.deleteOrganizer(organizerId);
    }

    // TODO: Anything else besides profiles.
}
