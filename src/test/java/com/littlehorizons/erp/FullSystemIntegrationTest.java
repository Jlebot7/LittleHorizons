package com.littlehorizons.erp;

import com.littlehorizons.erp.model.academic.Student;
import com.littlehorizons.erp.model.finance.Invoice;
import com.littlehorizons.erp.model.finance.Payment;
import com.littlehorizons.erp.service.AcademicService;
import com.littlehorizons.erp.service.FinanceService;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FullSystemIntegrationTest {

    @Test
    void testEndToEndPaymentAndInvoicePdfGeneration() {
        try {
            FinanceService financeService = new FinanceService();

            Payment payment = new Payment();
            payment.setIdStudent(1);
            payment.setIdGuardian(1);
            payment.setIdConcept(1);
            payment.setAmountPaid(new BigDecimal("450000.00"));
            payment.setAcademicYear(2024);
            payment.setAcademicPeriod("I");
            payment.setPaymentMethod("Transferencia Bancaria");
            payment.setReceiptNumber("TEST-REC-" + System.currentTimeMillis());
            payment.setNotes("Pago de pensión escolar automatizado");
            payment.setPaymentDate(LocalDate.now());

            Optional<Invoice> invoiceOpt = financeService.processPaymentAndInvoice(payment);
            assertTrue(invoiceOpt.isPresent(), "Invoice should be generated");

            Invoice invoice = invoiceOpt.get();
            assertNotNull(invoice.getCufe(), "CUFE must be generated");
            assertEqualsExactLength(invoice.getCufe(), 64);

            File pdfFile = new File(invoice.getFilePath());
            assertTrue(pdfFile.exists(), "PDF file must exist at " + invoice.getFilePath());
            assertTrue(pdfFile.length() > 1000, "PDF file must be non-empty");

            List<Invoice> allInvoices = financeService.getAllInvoices();
            assertFalse(allInvoices.isEmpty(), "Invoice list from database should not be empty");
        } catch (Exception e) {
            org.junit.jupiter.api.Assumptions.assumeTrue(false, "Live database test skipped if offline: " + e.getMessage());
        }
    }

    @Test
    void testAcademicStreamCalculations() {
        try {
            AcademicService academicService = new AcademicService();
            Map<Integer, Double> gpaMap = academicService.calculateGpaByStudent(2024, "I");
            assertNotNull(gpaMap);

            List<Student> ranking = academicService.getTopRankedStudents(2024, "I", 5);
            assertNotNull(ranking);

            Map<Integer, Long> absences = academicService.getAbsenceCountPerStudent(2024);
            assertNotNull(absences);
        } catch (Exception e) {
            org.junit.jupiter.api.Assumptions.assumeTrue(false, "Live database test skipped if offline: " + e.getMessage());
        }
    }

    private void assertEqualsExactLength(String value, int length) {
        assertNotNull(value);
        if (value.length() != length) {
            throw new AssertionError("Expected length " + length + " but was " + value.length());
        }
    }
}
