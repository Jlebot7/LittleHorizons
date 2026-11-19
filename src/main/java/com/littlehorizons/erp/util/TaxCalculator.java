package com.littlehorizons.erp.util;

import com.littlehorizons.erp.model.finance.PaymentConcept;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class TaxCalculator {

    public static final BigDecimal GENERAL_IVA_RATE = new BigDecimal("0.19");
    public static final BigDecimal EXEMPT_IVA_RATE = BigDecimal.ZERO;

    public static final String LEGAL_REF_EXCLUDED = "Excluido de IVA - Art. 476 Numeral 6 del Estatuto Tributario (Servicios de Educación Formal)";
    public static final String LEGAL_REF_TAXED = "Gravado con IVA 19% - Régimen Ordinario Art. 468 del Estatuto Tributario";

    private TaxCalculator() {
    }

    public static class TaxBreakdown {
        private final BigDecimal totalAmount;
        private final BigDecimal baseAmount;
        private final BigDecimal taxAmount;
        private final BigDecimal taxRate;
        private final boolean isExempt;
        private final String legalReference;

        public TaxBreakdown(BigDecimal totalAmount, BigDecimal baseAmount, BigDecimal taxAmount,
                            BigDecimal taxRate, boolean isExempt, String legalReference) {
            this.totalAmount = totalAmount.setScale(2, RoundingMode.HALF_UP);
            this.baseAmount = baseAmount.setScale(2, RoundingMode.HALF_UP);
            this.taxAmount = taxAmount.setScale(2, RoundingMode.HALF_UP);
            this.taxRate = taxRate.setScale(2, RoundingMode.HALF_UP);
            this.isExempt = isExempt;
            this.legalReference = legalReference;
        }

        public BigDecimal getTotalAmount() {
            return totalAmount;
        }

        public BigDecimal getBaseAmount() {
            return baseAmount;
        }

        public BigDecimal getTaxAmount() {
            return taxAmount;
        }

        public BigDecimal getTaxRate() {
            return taxRate;
        }

        public boolean isExempt() {
            return isExempt;
        }

        public String getLegalReference() {
            return legalReference;
        }

        public String getDianTaxSchemeCode() {
            return isExempt ? "ZZ" : "01";
        }

        public String getDianTaxSchemeName() {
            return isExempt ? "Excluido" : "IVA";
        }
    }

    public static TaxBreakdown calculateTax(BigDecimal amountPaid, PaymentConcept concept) {
        BigDecimal total = (amountPaid != null && amountPaid.compareTo(BigDecimal.ZERO) > 0)
                ? amountPaid
                : BigDecimal.ZERO;

        boolean isExempt = true;
        BigDecimal rate = EXEMPT_IVA_RATE;

        if (concept != null) {
            concept.evaluateTaxStatus();
            isExempt = Boolean.TRUE.equals(concept.getIsTaxExempt());
            rate = concept.getTaxRate() != null ? concept.getTaxRate() : EXEMPT_IVA_RATE;
        }

        if (isExempt || rate.compareTo(BigDecimal.ZERO) == 0) {
            return new TaxBreakdown(total, total, BigDecimal.ZERO, EXEMPT_IVA_RATE, true, LEGAL_REF_EXCLUDED);
        } else {
            BigDecimal divisor = BigDecimal.ONE.add(rate);
            BigDecimal base = total.divide(divisor, 4, RoundingMode.HALF_UP);
            BigDecimal tax = total.subtract(base);
            return new TaxBreakdown(total, base, tax, rate, false, LEGAL_REF_TAXED);
        }
    }

    public static TaxBreakdown calculateTax(BigDecimal amountPaid, String conceptName) {
        PaymentConcept dummy = new PaymentConcept();
        dummy.setConceptName(conceptName);
        dummy.evaluateTaxStatus();
        return calculateTax(amountPaid, dummy);
    }
}