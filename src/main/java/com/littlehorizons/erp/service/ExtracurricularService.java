package com.littlehorizons.erp.service;

import com.littlehorizons.erp.dao.extracurricular.ClubDAO;
import com.littlehorizons.erp.dao.extracurricular.ClubRepository;
import com.littlehorizons.erp.dao.extracurricular.EventDAO;
import com.littlehorizons.erp.dao.extracurricular.EventRepository;
import com.littlehorizons.erp.model.extracurricular.Club;
import com.littlehorizons.erp.model.extracurricular.ClubMembership;
import com.littlehorizons.erp.model.extracurricular.EventParticipant;
import com.littlehorizons.erp.model.extracurricular.SchoolEvent;
import com.littlehorizons.erp.util.ValidationUtil;

import java.util.*;
import java.util.stream.Collectors;

public class ExtracurricularService implements IExtracurricularService {

    private final ClubRepository clubDAO;
    private final EventRepository eventDAO;

    public ExtracurricularService() {
        this(new ClubDAO(), new EventDAO());
    }

    public ExtracurricularService(ClubRepository clubDAO, EventRepository eventDAO) {
        this.clubDAO = clubDAO;
        this.eventDAO = eventDAO;
    }

    @Override
    public Integer registerClub(Club club) {
        if (!ValidationUtil.isNotEmpty(club.getClubName())) {
            throw new IllegalArgumentException("El nombre del club es obligatorio.");
        }
        return clubDAO.save(club);
    }

    @Override
    public List<Club> getAllClubs() {
        return clubDAO.findAll();
    }

    @Override
    public Optional<Club> getClubById(int id) {
        return clubDAO.findById(id);
    }

    @Override
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

    @Override
    public boolean withdrawStudentFromClub(int membershipId, String reason) {
        return clubDAO.withdrawStudent(membershipId, reason);
    }

    @Override
    public List<ClubMembership> getClubMembers(int clubId) {
        return clubDAO.findMembershipsByClub(clubId);
    }

    @Override
    public Integer registerEvent(SchoolEvent event) {
        if (!ValidationUtil.isNotEmpty(event.getEventName()) || event.getEventDate() == null) {
            throw new IllegalArgumentException("El nombre del evento y su fecha son obligatorios.");
        }
        return eventDAO.save(event);
    }

    @Override
    public List<SchoolEvent> getAllEvents() {
        return eventDAO.findAll();
    }

    @Override
    public Optional<SchoolEvent> getEventById(int id) {
        return eventDAO.findById(id);
    }

    @Override
    public boolean registerParticipant(int eventId, int studentId, String role, String position, String observations) {
        return eventDAO.registerParticipant(eventId, studentId, role, position, observations);
    }

    @Override
    public List<EventParticipant> getEventParticipants(int eventId) {
        return eventDAO.findParticipantsByEvent(eventId);
    }

    @Override
    public List<Club> getClubsNearCapacity(double occupancyThresholdRatio) {
        return getAllClubs().stream()
                .filter(c -> c.getMaxCapacity() > 0)
                .filter(c -> {
                    int enrolled = c.getEnrolledCount() != null ? c.getEnrolledCount() : clubDAO.getActiveMembersCount(c.getIdClub());
                    return ((double) enrolled / c.getMaxCapacity()) >= occupancyThresholdRatio;
                })
                .sorted(Comparator.comparingDouble((Club c) -> {
                    int enrolled = c.getEnrolledCount() != null ? c.getEnrolledCount() : 0;
                    return (double) enrolled / c.getMaxCapacity();
                }).reversed())
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Long> getMembershipsCountByStatus() {
        return getAllClubs().stream()
                .flatMap(c -> clubDAO.findMembershipsByClub(c.getIdClub()).stream())
                .collect(Collectors.groupingBy(ClubMembership::getStatus, Collectors.counting()));
    }

    @Override
    public List<EventParticipant> getWinningParticipants() {
        return getAllEvents().stream()
                .flatMap(e -> eventDAO.findParticipantsByEvent(e.getIdEvent()).stream())
                .filter(p -> p.getPositionObtained() != null && !p.getPositionObtained().isBlank())
                .filter(p -> !p.getPositionObtained().equalsIgnoreCase("Participante"))
                .sorted(Comparator.comparing(EventParticipant::getPositionObtained))
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Long> getEventsCountByType() {
        return getAllEvents().stream()
                .filter(e -> e.getEventType() != null)
                .collect(Collectors.groupingBy(SchoolEvent::getEventType, Collectors.counting()));
    }

    @Override
    public double getAverageClubCapacity() {
        return getAllClubs().stream()
                .mapToInt(Club::getMaxCapacity)
                .average()
                .orElse(0.0);
    }
}
