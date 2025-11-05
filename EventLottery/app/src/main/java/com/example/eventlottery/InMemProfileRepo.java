//package com.example.eventlottery;
//
//import java.util.HashMap;
//import java.util.Map;
//
//public class InMemProfileRepo implements ProfileRepository{
//    private Map<String, EntrantProfile> entrants = new HashMap<>();
//    private Map<String, OrganizerProfile> organizers = new HashMap<>();
//
//    @Override
//    public EntrantProfile findEntrantById(String id) {
//        return entrants.get(id);
//    }
//
//    /**
//     * This saves an entrant profile.
//     *
//     * @param entrant : entrant profile
//     */
//    @Override
//    public void saveEntrant(EntrantProfile entrant) {
//        entrants.put(entrant.getDeviceId(), entrant);
//    }
//
//    /**
//     * This deletes an entrant profile by its unique id.
//     *
//     * @param id : the value of its unique key
//     */
//    @Override
//    public void deleteEntrant(String id) {
//        entrants.remove(id);
//    }
//
//    @Override
//    public OrganizerProfile findOrganizerById(String id) {
//        return organizers.get(id);
//    }
//
//    /**
//     * This saves an organizer profile.
//     *
//     * @param organizer : organizer profile
//     */
//    @Override
//    public void saveOrganizer(OrganizerProfile organizer) {
//        organizers.put(organizer.getDeviceId(), organizer);
//    }
//
//    /**
//     * This deletes an organizer profile by its unique id.
//     *
//     * @param id : the value of its unique key
//     */
//    @Override
//    public void deleteOrganizer(String id) {
//        organizers.remove(id);
//    }
//
//    // TODO: Admin search func
//}
