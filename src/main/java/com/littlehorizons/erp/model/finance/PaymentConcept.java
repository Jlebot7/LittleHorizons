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
    }

    public String getConceptName() {
        return conceptName;
    }

    public void setConceptName(String conceptName) {
        this.conceptName = conceptName;
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

    @Override
    public String toString() {
        return String.format("[%d] %s (%s) - COP $%,.2f (%s)",
                idConcept, conceptName, conceptCode, baseAmount, isRecurring ? "Recurrente" : "Único");
    }
}
