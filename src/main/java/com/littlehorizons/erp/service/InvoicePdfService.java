package com.littlehorizons.erp.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.littlehorizons.erp.dao.academic.GuardianDAO;
import com.littlehorizons.erp.dao.academic.GuardianRepository;
import com.littlehorizons.erp.dao.academic.StudentDAO;
import com.littlehorizons.erp.dao.academic.StudentRepository;
import com.littlehorizons.erp.dao.finance.PaymentDAO;
import com.littlehorizons.erp.dao.finance.PaymentRepository;
import com.littlehorizons.erp.model.academic.Guardian;
import com.littlehorizons.erp.model.academic.Student;
import com.littlehorizons.erp.model.finance.Invoice;
import com.littlehorizons.erp.model.finance.Payment;
import com.littlehorizons.erp.model.finance.PaymentConcept;
import com.littlehorizons.erp.util.CufeGenerator;
import com.littlehorizons.erp.util.InvoiceContextResolver;
import com.littlehorizons.erp.util.LoggerUtil;
import com.littlehorizons.erp.util.TaxCalculator;
import com.littlehorizons.erp.util.TaxCalculator.TaxBreakdown;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class InvoicePdfService {

    private static final String INVOICES_DIR = "invoices";
    private static final String SCHOOL_NAME = "Colegio Internacional Little Horizons S.A.S.";
    private static final String SCHOOL_NIT = "900.543.210-9";
    private static final String SCHOOL_ADDRESS = "Calle 127 # 45-12, Bogotá D.C., Colombia";
    private static final String SCHOOL_PHONE = "+57 (601) 745-8900";
    private static final String SCHOOL_EMAIL = "facturacion@littlehorizons.edu.co";
    private static final String DIAN_RESOLUTION = "Resolución de Facturación Electrónica DIAN No. 18764000129845 de 2024-01-10 (Rango LH-0001 al LH-9999)";

    private final PaymentRepository paymentDAO;
    private final StudentRepository studentDAO;
    private final GuardianRepository guardianDAO;
    private final InvoiceTxtService invoiceTxtService;
    private final InvoiceXmlService invoiceXmlService;

    public InvoicePdfService() {
        this(new PaymentDAO(), new StudentDAO(), new GuardianDAO(),
                new InvoiceTxtService(), new InvoiceXmlService());
    }

    public InvoicePdfService(PaymentRepository paymentDAO,
                             StudentRepository studentDAO,
                             GuardianRepository guardianDAO,
                             InvoiceTxtService invoiceTxtService,
                             InvoiceXmlService invoiceXmlService) {
        this.paymentDAO = paymentDAO;
        this.studentDAO = studentDAO;
        this.guardianDAO = guardianDAO;
        this.invoiceTxtService = invoiceTxtService;
        this.invoiceXmlService = invoiceXmlService;
        ensureDirectoryExists();
    }

    private void ensureDirectoryExists() {
        File dir = new File(INVOICES_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    public Optional<Invoice> generateInvoiceForPayment(Payment payment) {
        if (payment == null) {
            LoggerUtil.error("Cannot generate invoice for null payment.");
            return Optional.empty();
        }

        try {
            int seq = 1;
            try {
                seq = paymentDAO.getNextInvoiceSequence();
            } catch (Exception ignored) {
                seq = (int) (System.currentTimeMillis() % 10000);
            }

            String invoiceNumber = String.format("FE-LH-2024-%04d", seq);
            LocalDate issueDate = payment.getPaymentDate() != null ? payment.getPaymentDate() : LocalDate.now();

            Invoice tempInvoice = new Invoice();
            tempInvoice.setIdPayment(payment.getIdPago());
            tempInvoice.setAmount(payment.getAmountPaid());

            InvoiceContextResolver.ResolvedInvoiceContext ctx =
                    InvoiceContextResolver.resolve(tempInvoice, paymentDAO, studentDAO, guardianDAO);

            Student student = ctx.student();
            Guardian guardian = ctx.guardian();
            PaymentConcept concept = ctx.concept();

            return generateInvoiceDirect(invoiceNumber, issueDate, student, guardian, concept, payment);

        } catch (Exception e) {
            LoggerUtil.error("Failed to generate electronic invoice (PDF, TXT, XML)", e);
            return Optional.empty();
        }
    }

    public Optional<Invoice> generateInvoiceDirect(String invoiceNumber, LocalDate issueDate,
                                                   Student student, Guardian guardian,
                                                   String conceptName, Payment payment) {
        PaymentConcept concept = new PaymentConcept();
        concept.setConceptName(conceptName);
        concept.setConceptCode("SRV-01");
        concept.setBaseAmount(payment != null ? payment.getAmountPaid() : BigDecimal.ZERO);
        return generateInvoiceDirect(invoiceNumber, issueDate, student, guardian, concept, payment);
    }

    public Optional<Invoice> generateInvoiceDirect(String invoiceNumber, LocalDate issueDate,
                                                   Student student, Guardian guardian,
                                                   PaymentConcept concept, Payment payment) {
        try {
            ensureDirectoryExists();

            BigDecimal amountPaid = payment != null && payment.getAmountPaid() != null
                    ? payment.getAmountPaid() : BigDecimal.ZERO;
            String buyerDocNumber = guardian != null && guardian.getDocumentNumber() != null
                    ? guardian.getDocumentNumber() : "222222222222";

            String cufe = CufeGenerator.generateCufe(invoiceNumber, issueDate, amountPaid, buyerDocNumber);

            TaxBreakdown tax = TaxCalculator.calculateTax(amountPaid, concept);

            String qrPayload = String.format("NumFac: %s\nFecFac: %s\nNitFac: %s\nDocAdq: %s\nValFac: %s\nCUFE: %s",
                    invoiceNumber, issueDate, SCHOOL_NIT, buyerDocNumber, amountPaid, cufe);
            byte[] qrBytes = generateQrCodeBytes(qrPayload, 150, 150);

            String pdfFileName = String.format("factura_%s.pdf", invoiceNumber);
            String pdfFilePath = INVOICES_DIR + File.separator + pdfFileName;
            writePdfFile(pdfFilePath, invoiceNumber, issueDate, cufe, student, guardian, concept.getConceptName(), payment, tax, qrBytes);

            String txtFileName = String.format("factura_%s.txt", invoiceNumber);
            String txtFilePath = INVOICES_DIR + File.separator + txtFileName;
            String txtContent = invoiceTxtService.generateTxtContent(invoiceNumber, issueDate, cufe, student, guardian, concept, payment, tax);
            invoiceTxtService.writeTxtFile(txtFilePath, txtContent);

            String xmlFileName = String.format("factura_%s.xml", invoiceNumber);
            String xmlFilePath = INVOICES_DIR + File.separator + xmlFileName;
            String xmlContent = invoiceXmlService.generateXmlContent(invoiceNumber, issueDate, cufe, student, guardian, concept, payment, tax);
            invoiceXmlService.writeXmlFile(xmlFilePath, xmlContent);

            Invoice invoice = new Invoice();
            invoice.setInvoiceNumber(invoiceNumber);
            invoice.setIdPayment(payment != null && payment.getIdPago() != null ? payment.getIdPago() : 1);
            invoice.setIssueDate(issueDate);
            invoice.setCufe(cufe);
            invoice.setPdfFileName(pdfFileName);
            invoice.setFilePath(pdfFilePath);

            invoice.setTxtFileName(txtFileName);
            invoice.setTxtFilePath(txtFilePath);
            invoice.setXmlFileName(xmlFileName);
            invoice.setXmlFilePath(xmlFilePath);

            invoice.setSubtotalAmount(tax.getBaseAmount());
            invoice.setTaxAmount(tax.getTaxAmount());
            invoice.setTaxRate(tax.getTaxRate());
            invoice.setIsTaxExempt(tax.isExempt());
            invoice.setTaxLegalReference(tax.getLegalReference());

            invoice.setStudentName(student != null ? student.getFullName() : "Estudiante");
            invoice.setGuardianName(guardian != null ? guardian.getFullName() : "Acudiente");
            invoice.setConceptName(concept != null ? concept.getConceptName() : "Servicio");
            invoice.setAmount(amountPaid);

            try {
                paymentDAO.saveInvoice(invoice);
            } catch (Exception e) {
                LoggerUtil.error("Could not persist invoice record into database (offline mode)", e);
            }

            return Optional.of(invoice);
        } catch (Exception e) {
            LoggerUtil.error("Failed writing physical invoice files", e);
            return Optional.empty();
        }
    }

    private void writePdfFile(String filePath, String invoiceNumber, LocalDate issueDate, String cufe,
                              Student student, Guardian guardian, String conceptName,
                              Payment payment, TaxBreakdown tax, byte[] qrBytes) throws Exception {

        Document document = new Document(PageSize.LETTER, 36, 36, 36, 36);
        PdfWriter.getInstance(document, new FileOutputStream(filePath));
        document.open();

        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, new Color(20, 50, 100));
        Font subTitleFont = FontFactory.getFont(FontFactory.HELVETICA, 8, Color.DARK_GRAY);
        Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.BLACK);
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.BLACK);
        Font cufeFont = FontFactory.getFont(FontFactory.COURIER, 7, Color.DARK_GRAY);

        PdfPTable headerTable = new PdfPTable(2);
        headerTable.setWidthPercentage(100);
        headerTable.setWidths(new float[]{60, 40});

        PdfPCell emitterCell = new PdfPCell();
        emitterCell.setBorder(Rectangle.NO_BORDER);
        emitterCell.addElement(new Paragraph(SCHOOL_NAME, titleFont));
        emitterCell.addElement(new Paragraph("NIT: " + SCHOOL_NIT + " - Régimen Común", boldFont));
        emitterCell.addElement(new Paragraph(SCHOOL_ADDRESS, subTitleFont));
        emitterCell.addElement(new Paragraph("Tel: " + SCHOOL_PHONE + " | " + SCHOOL_EMAIL, subTitleFont));
        emitterCell.addElement(new Paragraph(DIAN_RESOLUTION, subTitleFont));
        headerTable.addCell(emitterCell);

        PdfPCell invoiceBoxCell = new PdfPCell();
        invoiceBoxCell.setBorder(Rectangle.BOX);
        invoiceBoxCell.setBackgroundColor(new Color(245, 247, 250));
        invoiceBoxCell.setPadding(8);
        invoiceBoxCell.addElement(new Paragraph("FACTURA ELECTRÓNICA DE VENTA", boldFont));
        invoiceBoxCell.addElement(new Paragraph("No: " + invoiceNumber, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.RED)));
        invoiceBoxCell.addElement(new Paragraph("Fecha de Emisión: " + issueDate.format(DateTimeFormatter.ISO_LOCAL_DATE), normalFont));
        invoiceBoxCell.addElement(new Paragraph("Forma de Pago: " + (payment != null && payment.getPaymentMethod() != null ? payment.getPaymentMethod() : "Efectivo"), normalFont));
        headerTable.addCell(invoiceBoxCell);

        document.add(headerTable);
        document.add(new Paragraph(" "));

        PdfPTable partiesTable = new PdfPTable(2);
        partiesTable.setWidthPercentage(100);
        partiesTable.setWidths(new float[]{50, 50});

        PdfPCell guardianCell = new PdfPCell();
        guardianCell.setBorder(Rectangle.BOX);
        guardianCell.setPadding(6);
        guardianCell.addElement(new Paragraph("DATOS DEL ADQUIRENTE / ACUDIENTE:", boldFont));
        guardianCell.addElement(new Paragraph("Nombre: " + (guardian != null ? guardian.getFullName() : "N/A"), normalFont));
        guardianCell.addElement(new Paragraph("Documento: " + (guardian != null ? guardian.getDocumentType() + " " + guardian.getDocumentNumber() : "N/A"), normalFont));
        guardianCell.addElement(new Paragraph("Dirección: " + (guardian != null && guardian.getAddress() != null ? guardian.getAddress() : "Bogotá D.C."), normalFont));
        guardianCell.addElement(new Paragraph("Teléfono: " + (guardian != null && guardian.getMainPhone() != null ? guardian.getMainPhone() : "N/A"), normalFont));
        partiesTable.addCell(guardianCell);

        PdfPCell studentCell = new PdfPCell();
        studentCell.setBorder(Rectangle.BOX);
        studentCell.setPadding(6);
        studentCell.addElement(new Paragraph("BENEFICIARIO DEL SERVICIO (ESTUDIANTE):", boldFont));
        studentCell.addElement(new Paragraph("Nombre: " + (student != null ? student.getFullName() : "N/A"), normalFont));
        studentCell.addElement(new Paragraph("Documento: " + (student != null ? student.getDocumentType() + " " + student.getDocumentNumber() : "N/A"), normalFont));
        studentCell.addElement(new Paragraph("Código LH: STU-" + (student != null && student.getIdStudent() != null ? student.getIdStudent() : "001"), normalFont));
        partiesTable.addCell(studentCell);

        document.add(partiesTable);
        document.add(new Paragraph(" "));

        PdfPTable itemsTable = new PdfPTable(5);
        itemsTable.setWidthPercentage(100);
        itemsTable.setWidths(new float[]{8, 47, 10, 17, 18});

        String[] headers = {"ÍTEM", "DESCRIPCIÓN DEL CONCEPTO", "CANT", "VALOR UNIT.", "SUBTOTAL"};
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, boldFont));
            cell.setBackgroundColor(new Color(220, 230, 242));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            itemsTable.addCell(cell);
        }

        itemsTable.addCell(new Phrase("1", normalFont));
        itemsTable.addCell(new Phrase(conceptName + (payment != null && payment.getNotes() != null ? " (" + payment.getNotes() + ")" : ""), normalFont));
        itemsTable.addCell(new Phrase("1", normalFont));
        itemsTable.addCell(new Phrase(String.format("COP $%,.2f", tax.getBaseAmount()), normalFont));
        itemsTable.addCell(new Phrase(String.format("COP $%,.2f", tax.getBaseAmount()), normalFont));

        PdfPCell empty = new PdfPCell(new Phrase(""));
        empty.setColspan(3);
        empty.setBorder(Rectangle.NO_BORDER);

        itemsTable.addCell(empty);
        PdfPCell subCell = new PdfPCell(new Phrase("Base Imponible:", boldFont));
        subCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        itemsTable.addCell(subCell);
        itemsTable.addCell(new Phrase(String.format("COP $%,.2f", tax.getBaseAmount()), normalFont));

        String ivaLabel = tax.isExempt()
                ? "IVA (0% Excluido Art. 476):"
                : String.format("IVA (Tarifa %s%%):", tax.getTaxRate().multiply(new BigDecimal("100")).stripTrailingZeros().toPlainString());

        itemsTable.addCell(empty);
        PdfPCell ivaCell = new PdfPCell(new Phrase(ivaLabel, boldFont));
        ivaCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        itemsTable.addCell(ivaCell);
        itemsTable.addCell(new Phrase(String.format("COP $%,.2f", tax.getTaxAmount()), normalFont));

        itemsTable.addCell(empty);
        PdfPCell totalCell = new PdfPCell(new Phrase("TOTAL A PAGAR:", boldFont));
        totalCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totalCell.setBackgroundColor(new Color(230, 240, 255));
        itemsTable.addCell(totalCell);
        PdfPCell totalValCell = new PdfPCell(new Phrase(String.format("COP $%,.2f", tax.getTotalAmount()), boldFont));
        totalValCell.setBackgroundColor(new Color(230, 240, 255));
        itemsTable.addCell(totalValCell);

        document.add(itemsTable);
        document.add(new Paragraph(" "));

        PdfPTable footerTable = new PdfPTable(2);
        footerTable.setWidthPercentage(100);
        footerTable.setWidths(new float[]{75, 25});

        PdfPCell cufeCell = new PdfPCell();
        cufeCell.setBorder(Rectangle.BOX);
        cufeCell.addElement(new Paragraph("CÓDIGO ÚNICO DE FACTURA ELECTRÓNICA (CUFE):", boldFont));
        cufeCell.addElement(new Paragraph(cufe, cufeFont));
        cufeCell.addElement(new Paragraph(" "));
        cufeCell.addElement(new Paragraph(tax.getLegalReference(), subTitleFont));
        cufeCell.addElement(new Paragraph("Formatos generados: PDF, TXT plano y XML UBL 2.1 estándar DIAN/MUISCA.", subTitleFont));
        footerTable.addCell(cufeCell);

        PdfPCell qrCell = new PdfPCell();
        qrCell.setBorder(Rectangle.BOX);
        qrCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        if (qrBytes != null) {
            Image qrImg = Image.getInstance(qrBytes);
            qrImg.scaleToFit(100, 100);
            qrImg.setAlignment(Element.ALIGN_CENTER);
            qrCell.addElement(qrImg);
        }
        footerTable.addCell(qrCell);

        document.add(footerTable);
        document.close();
    }

    private byte[] generateQrCodeBytes(String text, int width, int height) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);
            BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "png", baos);
            return baos.toByteArray();
        } catch (Exception e) {
            LoggerUtil.error("Failed to generate QR code", e);
            return null;
        }
    }
}
