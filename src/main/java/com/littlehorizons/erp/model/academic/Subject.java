package com.littlehorizons.erp.model.academic;

/**
 * Domain entity representing an Academic Subject.
 */
public class Subject {

    private Integer idSubject;
    private String subjectCode;
    private String subjectName;
    private String description;
    private Integer idGrade;
    private Integer weeklyHours;
    private Boolean isActive;

    public Subject() {
    }

    public Subject(Integer idSubject, String subjectCode, String subjectName,
                   String description, Integer idGrade, Integer weeklyHours, Boolean isActive) {
        this.idSubject = idSubject;
        this.subjectCode = subjectCode;
        this.subjectName = subjectName;
        this.description = description;
        this.idGrade = idGrade;
        this.weeklyHours = weeklyHours;
        this.isActive = isActive;
    }

    public Integer getIdSubject() {
        return idSubject;
    }

    public void setIdSubject(Integer idSubject) {
        this.idSubject = idSubject;
    }

    public String getSubjectCode() {
        return subjectCode;
    }

    public void setSubjectCode(String subjectCode) {
        this.subjectCode = subjectCode;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getIdGrade() {
        return idGrade;
    }

    public void setIdGrade(Integer idGrade) {
        this.idGrade = idGrade;
    }

    public Integer getWeeklyHours() {
        return weeklyHours;
    }

    public void setWeeklyHours(Integer weeklyHours) {
        this.weeklyHours = weeklyHours;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean active) {
        isActive = active;
    }

    @Override
    public String toString() {
        return String.format("[%d] %s (%s) - %d horas/sem", idSubject, subjectName, subjectCode, weeklyHours);
    }
}
