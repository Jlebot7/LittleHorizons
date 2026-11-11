package com.littlehorizons.erp.model.finance;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Domain entity representing a Payment transaction.
 */
public class Payment {

    private Integer idPago;
    private Integer idStudent;
    private Integer idGuardian;
    private Integer idConcept;
    private BigDecimal amountPaid;
    private LocalDate paymentDate;
    private String paymentMethod; // Efectivo, Transferencia, Tarjeta Débito, Tarjeta Crédito, Cheque
    private String receiptNumber;
    private Integer academicYear;
    private String academicPeriod; // I, II, III, IV, Anual
    private String notes;

    // Joined fields
    private String studentName;
    private String guardianName;
    private String conceptName;

    public Payment() {
    }

    public Payment(Integer idPago, Integer idStudent, Integer idGuardian, Integer idConcept,
                   BigDecimal amountPaid, LocalDate paymentDate, String paymentMethod,
                   String receiptNumber, Integer academicYear, String academicPeriod, String notes) {
        this.idPago = idPago;
        this.idStudent = idStudent;
        this.idGuardian = idGuardian;
        this.idConcept = idConcept;
        this.amountPaid = amountPaid;
        this.paymentDate = paymentDate;
        this.paymentMethod = paymentMethod;
        this.receiptNumber = receiptNumber;
        this.academicYear = academicYear;
        this.academicPeriod = academicPeriod;
        this.notes = notes;
    }

    public Integer getIdPago() {
        return idPago;
    }

    public void setIdPago(Integer idPago) {
        this.idPago = idPago;
    }

    public Integer getIdStudent() {
        return idStudent;
    }

    public void setIdStudent(Integer idStudent) {
        this.idStudent = idStudent;
    }

    public Integer getIdGuardian() {
        return idGuardian;
    }

    public void setIdGuardian(Integer idGuardian) {
        this.idGuardian = idGuardian;
    }

    public Integer getIdConcept() {
        return idConcept;
    }

    public void setIdConcept(Integer idConcept) {
        this.idConcept = idConcept;
    }

    public BigDecimal getAmountPaid() {
        return amountPaid;
    }

    public void setAmountPaid(BigDecimal amountPaid) {
        this.amountPaid = amountPaid;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDate paymentDate) {
        this.paymentDate = paymentDate;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getReceiptNumber() {
        return receiptNumber;
    }

    public void setReceiptNumber(String receiptNumber) {
        this.receiptNumber = receiptNumber;
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

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getGuardianName() {
        return guardianName;
    }

    public void setGuardianName(String guardianName) {
        this.guardianName = guardianName;
    }

    public String getConceptName() {
        return conceptName;
    }

    public void setConceptName(String conceptName) {
        this.conceptName = conceptName;
    }

    @Override
    public String toString() {
        return String.format("[%d] Recibo: %s | Estudiante: %s | Concepto: %s | Monto: COP $%,.2f (%s)",
                idPago, receiptNumber,
                studentName != null ? studentName : ("ID " + idStudent),
                conceptName != null ? conceptName : ("Concepto " + idConcept),
                amountPaid, paymentMethod);
    }
}
