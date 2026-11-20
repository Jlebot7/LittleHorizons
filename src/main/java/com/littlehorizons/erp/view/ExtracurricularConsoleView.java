package com.littlehorizons.erp.view;

import com.littlehorizons.erp.model.extracurricular.Club;
import com.littlehorizons.erp.model.extracurricular.ClubMembership;
import com.littlehorizons.erp.model.extracurricular.EventParticipant;
import com.littlehorizons.erp.model.extracurricular.SchoolEvent;

import java.util.List;

public class ExtracurricularConsoleView implements IExtracurricularView {

    @Override
    public void showMenu() {
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
    }

    @Override
    public void showClubs(List<Club> clubs) {
        ConsoleHelper.printSubHeader("CATÁLOGO DE CLUBES EXTRACURRICULARES");
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
    }

    @Override
    public void showRegisterClubHeader() {
        ConsoleHelper.printSubHeader("REGISTRO DE NUEVO CLUB");
    }

    @Override
    public void showEnrollClubHeader() {
        ConsoleHelper.printSubHeader("INSCRIPCIÓN DE ESTUDIANTE EN CLUB");
    }

    @Override
    public void showClubMembers(String clubName, List<ClubMembership> members) {
        ConsoleHelper.printSubHeader("MIEMBROS DEL CLUB");
        System.out.println("Integrantes del " + clubName + ":");
        if (members.isEmpty()) {
            ConsoleHelper.printWarning("El club no cuenta con estudiantes inscritos actualmente.");
        } else {
            System.out.printf("%-5s | %-32s | %-12s | %-10s%n", "ID", "ESTUDIANTE", "FECHA INGRESO", "ESTADO");
            System.out.println("─".repeat(70));
            for (ClubMembership m : members) {
                System.out.printf("%-5d | %-32s | %-12s | %-10s%n",
                        m.getIdMembership(), m.getStudentName(), m.getJoinDate(), m.getStatus());
            }
            System.out.println("─".repeat(70));
        }
    }

    @Override
    public void showEvents(List<SchoolEvent> events) {
        ConsoleHelper.printSubHeader("CALENDARIO GENERAL DE EVENTOS");
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
    }

    @Override
    public void showRegisterEventHeader() {
        ConsoleHelper.printSubHeader("REGISTRO DE EVENTO ESCOLAR");
    }

    @Override
    public void showEventRoles() {
        System.out.println("Tipos de evento: Académico, Cultural, Deportivo, Social, Científico");
    }

    @Override
    public void showRegisterParticipantHeader() {
        ConsoleHelper.printSubHeader("REGISTRO DE PARTICIPANTE EN EVENTO");
        System.out.println("Roles disponibles: Participante, Competidor, Organizador, Asistente");
    }

    @Override
    public void showEventParticipants(List<EventParticipant> participants) {
        ConsoleHelper.printSubHeader("LISTA DE PARTICIPANTES EN EVENTO");
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
    }

    @Override
    public void showSuccess(String message) {
        ConsoleHelper.printSuccess(message);
    }

    @Override
    public void showError(String message) {
        ConsoleHelper.printError(message);
    }

    @Override
    public void showWarning(String message) {
        ConsoleHelper.printWarning(message);
    }
}
