package com.littlehorizons.erp.model.academic;

import java.time.LocalDate;

/**
 * Domain entity representing daily student attendance per subject.
 */
public class Attendance {

    private Integer idAttendance;
    private Integer idStudent;
    private Integer idSubject;
    private LocalDate attendanceDate;
    private String status; // Presente, Ausente, Tardanza, Excusa Médica
    private String observations;
    private Integer recordedBy;

    // Joined fields
    private String studentName;
    private String subjectName;

    public Attendance() {
    }

    public Attendance(Integer idAttendance, Integer idStudent, Integer idSubject,
                      LocalDate attendanceDate, String status, String observations, Integer recordedBy) {
        this.idAttendance = idAttendance;
        this.idStudent = idStudent;
        this.idSubject = idSubject;
        this.attendanceDate = attendanceDate;
        this.status = status;
        this.observations = observations;
        this.recordedBy = recordedBy;
    }

    public Integer getIdAttendance() {
        return idAttendance;
    }

    public void setIdAttendance(Integer idAttendance) {
        this.idAttendance = idAttendance;
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

    public LocalDate getAttendanceDate() {
        return attendanceDate;
    }

    public void setAttendanceDate(LocalDate attendanceDate) {
        this.attendanceDate = attendanceDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getObservations() {
        return observations;
    }

    public void setObservations(String observations) {
        this.observations = observations;
    }

    public Integer getRecordedBy() {
        return recordedBy;
    }

    public void setRecordedBy(Integer recordedBy) {
        this.recordedBy = recordedBy;
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

    @Override
    public String toString() {
        return String.format("[%d] %s | %s | %s | Estado: %s",
                idAttendance, attendanceDate,
                studentName != null ? studentName : ("Estudiante " + idStudent),
                subjectName != null ? subjectName : ("Materia " + idSubject),
                status);
    }
}
