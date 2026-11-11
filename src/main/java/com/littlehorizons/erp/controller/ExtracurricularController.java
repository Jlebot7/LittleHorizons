package com.littlehorizons.erp.controller;

import com.littlehorizons.erp.model.extracurricular.Club;
import com.littlehorizons.erp.model.extracurricular.ClubMembership;
import com.littlehorizons.erp.model.extracurricular.EventParticipant;
import com.littlehorizons.erp.model.extracurricular.SchoolEvent;
import com.littlehorizons.erp.service.ExtracurricularService;
import com.littlehorizons.erp.util.ConsoleInputReader;
import com.littlehorizons.erp.view.ConsoleHelper;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class ExtracurricularController {

    private final ExtracurricularService extracurricularService;

    public ExtracurricularController(ExtracurricularService extracurricularService) {
        this.extracurricularService = extracurricularService;
    }

    public void run() {
        boolean back = false;
        while (!back) {
            ConsoleHelper.printHeader("ACTIVIDADES EXTRACURRICULARES", "Colegio Little Horizons");
            System.out.println("1. Catálogo general de clubes activos");
            System.out.println("2. Registrar nuevo club extracurricular");
            System.out.println("3. Inscribir estudiante en un club (Validación de cupo)");
            System.out.println("4. Consultar miembros inscritos en un club");
            System.out.println("5. Calendario general de eventos escolares");
            System.out.println("6. Registrar nuevo evento escolar");
            System.out.println("7. Registrar participante en evento (con posición si aplica)");
            System.out.println("8. Consultar participantes y posiciones de un evento");
            System.out.println("0. Regresar al Menú Principal");
            System.out.println();

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
        ConsoleHelper.printSubHeader("CATÁLOGO DE CLUBES EXTRACURRICULARES");
        List<Club> clubs = extracurricularService.getAllClubs();
        if (clubs.isEmpty()) {
            ConsoleHelper.printWarning("No hay clubes registrados.");
        } else {
            System.out.printf("%-4s | %-38s | %-6s | %-10s | %-25s%n", "ID", "NOMBRE DEL CLUB", "CUPO", "OCUPACIÓN", "HORARIO");
            System.out.println("─".repeat(95));
            for (Club c : clubs) {
                System.out.printf("%-4d | %-38s | %-6d | %2d / %-5d | %-25s%n",
                        c.getIdClub(), c.getClubName(), c.getMaxCapacity(),
                        c.getEnrolledCount() != null ? c.getEnrolledCount() : 0,
                        c.getMaxCapacity(),
                        c.getMeetingSchedule());
            }
            System.out.println("─".repeat(95));
        }
        ConsoleInputReader.waitForEnter();
    }

    private void registerClub() {
        ConsoleHelper.printSubHeader("REGISTRO DE NUEVO CLUB");
        String name = ConsoleInputReader.readString("Nombre del club: ");
        String desc = ConsoleInputReader.readString("Descripción: ");
        int teacherId = ConsoleInputReader.readInt("ID del profesor supervisor: ", 1, 99999);
        int capacity = ConsoleInputReader.readInt("Cupo máximo de estudiantes: ", 5, 100);
        String schedule = ConsoleInputReader.readString("Días y horario de reunión (ej: Martes 3-5 PM): ");

        Club c = new Club(null, name, desc, teacherId, capacity, schedule, true);
        Integer id = extracurricularService.registerClub(c);
        if (id != null) {
            ConsoleHelper.printSuccess("Club registrado exitosamente con ID: " + id);
        } else {
            ConsoleHelper.printError("Error al registrar el club.");
        }
        ConsoleInputReader.waitForEnter();
    }

    private void enrollStudentInClub() {
        ConsoleHelper.printSubHeader("INSCRIPCIÓN DE ESTUDIANTE EN CLUB");
        int studentId = ConsoleInputReader.readInt("ID del estudiante: ", 1, 99999);
        int clubId = ConsoleInputReader.readInt("ID del club: ", 1, 99999);

        try {
            boolean enrolled = extracurricularService.enrollStudentInClub(studentId, clubId);
            if (enrolled) {
                ConsoleHelper.printSuccess("Estudiante inscrito exitosamente en el club.");
            } else {
                ConsoleHelper.printError("No se pudo completar la inscripción.");
            }
        } catch (Exception e) {
            ConsoleHelper.printError(e.getMessage());
        }
        ConsoleInputReader.waitForEnter();
    }

    private void viewClubMembers() {
        ConsoleHelper.printSubHeader("MIEMBROS DEL CLUB");
        int clubId = ConsoleInputReader.readInt("Ingrese el ID del club: ", 1, 99999);
        Optional<Club> clubOpt = extracurricularService.getClubById(clubId);
        if (clubOpt.isEmpty()) {
            ConsoleHelper.printError("El club no existe.");
            ConsoleInputReader.waitForEnter();
            return;
        }

        System.out.println("Integrantes del " + clubOpt.get().getClubName() + ":");
        List<ClubMembership> members = extracurricularService.getClubMembers(clubId);
        if (members.isEmpty()) {
            ConsoleHelper.printWarning("No hay estudiantes inscritos en este club.");
        } else {
            System.out.printf("%-5s | %-35s | %-12s | %-10s%n", "ID MEM", "ESTUDIANTE", "FECHA INGRESO", "ESTADO");
            System.out.println("─".repeat(70));
            for (ClubMembership m : members) {
                System.out.printf("%-6d | %-35s | %-13s | %-10s%n",
                        m.getIdMembership(), m.getStudentName(), m.getJoinDate(), m.getStatus());
            }
            System.out.println("─".repeat(70));
        }
        ConsoleInputReader.waitForEnter();
    }

    private void listEvents() {
        ConsoleHelper.printSubHeader("CALENDARIO GENERAL DE EVENTOS");
        List<SchoolEvent> events = extracurricularService.getAllEvents();
        if (events.isEmpty()) {
            ConsoleHelper.printWarning("No hay eventos programados.");
        } else {
            System.out.printf("%-4s | %-40s | %-12s | %-12s | %-20s | %-10s%n", "ID", "NOMBRE DEL EVENTO", "TIPO", "FECHA", "LUGAR", "COMPETENCIA");
            System.out.println("─".repeat(110));
            for (SchoolEvent ev : events) {
                System.out.printf("%-4d | %-40s | %-12s | %-12s | %-20s | %-10s%n",
                        ev.getIdEvent(), ev.getEventName(), ev.getEventType(), ev.getEventDate(),
                        ev.getLocation(), ev.getHasCompetition() ? "Sí (Puestos)" : "No");
            }
            System.out.println("─".repeat(110));
        }
        ConsoleInputReader.waitForEnter();
    }

    private void registerEvent() {
        ConsoleHelper.printSubHeader("REGISTRO DE EVENTO ESCOLAR");
        String name = ConsoleInputReader.readString("Nombre del evento: ");
        String desc = ConsoleInputReader.readString("Descripción: ");
        System.out.println("Tipos de evento: Académico, Cultural, Deportivo, Social, Científico");
        String type = ConsoleInputReader.readString("Tipo de evento: ");
        LocalDate date = ConsoleInputReader.readDate("Fecha del evento");
        String loc = ConsoleInputReader.readString("Lugar / Espacio físico: ");
        boolean comp = ConsoleInputReader.readBoolean("¿Es un evento con competencia y premiación de posiciones?");
        int orgId = ConsoleInputReader.readInt("ID del profesor organizador: ", 1, 99999);

        SchoolEvent ev = new SchoolEvent(null, name, desc, type, date, loc, comp, orgId, true);
        Integer id = extracurricularService.registerEvent(ev);
        if (id != null) {
            ConsoleHelper.printSuccess("Evento programado exitosamente con ID: " + id);
        } else {
            ConsoleHelper.printError("Error al registrar el evento.");
        }
        ConsoleInputReader.waitForEnter();
    }

    private void registerParticipant() {
        ConsoleHelper.printSubHeader("REGISTRO DE PARTICIPANTE EN EVENTO");
        int eventId = ConsoleInputReader.readInt("ID del evento: ", 1, 99999);
        int studentId = ConsoleInputReader.readInt("ID del estudiante: ", 1, 99999);

        System.out.println("Roles disponibles: Participante, Competidor, Organizador, Asistente");
        String role = ConsoleInputReader.readString("Rol del estudiante: ");
        String pos = ConsoleInputReader.readOptionalString("Posición obtenida si aplica (1er Lugar, 2do Lugar, 3er Lugar, Participante): ");
        String obs = ConsoleInputReader.readOptionalString("Observaciones o reconocimientos: ");

        boolean ok = extracurricularService.registerParticipant(eventId, studentId, role, pos.isEmpty() ? "Participante" : pos, obs);
        if (ok) {
            ConsoleHelper.printSuccess("Participación registrada exitosamente.");
        } else {
            ConsoleHelper.printError("Error al registrar la participación.");
        }
        ConsoleInputReader.waitForEnter();
    }

    private void viewEventParticipants() {
        ConsoleHelper.printSubHeader("LISTA DE PARTICIPANTES EN EVENTO");
        int eventId = ConsoleInputReader.readInt("ID del evento: ", 1, 99999);
        List<EventParticipant> participants = extracurricularService.getEventParticipants(eventId);

        if (participants.isEmpty()) {
            ConsoleHelper.printWarning("No hay participantes registrados para este evento.");
        } else {
            System.out.printf("%-5s | %-32s | %-15s | %-15s | %-25s%n", "ID", "ESTUDIANTE", "ROL", "POSICIÓN", "OBSERVACIONES");
            System.out.println("─".repeat(100));
            for (EventParticipant ep : participants) {
                System.out.printf("%-5d | %-32s | %-15s | %-15s | %-25s%n",
                        ep.getIdParticipation(), ep.getStudentName(), ep.getParticipationRole(),
                        ep.getPositionObtained() != null ? ep.getPositionObtained() : "N/A",
                        ep.getObservations() != null ? ep.getObservations() : "");
            }
            System.out.println("─".repeat(100));
        }
        ConsoleInputReader.waitForEnter();
    }
}
