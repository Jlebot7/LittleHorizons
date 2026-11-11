package com.littlehorizons.erp.model.academic;

import java.time.LocalDate;

/**
 * Domain entity representing a Student enrolled in Little Horizons School.
 */
public class Student {

    private Integer idStudent;
    private String documentType;
    private String documentNumber;
    private String firstName;
    private String secondName;
    private String firstLastName;
    private String secondLastName;
    private LocalDate birthDate;
    private String gender;
    private String address;
    private String phone;
    private String email;
    private String bloodType;
    private String allergies;
    private String medicalConditions;
    private LocalDate admissionDate;
    private String status;

    public Student() {
    }

    public Student(Integer idStudent, String documentType, String documentNumber,
                   String firstName, String secondName, String firstLastName, String secondLastName,
                   LocalDate birthDate, String gender, String address, String phone,
                   String email, String bloodType, String allergies, String medicalConditions,
                   LocalDate admissionDate, String status) {
        this.idStudent = idStudent;
        this.documentType = documentType;
        this.documentNumber = documentNumber;
        this.firstName = firstName;
        this.secondName = secondName;
        this.firstLastName = firstLastName;
        this.secondLastName = secondLastName;
        this.birthDate = birthDate;
        this.gender = gender;
        this.address = address;
        this.phone = phone;
        this.email = email;
        this.bloodType = bloodType;
        this.allergies = allergies;
        this.medicalConditions = medicalConditions;
        this.admissionDate = admissionDate;
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

    public Integer getIdStudent() {
        return idStudent;
    }

    public void setIdStudent(Integer idStudent) {
        this.idStudent = idStudent;
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

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getBloodType() {
        return bloodType;
    }

    public void setBloodType(String bloodType) {
        this.bloodType = bloodType;
    }

    public String getAllergies() {
        return allergies;
    }

    public void setAllergies(String allergies) {
        this.allergies = allergies;
    }

    public String getMedicalConditions() {
        return medicalConditions;
    }

    public void setMedicalConditions(String medicalConditions) {
        this.medicalConditions = medicalConditions;
    }

    public LocalDate getAdmissionDate() {
        return admissionDate;
    }

    public void setAdmissionDate(LocalDate admissionDate) {
        this.admissionDate = admissionDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return String.format("[%d] %s (%s %s) - %s",
                idStudent, getFullName(), documentType, documentNumber, status);
    }
}
