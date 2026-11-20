package com.littlehorizons.erp.dao.extracurricular;

import com.littlehorizons.erp.model.extracurricular.EventParticipant;
import com.littlehorizons.erp.model.extracurricular.SchoolEvent;

import java.util.List;
import java.util.Optional;

public interface EventRepository {
    Integer save(SchoolEvent event);
    List<SchoolEvent> findAll();
    Optional<SchoolEvent> findById(int id);
    boolean registerParticipant(int eventId, int studentId, String role, String position, String observations);
    List<EventParticipant> findParticipantsByEvent(int eventId);
}
