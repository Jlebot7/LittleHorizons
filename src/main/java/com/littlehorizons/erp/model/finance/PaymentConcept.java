package com.littlehorizons.erp.model.finance;

import java.math.BigDecimal;

public class PaymentConcept {

    private Integer idConcept;
    private String conceptCode;
    private String conceptName;
    private String description;
    private BigDecimal baseAmount;
    private Boolean isRecurring;
    private Boolean isActive;

    private BigDecimal taxRate;
    private Boolean isTaxExempt;

    public PaymentConcept() {
    }

    public PaymentConcept(Integer idConcept, String conceptCode, String conceptName,
                          String description, BigDecimal baseAmount, Boolean isRecurring, Boolean isActive) {
        this.idConcept = idConcept;
        this.conceptCode = conceptCode;
        this.conceptName = conceptName;
        this.description = description;
        this.baseAmount = baseAmount;
        this.isRecurring = isRecurring;
        this.isActive = isActive;
        evaluateTaxStatus();
    }

    public PaymentConcept(Integer idConcept, String conceptCode, String conceptName,
                          String description, BigDecimal baseAmount, Boolean isRecurring, Boolean isActive,
                          BigDecimal taxRate, Boolean isTaxExempt) {
        this.idConcept = idConcept;
        this.conceptCode = conceptCode;
        this.conceptName = conceptName;
        this.description = description;
        this.baseAmount = baseAmount;
        this.isRecurring = isRecurring;
        this.isActive = isActive;
        this.taxRate = taxRate;
        this.isTaxExempt = isTaxExempt;
    }

    public void evaluateTaxStatus() {
        if (taxRate != null && isTaxExempt != null) {
            return;
        }
        String name = (conceptName != null ? conceptName : "").toLowerCase();
        String code = (conceptCode != null ? conceptCode : "").toLowerCase();
        if (name.contains("uniforme") || name.contains("cafeteria") || name.contains("tienda") || code.startsWith("UNI")) {
            this.taxRate = new BigDecimal("0.19");
            this.isTaxExempt = false;
        } else {
            this.taxRate = BigDecimal.ZERO;
            this.isTaxExempt = true;
        }
    }

    public Integer getIdConcept() {
        return idConcept;
    }

    public void setIdConcept(Integer idConcept) {
        this.idConcept = idConcept;
    }

    public String getConceptCode() {
        return conceptCode;
    }

    public void setConceptCode(String conceptCode) {
        this.conceptCode = conceptCode;
        evaluateTaxStatus();
    }

    public String getConceptName() {
        return conceptName;
    }

    public void setConceptName(String conceptName) {
        this.conceptName = conceptName;
        evaluateTaxStatus();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getBaseAmount() {
        return baseAmount;
    }

    public void setBaseAmount(BigDecimal baseAmount) {
        this.baseAmount = baseAmount;
    }

    public Boolean getIsRecurring() {
        return isRecurring;
    }

    public void setIsRecurring(Boolean recurring) {
        isRecurring = recurring;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean active) {
        isActive = active;
    }

    public BigDecimal getTaxRate() {
        if (taxRate == null) {
            evaluateTaxStatus();
        }
        return taxRate;
    }

    public void setTaxRate(BigDecimal taxRate) {
        this.taxRate = taxRate;
    }

    public Boolean getIsTaxExempt() {
        if (isTaxExempt == null) {
            evaluateTaxStatus();
        }
        return isTaxExempt;
    }

    public void setIsTaxExempt(Boolean taxExempt) {
        isTaxExempt = taxExempt;
    }

    public String getTaxClassification() {
        if (Boolean.TRUE.equals(getIsTaxExempt())) {
            return "Excluido de IVA (Art. 476 num. 6 E.T. - Educación Formal)";
        }
        return "Gravado IVA 19% (Régimen General Art. 468 E.T.)";
    }

    @Override
    public String toString() {
        return String.format("[%d] %s (%s) - COP $%,.2f (%s) [%s]",
                idConcept, conceptName, conceptCode, baseAmount,
                Boolean.TRUE.equals(isRecurring) ? "Recurrente" : "Único",
                getTaxClassification());
    }
}