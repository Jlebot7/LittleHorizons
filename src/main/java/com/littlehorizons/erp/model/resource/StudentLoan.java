package com.littlehorizons.erp.model.resource;

import java.time.LocalDate;

public class StudentLoan {

    private Integer idLoan;
    private Integer idResource;
    private Integer idStudent;
    private LocalDate loanDate;
    private LocalDate expectedReturnDate;
    private LocalDate actualReturnDate;
    private String status; 
    private String observations;

    private String resourceName;
    private String studentName;

    public StudentLoan() {
    }

    public StudentLoan(Integer idLoan, Integer idResource, Integer idStudent,
                       LocalDate loanDate, LocalDate expectedReturnDate,
                       LocalDate actualReturnDate, String status, String observations) {
        this.idLoan = idLoan;
        this.idResource = idResource;
        this.idStudent = idStudent;
        this.loanDate = loanDate;
        this.expectedReturnDate = expectedReturnDate;
        this.actualReturnDate = actualReturnDate;
        this.status = status;
        this.observations = observations;
    }

    public Integer getIdLoan() {
        return idLoan;
    }

    public void setIdLoan(Integer idLoan) {
        this.idLoan = idLoan;
    }

    public Integer getIdResource() {
        return idResource;
    }

    public void setIdResource(Integer idResource) {
        this.idResource = idResource;
    }

    public Integer getIdStudent() {
        return idStudent;
    }

    public void setIdStudent(Integer idStudent) {
        this.idStudent = idStudent;
    }

    public LocalDate getLoanDate() {
        return loanDate;
    }

    public void setLoanDate(LocalDate loanDate) {
        this.loanDate = loanDate;
    }

    public LocalDate getExpectedReturnDate() {
        return expectedReturnDate;
    }

    public void setExpectedReturnDate(LocalDate expectedReturnDate) {
        this.expectedReturnDate = expectedReturnDate;
    }

    public LocalDate getActualReturnDate() {
        return actualReturnDate;
    }

    public void setActualReturnDate(LocalDate actualReturnDate) {
        this.actualReturnDate = actualReturnDate;
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

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    @Override
    public String toString() {
        return String.format("[%d] Recurso: %s | Estudiante: %s | Límite: %s | Estado: %s",
                idLoan,
                resourceName != null ? resourceName : ("Recurso " + idResource),
                studentName != null ? studentName : ("Estudiante " + idStudent),
                expectedReturnDate, status);
    }
}
