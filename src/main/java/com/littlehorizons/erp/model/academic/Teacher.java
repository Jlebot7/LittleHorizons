package com.littlehorizons.erp.model.academic;

import java.time.LocalDate;

/**
 * Domain entity representing a Teacher / Professor.
 */
public class Teacher {

    private Integer idTeacher;
    private String documentType;
    private String documentNumber;
    private String firstName;
    private String secondName;
    private String firstLastName;
    private String secondLastName;
    private String email;
    private String phone;
    private String specialization;
    private LocalDate hireDate;
    private String status;

    public Teacher() {
    }

    public Teacher(Integer idTeacher, String documentType, String documentNumber,
                   String firstName, String secondName, String firstLastName, String secondLastName,
                   String email, String phone, String specialization, LocalDate hireDate, String status) {
        this.idTeacher = idTeacher;
        this.documentType = documentType;
        this.documentNumber = documentNumber;
        this.firstName = firstName;
        this.secondName = secondName;
        this.firstLastName = firstLastName;
        this.secondLastName = secondLastName;
        this.email = email;
        this.phone = phone;
        this.specialization = specialization;
        this.hireDate = hireDate;
        this.status = status;
    }

    public String getFullName() {
        StringBuilder sb = new StringBuilder(firstName);
        if (secondName != null && !secondName.trim().isEmpty()) {
            sb.append(" ").append(secondName);
        }
        sb.append(" ").append(firstLastName);
        if (secondLastName != null && !secondLastName.trim().isEmpty()) {
            sb.append(" ").append(secondLastName);
        }
        return sb.toString();
    }

    public Integer getIdTeacher() {
        return idTeacher;
    }

    public void setIdTeacher(Integer idTeacher) {
        this.idTeacher = idTeacher;
    }

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getSecondName() {
        return secondName;
    }

    public void setSecondName(String secondName) {
        this.secondName = secondName;
    }

    public String getFirstLastName() {
        return firstLastName;
    }

    public void setFirstLastName(String firstLastName) {
        this.firstLastName = firstLastName;
    }

    public String getSecondLastName() {
        return secondLastName;
    }

    public void setSecondLastName(String secondLastName) {
        this.secondLastName = secondLastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public LocalDate getHireDate() {
        return hireDate;
    }

    public void setHireDate(LocalDate hireDate) {
        this.hireDate = hireDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return String.format("[%d] %s (%s) - %s", idTeacher, getFullName(), specialization, email);
    }
}
