package com.littlehorizons.erp;

import com.littlehorizons.erp.model.finance.Invoice;
import com.littlehorizons.erp.model.finance.Payment;
import com.littlehorizons.erp.service.InvoicePdfService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class InvoicePdfGenerationTest {

    @Test
    @DisplayName("Should generate invoice PDF file and verify file creation on disk")
    void testPdfInvoiceCreation() {
        InvoicePdfService service = new InvoicePdfService();
        Payment payment = new Payment(
                9999,
                1,
                1,
                2,
                new BigDecimal("420000.00"),
                LocalDate.now(),
                "Transferencia",
                "REC-2024-TEST",
                2024,
                "I",
                "Prueba automatizada de generación PDF"
        );

        Optional<Invoice> invoiceOpt = service.generateInvoiceForPayment(payment);

        assertTrue(invoiceOpt.isPresent(), "Invoice must be generated");
        Invoice inv = invoiceOpt.get();
        assertNotNull(inv.getCufe(), "CUFE must not be null");
        assertNotNull(inv.getFilePath(), "File path must not be null");

        File pdfFile = new File(inv.getFilePath());
        assertTrue(pdfFile.exists(), "Generated PDF file must exist on disk");
        assertTrue(pdfFile.length() > 500, "PDF file must contain valid non-empty byte stream");
    }
}
