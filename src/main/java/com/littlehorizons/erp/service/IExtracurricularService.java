package com.littlehorizons.erp.service;

import com.littlehorizons.erp.model.extracurricular.Club;
import com.littlehorizons.erp.model.extracurricular.ClubMembership;
import com.littlehorizons.erp.model.extracurricular.EventParticipant;
import com.littlehorizons.erp.model.extracurricular.SchoolEvent;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface IExtracurricularService {
    Integer registerClub(Club club);
    List<Club> getAllClubs();
    Optional<Club> getClubById(int id);
    boolean enrollStudentInClub(int studentId, int clubId);
    boolean withdrawStudentFromClub(int membershipId, String reason);
    List<ClubMembership> getClubMembers(int clubId);
    Integer registerEvent(SchoolEvent event);
    List<SchoolEvent> getAllEvents();
    Optional<SchoolEvent> getEventById(int id);
    boolean registerParticipant(int eventId, int studentId, String role, String position, String observations);
    List<EventParticipant> getEventParticipants(int eventId);
    List<Club> getClubsNearCapacity(double occupancyThresholdRatio);
    Map<String, Long> getMembershipsCountByStatus();
    List<EventParticipant> getWinningParticipants();
    Map<String, Long> getEventsCountByType();
    double getAverageClubCapacity();
}
