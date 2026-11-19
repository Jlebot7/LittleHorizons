package com.littlehorizons.erp.model.finance;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Invoice {

    private Integer idInvoice;
    private String invoiceNumber;
    private Integer idPayment;
    private LocalDate issueDate;
    private String cufe;
    private String pdfFileName;
    private String filePath;

    private String txtFileName;
    private String txtFilePath;
    private String xmlFileName;
    private String xmlFilePath;

    private BigDecimal subtotalAmount;
    private BigDecimal taxAmount;
    private BigDecimal taxRate;
    private Boolean isTaxExempt;
    private String taxLegalReference;

    private String studentName;
    private String guardianName;
    private String conceptName;
    private BigDecimal amount;

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
        initFilePaths();
    }

    private void initFilePaths() {
        if (invoiceNumber != null) {
            if (this.txtFileName == null) {
                this.txtFileName = String.format("factura_%s.txt", invoiceNumber);
                this.txtFilePath = "invoices/" + this.txtFileName;
            }
            if (this.xmlFileName == null) {
                this.xmlFileName = String.format("factura_%s.xml", invoiceNumber);
                this.xmlFilePath = "invoices/" + this.xmlFileName;
            }
        }
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
        initFilePaths();
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

    public String getTxtFileName() {
        if (txtFileName == null && invoiceNumber != null) {
            txtFileName = String.format("factura_%s.txt", invoiceNumber);
        }
        return txtFileName;
    }

    public void setTxtFileName(String txtFileName) {
        this.txtFileName = txtFileName;
    }

    public String getTxtFilePath() {
        if (txtFilePath == null && getTxtFileName() != null) {
            txtFilePath = "invoices/" + getTxtFileName();
        }
        return txtFilePath;
    }

    public void setTxtFilePath(String txtFilePath) {
        this.txtFilePath = txtFilePath;
    }

    public String getXmlFileName() {
        if (xmlFileName == null && invoiceNumber != null) {
            xmlFileName = String.format("factura_%s.xml", invoiceNumber);
        }
        return xmlFileName;
    }

    public void setXmlFileName(String xmlFileName) {
        this.xmlFileName = xmlFileName;
    }

    public String getXmlFilePath() {
        if (xmlFilePath == null && getXmlFileName() != null) {
            xmlFilePath = "invoices/" + getXmlFileName();
        }
        return xmlFilePath;
    }

    public void setXmlFilePath(String xmlFilePath) {
        this.xmlFilePath = xmlFilePath;
    }

    public BigDecimal getSubtotalAmount() {
        return subtotalAmount;
    }

    public void setSubtotalAmount(BigDecimal subtotalAmount) {
        this.subtotalAmount = subtotalAmount;
    }

    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(BigDecimal taxAmount) {
        this.taxAmount = taxAmount;
    }

    public BigDecimal getTaxRate() {
        return taxRate;
    }

    public void setTaxRate(BigDecimal taxRate) {
        this.taxRate = taxRate;
    }

    public Boolean getIsTaxExempt() {
        return isTaxExempt;
    }

    public void setIsTaxExempt(Boolean taxExempt) {
        isTaxExempt = taxExempt;
    }

    public String getTaxLegalReference() {
        return taxLegalReference;
    }

    public void setTaxLegalReference(String taxLegalReference) {
        this.taxLegalReference = taxLegalReference;
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

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    @Override
    public String toString() {
        return String.format("[%d] Factura: %s | Fecha: %s | PDF: %s | TXT: %s | XML: %s",
                idInvoice, invoiceNumber, issueDate, pdfFileName, getTxtFileName(), getXmlFileName());
    }
}