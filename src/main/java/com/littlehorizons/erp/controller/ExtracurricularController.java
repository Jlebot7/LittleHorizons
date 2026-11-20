package com.littlehorizons.erp.controller;

import com.littlehorizons.erp.model.extracurricular.Club;
import com.littlehorizons.erp.model.extracurricular.ClubMembership;
import com.littlehorizons.erp.model.extracurricular.EventParticipant;
import com.littlehorizons.erp.model.extracurricular.SchoolEvent;
import com.littlehorizons.erp.service.IExtracurricularService;
import com.littlehorizons.erp.util.ConsoleInputReader;
import com.littlehorizons.erp.view.ExtracurricularConsoleView;
import com.littlehorizons.erp.view.IExtracurricularView;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class ExtracurricularController {

    private final IExtracurricularService extracurricularService;
    private final IExtracurricularView extracurricularView;

    public ExtracurricularController(IExtracurricularService extracurricularService) {
        this(extracurricularService, new ExtracurricularConsoleView());
    }

    public ExtracurricularController(IExtracurricularService extracurricularService, IExtracurricularView extracurricularView) {
        this.extracurricularService = extracurricularService;
        this.extracurricularView = extracurricularView;
    }

    public void run() {
        boolean back = false;
        while (!back) {
            extracurricularView.showMenu();
            int option = ConsoleInputReader.readInt("Seleccione una opción: ", 0, 8);
            switch (option) {
                case 1 -> listClubs();
                case 2 -> registerClub();
                case 3 -> enrollStudentInClub();
                case 4 -> viewClubMembers();
                case 5 -> listEvents();
                case 6 -> registerEvent();
                case 7 -> registerParticipant();
                case 8 -> viewEventParticipants();
                case 0 -> back = true;
            }
        }
    }

    private void listClubs() {
        List<Club> clubs = extracurricularService.getAllClubs();
        extracurricularView.showClubs(clubs);
        ConsoleInputReader.waitForEnter();
    }

    private void registerClub() {
        extracurricularView.showRegisterClubHeader();
        String name = ConsoleInputReader.readString("Nombre del club: ");
        String desc = ConsoleInputReader.readString("Descripción: ");
        int teacherId = ConsoleInputReader.readInt("ID del profesor supervisor: ", 1, 99999);
        int capacity = ConsoleInputReader.readInt("Cupo máximo de estudiantes: ", 5, 100);
        String schedule = ConsoleInputReader.readString("Días y horario de reunión (ej: Martes 3-5 PM): ");

        Club c = new Club(null, name, desc, teacherId, capacity, schedule, true);
        try {
            Integer id = extracurricularService.registerClub(c);
            if (id != null) {
                extracurricularView.showSuccess("Club registrado exitosamente con ID: " + id);
            } else {
                extracurricularView.showError("Error al registrar el club.");
            }
        } catch (IllegalArgumentException e) {
            extracurricularView.showError("Validación de club fallida: " + e.getMessage());
        } catch (IllegalStateException e) {
            extracurricularView.showError(e.getMessage());
        } catch (Exception e) {
            extracurricularView.showError("Error al registrar el club: " + e.getMessage());
        }
        ConsoleInputReader.waitForEnter();
    }

    private void enrollStudentInClub() {
        extracurricularView.showEnrollClubHeader();
        int studentId = ConsoleInputReader.readInt("ID del estudiante: ", 1, 99999);
        int clubId = ConsoleInputReader.readInt("ID del club: ", 1, 99999);

        try {
            boolean enrolled = extracurricularService.enrollStudentInClub(studentId, clubId);
            if (enrolled) {
                extracurricularView.showSuccess("Estudiante inscrito exitosamente en el club.");
            } else {
                extracurricularView.showError("No se pudo completar la inscripción.");
            }
        } catch (IllegalArgumentException e) {
            extracurricularView.showError("Validación fallida: " + e.getMessage());
        } catch (IllegalStateException e) {
            extracurricularView.showError(e.getMessage());
        } catch (Exception e) {
            extracurricularView.showError("Error al inscribir en club: " + e.getMessage());
        }
        ConsoleInputReader.waitForEnter();
    }

    private void viewClubMembers() {
        int clubId = ConsoleInputReader.readInt("Ingrese el ID del club: ", 1, 99999);
        Optional<Club> clubOpt = extracurricularService.getClubById(clubId);
        if (clubOpt.isEmpty()) {
            extracurricularView.showError("El club no existe.");
            ConsoleInputReader.waitForEnter();
            return;
        }

        List<ClubMembership> members = extracurricularService.getClubMembers(clubId);
        extracurricularView.showClubMembers(clubOpt.get().getClubName(), members);
        ConsoleInputReader.waitForEnter();
    }

    private void listEvents() {
        List<SchoolEvent> events = extracurricularService.getAllEvents();
        extracurricularView.showEvents(events);
        ConsoleInputReader.waitForEnter();
    }

    private void registerEvent() {
        extracurricularView.showRegisterEventHeader();
        String name = ConsoleInputReader.readString("Nombre del evento: ");
        String desc = ConsoleInputReader.readString("Descripción: ");
        extracurricularView.showEventRoles();
        String type = ConsoleInputReader.readString("Tipo de evento: ");
        LocalDate date = ConsoleInputReader.readDate("Fecha del evento");
        String loc = ConsoleInputReader.readString("Lugar / Espacio físico: ");
        boolean comp = ConsoleInputReader.readBoolean("¿Es un evento con competencia y premiación de posiciones?");
        int orgId = ConsoleInputReader.readInt("ID del profesor organizador: ", 1, 99999);

        SchoolEvent ev = new SchoolEvent(null, name, desc, type, date, loc, comp, orgId, true);
        try {
            Integer id = extracurricularService.registerEvent(ev);
            if (id != null) {
                extracurricularView.showSuccess("Evento programado exitosamente con ID: " + id);
            } else {
                extracurricularView.showError("Error al registrar el evento.");
            }
        } catch (IllegalArgumentException e) {
            extracurricularView.showError("Validación de evento fallida: " + e.getMessage());
        } catch (IllegalStateException e) {
            extracurricularView.showError(e.getMessage());
        } catch (Exception e) {
            extracurricularView.showError("Error al registrar el evento: " + e.getMessage());
        }
        ConsoleInputReader.waitForEnter();
    }

    private void registerParticipant() {
        extracurricularView.showRegisterParticipantHeader();
        int eventId = ConsoleInputReader.readInt("ID del evento: ", 1, 99999);
        int studentId = ConsoleInputReader.readInt("ID del estudiante: ", 1, 99999);

        String role = ConsoleInputReader.readString("Rol del estudiante: ");
        String pos = ConsoleInputReader.readOptionalString("Posición obtenida si aplica (1er Lugar, 2do Lugar, 3er Lugar, Participante): ");
        String obs = ConsoleInputReader.readOptionalString("Observaciones o reconocimientos: ");

        try {
            boolean ok = extracurricularService.registerParticipant(eventId, studentId, role, pos.isEmpty() ? "Participante" : pos, obs);
            if (ok) {
                extracurricularView.showSuccess("Participación registrada exitosamente.");
            } else {
                extracurricularView.showError("Error al registrar la participación.");
            }
        } catch (IllegalArgumentException e) {
            extracurricularView.showError("Validación fallida: " + e.getMessage());
        } catch (IllegalStateException e) {
            extracurricularView.showError(e.getMessage());
        } catch (Exception e) {
            extracurricularView.showError("Error al registrar la participación: " + e.getMessage());
        }
        ConsoleInputReader.waitForEnter();
    }

    private void viewEventParticipants() {
        int eventId = ConsoleInputReader.readInt("ID del evento: ", 1, 99999);
        List<EventParticipant> participants = extracurricularService.getEventParticipants(eventId);
        extracurricularView.showEventParticipants(participants);
        ConsoleInputReader.waitForEnter();
    }
}
