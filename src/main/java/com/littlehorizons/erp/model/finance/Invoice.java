package com.littlehorizons.erp.model.finance;

import java.time.LocalDate;

public class Invoice {

    private Integer idInvoice;
    private String invoiceNumber;
    private Integer idPayment;
    private LocalDate issueDate;
    private String cufe;
    private String pdfFileName;
    private String filePath;

    private String studentName;
    private String guardianName;
    private String conceptName;
    private java.math.BigDecimal amount;

    public Invoice() {
    }

    public Invoice(Integer idInvoice, String invoiceNumber, Integer idPayment,
                   LocalDate issueDate, String cufe, String pdfFileName, String filePath) {
        this.idInvoice = idInvoice;
        this.invoiceNumber = invoiceNumber;
        this.idPayment = idPayment;
        this.issueDate = issueDate;
        this.cufe = cufe;
        this.pdfFileName = pdfFileName;
        this.filePath = filePath;
    }

    public Integer getIdInvoice() {
        return idInvoice;
    }

    public void setIdInvoice(Integer idInvoice) {
        this.idInvoice = idInvoice;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public Integer getIdPayment() {
        return idPayment;
    }

    public void setIdPayment(Integer idPayment) {
        this.idPayment = idPayment;
    }

    public LocalDate getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(LocalDate issueDate) {
        this.issueDate = issueDate;
    }

    public String getCufe() {
        return cufe;
    }

    public void setCufe(String cufe) {
        this.cufe = cufe;
    }

    public String getPdfFileName() {
        return pdfFileName;
    }

    public void setPdfFileName(String pdfFileName) {
        this.pdfFileName = pdfFileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
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

    public java.math.BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(java.math.BigDecimal amount) {
        this.amount = amount;
    }

    @Override
    public String toString() {
        return String.format("[%d] Factura: %s | Fecha: %s | PDF: %s",
                idInvoice, invoiceNumber, issueDate, pdfFileName);
    }
}
