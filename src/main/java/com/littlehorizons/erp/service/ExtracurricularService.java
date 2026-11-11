package com.littlehorizons.erp.service;

import com.littlehorizons.erp.dao.extracurricular.ClubDAO;
import com.littlehorizons.erp.dao.extracurricular.EventDAO;
import com.littlehorizons.erp.model.extracurricular.Club;
import com.littlehorizons.erp.model.extracurricular.ClubMembership;
import com.littlehorizons.erp.model.extracurricular.EventParticipant;
import com.littlehorizons.erp.model.extracurricular.SchoolEvent;
import com.littlehorizons.erp.util.ValidationUtil;

import java.util.List;
import java.util.Optional;

/**
 * Service encapsulating business rules for Extracurricular Clubs and Events.
 */
public class ExtracurricularService {

    private final ClubDAO clubDAO;
    private final EventDAO eventDAO;

    public ExtracurricularService() {
        this.clubDAO = new ClubDAO();
        this.eventDAO = new EventDAO();
    }

    // --- Club Operations ---

    public Integer registerClub(Club club) {
        if (!ValidationUtil.isNotEmpty(club.getClubName())) {
            throw new IllegalArgumentException("El nombre del club es obligatorio.");
        }
        return clubDAO.save(club);
    }

    public List<Club> getAllClubs() {
        return clubDAO.findAll();
    }

    public Optional<Club> getClubById(int id) {
        return clubDAO.findById(id);
    }

    /**
     * Enrolls a student in a club after verifying maximum capacity constraint.
     *
     * @param studentId Student ID
     * @param clubId Club ID
     * @return true if successfully enrolled, false if capacity is exceeded or error
     */
    public boolean enrollStudentInClub(int studentId, int clubId) {
        Optional<Club> clubOpt = clubDAO.findById(clubId);
        if (clubOpt.isEmpty()) {
            throw new IllegalArgumentException("El club seleccionado no existe.");
        }

        Club club = clubOpt.get();
        int currentMembers = clubDAO.getActiveMembersCount(clubId);
        if (currentMembers >= club.getMaxCapacity()) {
            throw new IllegalStateException(String.format("El club '%s' ha alcanzado su cupo máximo de %d estudiantes.",
                    club.getClubName(), club.getMaxCapacity()));
        }

        return clubDAO.enrollStudent(studentId, clubId);
    }

    public boolean withdrawStudentFromClub(int membershipId, String reason) {
        return clubDAO.withdrawStudent(membershipId, reason);
    }

    public List<ClubMembership> getClubMembers(int clubId) {
        return clubDAO.findMembershipsByClub(clubId);
    }

    // --- Event Operations ---

    public Integer registerEvent(SchoolEvent event) {
        if (!ValidationUtil.isNotEmpty(event.getEventName()) || event.getEventDate() == null) {
            throw new IllegalArgumentException("El nombre del evento y su fecha son obligatorios.");
        }
        return eventDAO.save(event);
    }

    public List<SchoolEvent> getAllEvents() {
        return eventDAO.findAll();
    }

    public Optional<SchoolEvent> getEventById(int id) {
        return eventDAO.findById(id);
    }

    public boolean registerParticipant(int eventId, int studentId, String role, String position, String observations) {
        return eventDAO.registerParticipant(eventId, studentId, role, position, observations);
    }

    public List<EventParticipant> getEventParticipants(int eventId) {
        return eventDAO.findParticipantsByEvent(eventId);
    }
}
