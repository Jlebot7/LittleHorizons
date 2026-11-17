package com.littlehorizons.erp;

import com.littlehorizons.erp.model.academic.Guardian;
import com.littlehorizons.erp.model.academic.Student;
import com.littlehorizons.erp.model.finance.Invoice;
import com.littlehorizons.erp.model.finance.Payment;
import com.littlehorizons.erp.model.finance.PaymentConcept;
import com.littlehorizons.erp.service.InvoicePdfService;
import com.littlehorizons.erp.service.InvoiceTxtService;
import com.littlehorizons.erp.service.InvoiceXmlService;
import com.littlehorizons.erp.util.TaxCalculator;
import com.littlehorizons.erp.util.TaxCalculator.TaxBreakdown;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.StringReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class InvoiceMultiFormatTaxTest {

    @Test
    @DisplayName("Estatuto Tributario Art. 476: Educational services must be tax-exempt (IVA 0%)")
    void testEducationalServicesTaxExempt() {
        BigDecimal amount = new BigDecimal("350000.00");
        PaymentConcept educationalConcept = new PaymentConcept();
        educationalConcept.setConceptName("Pensión Escolar Mensual");
        educationalConcept.setConceptCode("PEN-001");

        TaxBreakdown tax = TaxCalculator.calculateTax(amount, educationalConcept);

        assertTrue(tax.isExempt(), "Educational service should be tax-exempt under Art. 476 E.T.");
        assertEquals(0, BigDecimal.ZERO.compareTo(tax.getTaxRate()), "Tax rate should be 0%");
        assertEquals(0, BigDecimal.ZERO.compareTo(tax.getTaxAmount()), "Tax amount should be 0 COP");
        assertEquals(0, amount.compareTo(tax.getBaseAmount()), "Base amount should equal total amount paid");
        assertEquals(0, amount.compareTo(tax.getTotalAmount()), "Total amount should match total amount paid");
        assertTrue(tax.getLegalReference().contains("476"), "Legal reference must cite Art. 476 of Estatuto Tributario");
        assertEquals("ZZ", tax.getDianTaxSchemeCode(), "Exempt services use code ZZ or exempt scheme in MUISCA");
    }

    @Test
    @DisplayName("Estatuto Tributario Art. 468: Commercial goods (uniforms) must calculate 19% IVA")
    void testCommercialGoodsTaxedAt19Percent() {
        // Un uniforme que cuesta 119,000 COP con IVA incluido: base = 100,000, IVA = 19,000
        BigDecimal totalPaid = new BigDecimal("119000.00");
        PaymentConcept uniformConcept = new PaymentConcept();
        uniformConcept.setConceptName("Uniforme Deportivo Oficial");
        uniformConcept.setConceptCode("UNI-001");

        TaxBreakdown tax = TaxCalculator.calculateTax(totalPaid, uniformConcept);

        assertFalse(tax.isExempt(), "Commercial items should not be exempt");
        assertEquals(new BigDecimal("0.19"), tax.getTaxRate(), "Tax rate should be 19%");
        assertEquals(new BigDecimal("100000.00"), tax.getBaseAmount(), "Base amount should be 100,000.00");
        assertEquals(new BigDecimal("19000.00"), tax.getTaxAmount(), "Tax amount should be 19,000.00");
        assertEquals(totalPaid, tax.getTotalAmount(), "Total should match total paid");
        assertTrue(tax.getLegalReference().contains("468"), "Legal reference must cite Art. 468 of Estatuto Tributario");
        assertEquals("01", tax.getDianTaxSchemeCode(), "Tax scheme for IVA is 01");
    }

    @Test
    @DisplayName("InvoiceTxtService: Generates clean ASCII/TXT invoice conforming to Art. 617 E.T.")
    void testInvoiceTxtGeneration() {
        InvoiceTxtService txtService = new InvoiceTxtService();
        Student student = createSampleStudent();
        Guardian guardian = createSampleGuardian();
        PaymentConcept concept = new PaymentConcept(1, "MAT-01", "Matrícula Anual 2024", "Matrícula",
                new BigDecimal("450000.00"), false, true);
        Payment payment = new Payment(1, 1, 1, 1, new BigDecimal("450000.00"), LocalDate.now(),
                "Transferencia", "REC-2024-TEST-01", 2024, "I", "Pago en línea");
        TaxBreakdown tax = TaxCalculator.calculateTax(payment.getAmountPaid(), concept);

        String cufe = "1234567890abcdef1234567890abcdef1234567890abcdef1234567890abcdef";
        String txt = txtService.generateTxtContent("FE-LH-2024-TEST", LocalDate.now(), cufe,
                student, guardian, concept, payment, tax);

        assertNotNull(txt);
        assertTrue(txt.contains("FACTURA ELECTRÓNICA DE VENTA"), "Should contain electronic invoice title");
        assertTrue(txt.contains("900.543.210-9"), "Should contain school NIT (Art. 617 Lit. b)");
        assertTrue(txt.contains("FE-LH-2024-TEST"), "Should contain invoice number (Art. 617 Lit. c)");
        assertTrue(txt.contains("Art. 476"), "Should contain tax legal reference");
        assertTrue(txt.contains(cufe), "Should contain CUFE");
        assertTrue(txt.contains(guardian.getFullName()), "Should contain buyer/guardian name (Art. 617 Lit. e)");
    }

    @Test
    @DisplayName("InvoiceXmlService: Generates valid well-formed UBL 2.1 XML typical of MUISCA / DIAN standard")
    void testInvoiceXmlGenerationAndWellFormed() throws Exception {
        InvoiceXmlService xmlService = new InvoiceXmlService();
        Student student = createSampleStudent();
        Guardian guardian = createSampleGuardian();
        PaymentConcept concept = new PaymentConcept(2, "UNI-01", "Uniforme Escolar Diario", "Uniforme",
                new BigDecimal("119000.00"), false, true);
        Payment payment = new Payment(2, 1, 1, 2, new BigDecimal("119000.00"), LocalDate.now(),
                "Tarjeta Débito", "REC-2024-TEST-02", 2024, "I", "Compra en tienda");
        TaxBreakdown tax = TaxCalculator.calculateTax(payment.getAmountPaid(), concept);

        String cufe = "abcdef1234567890abcdef1234567890abcdef1234567890abcdef1234567890";
        String xml = xmlService.generateXmlContent("FE-LH-2024-XML-TEST", LocalDate.now(), cufe,
                student, guardian, concept, payment, tax);

        assertNotNull(xml);
        assertTrue(xml.contains("urn:oasis:names:specification:ubl:schema:xsd:Invoice-2"), "Must declare UBL 2.1 namespace");
        assertTrue(xml.contains("dian:DianExtensions"), "Must include DIAN extensions");
        assertTrue(xml.contains("<cbc:UBLVersionID>UBL 2.1</cbc:UBLVersionID>"), "Must declare UBL 2.1 version");
        assertTrue(xml.contains("<cac:TaxTotal>"), "Must include TaxTotal section");
        assertTrue(xml.contains(cufe), "Must embed CUFE");

        // Parse XML to guarantee it is strictly well-formed
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new InputSource(new StringReader(xml)));

        assertNotNull(doc);
        assertEquals("Invoice", doc.getDocumentElement().getLocalName());
    }

    @Test
    @DisplayName("Multi-Format Generation: Generates PDF, TXT and XML in parallel upon invoice emission")
    void testMultiFormatGenerationCreatesAllThreeFiles() {
        InvoicePdfService service = new InvoicePdfService();
        Student student = createSampleStudent();
        Guardian guardian = createSampleGuardian();
        PaymentConcept concept = new PaymentConcept(1, "PEN-01", "Pensión Mensual Febrero", "Pensión",
                new BigDecimal("350000.00"), true, true);
        Payment payment = new Payment(100, 1, 1, 1, new BigDecimal("350000.00"), LocalDate.now(),
                "Efectivo", "REC-2024-TEST-99", 2024, "I", "Pago en caja");

        String invoiceNum = "FE-LH-2024-TEST-MULTI";
        Optional<Invoice> result = service.generateInvoiceDirect(invoiceNum, LocalDate.now(), student, guardian, concept, payment);

        assertTrue(result.isPresent(), "Invoice generation should succeed");
        Invoice inv = result.get();

        File pdfFile = new File(inv.getFilePath());
        File txtFile = new File(inv.getTxtFilePath());
        File xmlFile = new File(inv.getXmlFilePath());

        assertTrue(pdfFile.exists(), "PDF file must exist on disk: " + pdfFile.getAbsolutePath());
        assertTrue(txtFile.exists(), "TXT file must exist on disk: " + txtFile.getAbsolutePath());
        assertTrue(xmlFile.exists(), "XML file must exist on disk: " + xmlFile.getAbsolutePath());

        assertTrue(pdfFile.length() > 0, "PDF file should not be empty");
        assertTrue(txtFile.length() > 0, "TXT file should not be empty");
        assertTrue(xmlFile.length() > 0, "XML file should not be empty");

        // Cleanup test files
        pdfFile.deleteOnExit();
        txtFile.deleteOnExit();
        xmlFile.deleteOnExit();
    }

    private Student createSampleStudent() {
        Student s = new Student();
        s.setIdStudent(1);
        s.setFirstName("Valeria");
        s.setFirstLastName("Gómez");
        s.setDocumentType("TI");
        s.setDocumentNumber("1012345678");
        s.setAddress("Calle 100 # 15-20");
        return s;
    }

    private Guardian createSampleGuardian() {
        Guardian g = new Guardian();
        g.setIdGuardian(1);
        g.setFirstName("Carlos");
        g.setFirstLastName("Gómez");
        g.setDocumentType("CC");
        g.setDocumentNumber("80123456");
        g.setAddress("Calle 100 # 15-20, Bogotá D.C.");
        g.setMainPhone("+57 300 123 4567");
        return g;
    }
}
