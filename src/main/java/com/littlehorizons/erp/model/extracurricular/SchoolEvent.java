package com.littlehorizons.erp.model.extracurricular;

import java.time.LocalDate;

public class SchoolEvent {

    private Integer idEvent;
    private String eventName;
    private String description;
    private String eventType; 
    private LocalDate eventDate;
    private String location;
    private Boolean hasCompetition;
    private Integer idOrganizer;
    private Boolean isActive;

    private String organizerName;

    public SchoolEvent() {
    }

    public SchoolEvent(Integer idEvent, String eventName, String description,
                       String eventType, LocalDate eventDate, String location,
                       Boolean hasCompetition, Integer idOrganizer, Boolean isActive) {
        this.idEvent = idEvent;
        this.eventName = eventName;
        this.description = description;
        this.eventType = eventType;
        this.eventDate = eventDate;
        this.location = location;
        this.hasCompetition = hasCompetition;
        this.idOrganizer = idOrganizer;
        this.isActive = isActive;
    }

    public Integer getIdEvent() {
        return idEvent;
    }

    public void setIdEvent(Integer idEvent) {
        this.idEvent = idEvent;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public LocalDate getEventDate() {
        return eventDate;
    }

    public void setEventDate(LocalDate eventDate) {
        this.eventDate = eventDate;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Boolean getHasCompetition() {
        return hasCompetition;
    }

    public void setHasCompetition(Boolean hasCompetition) {
        this.hasCompetition = hasCompetition;
    }

    public Integer getIdOrganizer() {
        return idOrganizer;
    }

    public void setIdOrganizer(Integer idOrganizer) {
        this.idOrganizer = idOrganizer;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean active) {
        isActive = active;
    }

    public String getOrganizerName() {
        return organizerName;
    }

    public void setOrganizerName(String organizerName) {
        this.organizerName = organizerName;
    }

    @Override
    public String toString() {
        return String.format("[%d] %s (%s) - Fecha: %s - Lugar: %s",
                idEvent, eventName, eventType, eventDate, location);
    }
}
