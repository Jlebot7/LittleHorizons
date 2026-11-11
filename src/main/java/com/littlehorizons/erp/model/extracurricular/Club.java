package com.littlehorizons.erp.model.extracurricular;

/**
 * Domain entity representing an Extracurricular Club.
 */
public class Club {

    private Integer idClub;
    private String clubName;
    private String description;
    private Integer idSupervisorTeacher;
    private Integer maxCapacity;
    private String meetingSchedule;
    private Boolean isActive;

    // Joined fields
    private String supervisorName;
    private Integer enrolledCount;

    public Club() {
    }

    public Club(Integer idClub, String clubName, String description, Integer idSupervisorTeacher,
                Integer maxCapacity, String meetingSchedule, Boolean isActive) {
        this.idClub = idClub;
        this.clubName = clubName;
        this.description = description;
        this.idSupervisorTeacher = idSupervisorTeacher;
        this.maxCapacity = maxCapacity;
        this.meetingSchedule = meetingSchedule;
        this.isActive = isActive;
    }

    public Integer getIdClub() {
        return idClub;
    }

    public void setIdClub(Integer idClub) {
        this.idClub = idClub;
    }

    public String getClubName() {
        return clubName;
    }

    public void setClubName(String clubName) {
        this.clubName = clubName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getIdSupervisorTeacher() {
        return idSupervisorTeacher;
    }

    public void setIdSupervisorTeacher(Integer idSupervisorTeacher) {
        this.idSupervisorTeacher = idSupervisorTeacher;
    }

    public Integer getMaxCapacity() {
        return maxCapacity;
    }

    public void setMaxCapacity(Integer maxCapacity) {
        this.maxCapacity = maxCapacity;
    }

    public String getMeetingSchedule() {
        return meetingSchedule;
    }

    public void setMeetingSchedule(String meetingSchedule) {
        this.meetingSchedule = meetingSchedule;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean active) {
        isActive = active;
    }

    public String getSupervisorName() {
        return supervisorName;
    }

    public void setSupervisorName(String supervisorName) {
        this.supervisorName = supervisorName;
    }

    public Integer getEnrolledCount() {
        return enrolledCount;
    }

    public void setEnrolledCount(Integer enrolledCount) {
        this.enrolledCount = enrolledCount;
    }

    @Override
    public String toString() {
        return String.format("[%d] %s (Capacidad: %d, Horario: %s)", idClub, clubName, maxCapacity, meetingSchedule);
    }
}
