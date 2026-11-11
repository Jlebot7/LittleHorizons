package com.littlehorizons.erp.model.extracurricular;

/**
 * Domain entity representing an Event Participation record.
 */
public class EventParticipant {

    private Integer idParticipation;
    private Integer idEvent;
    private Integer idStudent;
    private String participationRole; // Participante, Competidor, Organizador, Asistente
    private String positionObtained; // 1er Lugar, 2do Lugar, 3er Lugar, Participante
    private String observations;

    // Joined fields
    private String eventName;
    private String studentName;

    public EventParticipant() {
    }

    public EventParticipant(Integer idParticipation, Integer idEvent, Integer idStudent,
                            String participationRole, String positionObtained, String observations) {
        this.idParticipation = idParticipation;
        this.idEvent = idEvent;
        this.idStudent = idStudent;
        this.participationRole = participationRole;
        this.positionObtained = positionObtained;
        this.observations = observations;
    }

    public Integer getIdParticipation() {
        return idParticipation;
    }

    public void setIdParticipation(Integer idParticipation) {
        this.idParticipation = idParticipation;
    }

    public Integer getIdEvent() {
        return idEvent;
    }

    public void setIdEvent(Integer idEvent) {
        this.idEvent = idEvent;
    }

    public Integer getIdStudent() {
        return idStudent;
    }

    public void setIdStudent(Integer idStudent) {
        this.idStudent = idStudent;
    }

    public String getParticipationRole() {
        return participationRole;
    }

    public void setParticipationRole(String participationRole) {
        this.participationRole = participationRole;
    }

    public String getPositionObtained() {
        return positionObtained;
    }

    public void setPositionObtained(String positionObtained) {
        this.positionObtained = positionObtained;
    }

    public String getObservations() {
        return observations;
    }

    public void setObservations(String observations) {
        this.observations = observations;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    @Override
    public String toString() {
        return String.format("[%d] %s en %s (%s - %s)",
                idParticipation,
                studentName != null ? studentName : ("Estudiante " + idStudent),
                eventName != null ? eventName : ("Evento " + idEvent),
                participationRole,
                positionObtained != null ? positionObtained : "Sin posición");
    }
}
