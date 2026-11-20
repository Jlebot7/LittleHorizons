package com.littlehorizons.erp.view;

import com.littlehorizons.erp.model.extracurricular.Club;
import com.littlehorizons.erp.model.extracurricular.ClubMembership;
import com.littlehorizons.erp.model.extracurricular.EventParticipant;
import com.littlehorizons.erp.model.extracurricular.SchoolEvent;

import java.util.List;

public interface IExtracurricularView {
    void showMenu();
    void showClubs(List<Club> clubs);
    void showRegisterClubHeader();
    void showEnrollClubHeader();
    void showClubMembers(String clubName, List<ClubMembership> members);
    void showEvents(List<SchoolEvent> events);
    void showRegisterEventHeader();
    void showEventRoles();
    void showRegisterParticipantHeader();
    void showEventParticipants(List<EventParticipant> participants);
    void showSuccess(String message);
    void showError(String message);
    void showWarning(String message);
}
