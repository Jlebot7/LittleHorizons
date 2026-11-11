package com.littlehorizons.erp.model.academic;

import java.math.BigDecimal;
import java.time.LocalDate;

public class AcademicRecord {

    private Integer idRecord;
    private Integer idStudent;
    private Integer idSubject;
    private Integer idTeacher;
    private Integer academicYear;
    private String academicPeriod; 
    private BigDecimal gradeValue;
    private String observations;
    private LocalDate recordDate;

    private String studentName;
    private String subjectName;
    private String teacherName;

    public AcademicRecord() {
    }

    public AcademicRecord(Integer idRecord, Integer idStudent, Integer idSubject,
                          Integer idTeacher, Integer academicYear, String academicPeriod,
                          BigDecimal gradeValue, String observations, LocalDate recordDate) {
        this.idRecord = idRecord;
        this.idStudent = idStudent;
        this.idSubject = idSubject;
        this.idTeacher = idTeacher;
        this.academicYear = academicYear;
        this.academicPeriod = academicPeriod;
        this.gradeValue = gradeValue;
        this.observations = observations;
        this.recordDate = recordDate;
    }

    public Integer getIdRecord() {
        return idRecord;
    }

    public void setIdRecord(Integer idRecord) {
        this.idRecord = idRecord;
    }

    public Integer getIdStudent() {
        return idStudent;
    }

    public void setIdStudent(Integer idStudent) {
        this.idStudent = idStudent;
    }

    public Integer getIdSubject() {
        return idSubject;
    }

    public void setIdSubject(Integer idSubject) {
        this.idSubject = idSubject;
    }

    public Integer getIdTeacher() {
        return idTeacher;
    }

    public void setIdTeacher(Integer idTeacher) {
        this.idTeacher = idTeacher;
    }

    public Integer getAcademicYear() {
        return academicYear;
    }

    public void setAcademicYear(Integer academicYear) {
        this.academicYear = academicYear;
    }

    public String getAcademicPeriod() {
        return academicPeriod;
    }

    public void setAcademicPeriod(String academicPeriod) {
        this.academicPeriod = academicPeriod;
    }

    public BigDecimal getGradeValue() {
        return gradeValue;
    }

    public void setGradeValue(BigDecimal gradeValue) {
        this.gradeValue = gradeValue;
    }

    public String getObservations() {
        return observations;
    }

    public void setObservations(String observations) {
        this.observations = observations;
    }

    public LocalDate getRecordDate() {
        return recordDate;
    }

    public void setRecordDate(LocalDate recordDate) {
        this.recordDate = recordDate;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    @Override
    public String toString() {
        return String.format("[%d] Estudiante: %s | Materia: %s | Período: %s | Nota: %s",
                idRecord,
                studentName != null ? studentName : ("ID " + idStudent),
                subjectName != null ? subjectName : ("ID " + idSubject),
                academicPeriod,
                gradeValue != null ? gradeValue.toString() : "N/A");
    }
}
