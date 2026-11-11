package com.littlehorizons.erp.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.littlehorizons.erp.dao.academic.GuardianDAO;
import com.littlehorizons.erp.dao.academic.StudentDAO;
import com.littlehorizons.erp.dao.finance.PaymentDAO;
import com.littlehorizons.erp.model.academic.Guardian;
import com.littlehorizons.erp.model.academic.Student;
import com.littlehorizons.erp.model.finance.Invoice;
import com.littlehorizons.erp.model.finance.Payment;
import com.littlehorizons.erp.model.finance.PaymentConcept;
import com.littlehorizons.erp.util.CufeGenerator;
import com.littlehorizons.erp.util.LoggerUtil;
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

    private final PaymentDAO paymentDAO;
    private final StudentDAO studentDAO;
    private final GuardianDAO guardianDAO;

    public InvoicePdfService() {
        this.paymentDAO = new PaymentDAO();
        this.studentDAO = new StudentDAO();
        this.guardianDAO = new GuardianDAO();
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

            Student student = null;
            if (payment.getIdStudent() != null) {
                try {
                    student = studentDAO.findById(payment.getIdStudent()).orElse(null);
                } catch (Exception ignored) {
                }
            }
            if (student == null) {
                student = new Student();
                student.setFirstName("Estudiante");
                student.setFirstLastName("LH");
                student.setDocumentType("TI");
                student.setDocumentNumber("1098000000");
                student.setAddress("Bogotá D.C.");
            }

            Guardian guardian = null;
            if (payment.getIdGuardian() != null) {
                try {
                    guardian = guardianDAO.findById(payment.getIdGuardian()).orElse(null);
                } catch (Exception ignored) {
                }
            }
            if (guardian == null) {
                guardian = new Guardian();
                guardian.setFirstName(student.getFirstName());
                guardian.setFirstLastName(student.getFirstLastName());
                guardian.setDocumentType("CC");
                guardian.setDocumentNumber(student.getDocumentNumber());
                guardian.setAddress(student.getAddress());
            }

            String conceptName = "Servicios Educativos / Pensión";
            if (payment.getIdConcept() != null) {
                try {
                    conceptName = paymentDAO.findConceptById(payment.getIdConcept())
                            .map(PaymentConcept::getConceptName).orElse("Pensión Escolar");
                } catch (Exception ignored) {
                }
            }

            return generateInvoiceDirect(invoiceNumber, issueDate, student, guardian, conceptName, payment);

        } catch (Exception e) {
            LoggerUtil.error("Failed to generate electronic invoice PDF", e);
            return Optional.empty();
        }
    }

    public Optional<Invoice> generateInvoiceDirect(String invoiceNumber, LocalDate issueDate,
                                                   Student student, Guardian guardian,
                                                   String conceptName, Payment payment) {
        try {
            ensureDirectoryExists();
            String cufe = CufeGenerator.generateCufe(invoiceNumber, issueDate, payment.getAmountPaid(), guardian.getDocumentNumber());

            String qrPayload = String.format("NumFac: %s\nFecFac: %s\nNitFac: %s\nDocAdq: %s\nValFac: %s\nCUFE: %s",
                    invoiceNumber, issueDate, SCHOOL_NIT, guardian.getDocumentNumber(), payment.getAmountPaid(), cufe);
            byte[] qrBytes = generateQrCodeBytes(qrPayload, 150, 150);

            String pdfFileName = String.format("factura_%s.pdf", invoiceNumber);
            String filePath = INVOICES_DIR + File.separator + pdfFileName;

            writePdfFile(filePath, invoiceNumber, issueDate, cufe, student, guardian, conceptName, payment, qrBytes);

            Invoice invoice = new Invoice();
            invoice.setInvoiceNumber(invoiceNumber);
            invoice.setIdPayment(payment.getIdPago() != null ? payment.getIdPago() : 1);
            invoice.setIssueDate(issueDate);
            invoice.setCufe(cufe);
            invoice.setPdfFileName(pdfFileName);
            invoice.setFilePath(filePath);

            try {
                paymentDAO.saveInvoice(invoice);
            } catch (Exception ignored) {
            }

            LoggerUtil.info("Generated PDF invoice: " + filePath + " with CUFE: " + cufe);
            return Optional.of(invoice);
        } catch (Exception e) {
            LoggerUtil.error("Error in generateInvoiceDirect", e);
            return Optional.empty();
        }
    }

    private void writePdfFile(String filePath, String invoiceNumber, LocalDate issueDate, String cufe,
                              Student student, Guardian guardian, String conceptName, Payment payment,
                              byte[] qrBytes) throws Exception {

        Document document = new Document(PageSize.A4, 36, 36, 36, 36);
        PdfWriter.getInstance(document, new FileOutputStream(filePath));
        document.open();

        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, Color.DARK_GRAY);
        Font subTitleFont = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.GRAY);
        Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.BLACK);
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.BLACK);
        Font cufeFont = FontFactory.getFont(FontFactory.COURIER, 7, Color.DARK_GRAY);

        PdfPTable headerTable = new PdfPTable(2);
        headerTable.setWidthPercentage(100);
        headerTable.setWidths(new float[]{65, 35});

        PdfPCell leftCell = new PdfPCell();
        leftCell.setBorder(Rectangle.NO_BORDER);
        leftCell.addElement(new Paragraph(SCHOOL_NAME, titleFont));
        leftCell.addElement(new Paragraph("NIT: " + SCHOOL_NIT + " - Régimen Común", boldFont));
        leftCell.addElement(new Paragraph(SCHOOL_ADDRESS, subTitleFont));
        leftCell.addElement(new Paragraph("Tel: " + SCHOOL_PHONE + " | " + SCHOOL_EMAIL, subTitleFont));
        leftCell.addElement(new Paragraph(DIAN_RESOLUTION, subTitleFont));
        headerTable.addCell(leftCell);

        PdfPCell rightCell = new PdfPCell();
        rightCell.setBorder(Rectangle.BOX);
        rightCell.setBackgroundColor(new Color(245, 247, 250));
        rightCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        rightCell.addElement(new Paragraph("FACTURA ELECTRÓNICA DE VENTA", boldFont));
        rightCell.addElement(new Paragraph(invoiceNumber, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.RED)));
        rightCell.addElement(new Paragraph("Fecha de Emisión: " + issueDate.format(DateTimeFormatter.ISO_LOCAL_DATE), normalFont));
        rightCell.addElement(new Paragraph("Recibo No: " + (payment.getReceiptNumber() != null ? payment.getReceiptNumber() : "REC-2024-001"), subTitleFont));
        rightCell.addElement(new Paragraph("Método: " + (payment.getPaymentMethod() != null ? payment.getPaymentMethod() : "Transferencia"), subTitleFont));
        headerTable.addCell(rightCell);

        document.add(headerTable);
        document.add(new Paragraph(" "));

        PdfPTable clientTable = new PdfPTable(2);
        clientTable.setWidthPercentage(100);
        clientTable.setWidths(new float[]{50, 50});

        PdfPCell c1 = new PdfPCell();
        c1.setBackgroundColor(new Color(250, 250, 250));
        c1.addElement(new Paragraph("DATOS DEL ADQUIRENTE / ACUDIENTE", boldFont));
        c1.addElement(new Paragraph("Nombre: " + (guardian.getFullName() != null ? guardian.getFullName() : "Acudiente General"), normalFont));
        c1.addElement(new Paragraph("Documento: " + (guardian.getDocumentType() != null ? guardian.getDocumentType() : "CC") + " " + (guardian.getDocumentNumber() != null ? guardian.getDocumentNumber() : "N/A"), normalFont));
        c1.addElement(new Paragraph("Teléfono: " + (guardian.getMainPhone() != null ? guardian.getMainPhone() : "N/A"), normalFont));
        c1.addElement(new Paragraph("Dirección: " + (guardian.getAddress() != null ? guardian.getAddress() : "Bogotá D.C."), normalFont));
        clientTable.addCell(c1);

        PdfPCell c2 = new PdfPCell();
        c2.setBackgroundColor(new Color(250, 250, 250));
        c2.addElement(new Paragraph("DATOS DEL BENEFICIARIO (ESTUDIANTE)", boldFont));
        c2.addElement(new Paragraph("Estudiante: " + (student.getFullName() != null ? student.getFullName() : "Estudiante Little Horizons"), normalFont));
        c2.addElement(new Paragraph("Documento: " + (student.getDocumentType() != null ? student.getDocumentType() : "TI") + " " + (student.getDocumentNumber() != null ? student.getDocumentNumber() : "N/A"), normalFont));
        c2.addElement(new Paragraph("Año Académico: " + (payment.getAcademicYear() != null ? payment.getAcademicYear() : 2024), normalFont));
        c2.addElement(new Paragraph("Período: " + (payment.getAcademicPeriod() != null ? payment.getAcademicPeriod() : "I"), normalFont));
        clientTable.addCell(c2);

        document.add(clientTable);
        document.add(new Paragraph(" "));

        PdfPTable itemsTable = new PdfPTable(5);
        itemsTable.setWidthPercentage(100);
        itemsTable.setWidths(new float[]{10, 45, 15, 15, 15});

        String[] headers = {"Ítem", "Descripción del Concepto", "Cant.", "Valor Unit.", "Subtotal"};
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE)));
            cell.setBackgroundColor(new Color(33, 60, 115));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            itemsTable.addCell(cell);
        }

        BigDecimal amt = payment.getAmountPaid() != null ? payment.getAmountPaid() : BigDecimal.ZERO;
        itemsTable.addCell(new Phrase("1", normalFont));
        itemsTable.addCell(new Phrase(conceptName + (payment.getNotes() != null ? " (" + payment.getNotes() + ")" : ""), normalFont));
        itemsTable.addCell(new Phrase("1", normalFont));
        itemsTable.addCell(new Phrase(String.format("COP $%,.2f", amt), normalFont));
        itemsTable.addCell(new Phrase(String.format("COP $%,.2f", amt), normalFont));

        PdfPCell empty = new PdfPCell(new Phrase(""));
        empty.setColspan(3);
        empty.setBorder(Rectangle.NO_BORDER);

        itemsTable.addCell(empty);
        PdfPCell subCell = new PdfPCell(new Phrase("Subtotal:", boldFont));
        subCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        itemsTable.addCell(subCell);
        itemsTable.addCell(new Phrase(String.format("COP $%,.2f", amt), normalFont));

        itemsTable.addCell(empty);
        PdfPCell ivaCell = new PdfPCell(new Phrase("IVA (0% Exento):", boldFont));
        ivaCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        itemsTable.addCell(ivaCell);
        itemsTable.addCell(new Phrase("COP $0.00", normalFont));

        itemsTable.addCell(empty);
        PdfPCell totalCell = new PdfPCell(new Phrase("TOTAL A PAGAR:", boldFont));
        totalCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totalCell.setBackgroundColor(new Color(230, 240, 255));
        itemsTable.addCell(totalCell);
        PdfPCell totalValCell = new PdfPCell(new Phrase(String.format("COP $%,.2f", amt), boldFont));
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
        cufeCell.addElement(new Paragraph("Documento tributario emitido según Decreto 2242 de 2015 y Resolución DIAN 000042 de 2020. Exento de IVA conforme al Artículo 476 del Estatuto Tributario para servicios educativos.", subTitleFont));
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
