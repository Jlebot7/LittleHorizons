package com.littlehorizons.erp.model.extracurricular;

import java.time.LocalDate;

/**
 * Domain entity representing a Student Membership in a Club.
 */
public class ClubMembership {

    private Integer idMembership;
    private Integer idStudent;
    private Integer idClub;
    private LocalDate joinDate;
    private String status; // Activa, Retirada
    private LocalDate withdrawalDate;
    private String withdrawalReason;

    // Joined fields
    private String studentName;
    private String clubName;

    public ClubMembership() {
    }

    public ClubMembership(Integer idMembership, Integer idStudent, Integer idClub,
                          LocalDate joinDate, String status, LocalDate withdrawalDate, String withdrawalReason) {
        this.idMembership = idMembership;
        this.idStudent = idStudent;
        this.idClub = idClub;
        this.joinDate = joinDate;
        this.status = status;
        this.withdrawalDate = withdrawalDate;
        this.withdrawalReason = withdrawalReason;
    }

    public Integer getIdMembership() {
        return idMembership;
    }

    public void setIdMembership(Integer idMembership) {
        this.idMembership = idMembership;
    }

    public Integer getIdStudent() {
        return idStudent;
    }

    public void setIdStudent(Integer idStudent) {
        this.idStudent = idStudent;
    }

    public Integer getIdClub() {
        return idClub;
    }

    public void setIdClub(Integer idClub) {
        this.idClub = idClub;
    }

    public LocalDate getJoinDate() {
        return joinDate;
    }

    public void setJoinDate(LocalDate joinDate) {
        this.joinDate = joinDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDate getWithdrawalDate() {
        return withdrawalDate;
    }

    public void setWithdrawalDate(LocalDate withdrawalDate) {
        this.withdrawalDate = withdrawalDate;
    }

    public String getWithdrawalReason() {
        return withdrawalReason;
    }

    public void setWithdrawalReason(String withdrawalReason) {
        this.withdrawalReason = withdrawalReason;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getClubName() {
        return clubName;
    }

    public void setClubName(String clubName) {
        this.clubName = clubName;
    }

    @Override
    public String toString() {
        return String.format("[%d] %s en %s (%s)",
                idMembership,
                studentName != null ? studentName : ("Estudiante " + idStudent),
                clubName != null ? clubName : ("Club " + idClub),
                status);
    }
}
