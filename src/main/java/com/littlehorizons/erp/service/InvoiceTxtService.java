package com.littlehorizons.erp.service;

import com.littlehorizons.erp.model.academic.Guardian;
import com.littlehorizons.erp.model.academic.Student;
import com.littlehorizons.erp.model.finance.Invoice;
import com.littlehorizons.erp.model.finance.Payment;
import com.littlehorizons.erp.model.finance.PaymentConcept;
import com.littlehorizons.erp.util.LoggerUtil;
import com.littlehorizons.erp.util.TaxCalculator;
import com.littlehorizons.erp.util.TaxCalculator.TaxBreakdown;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class InvoiceTxtService {

    private static final String INVOICES_DIR = "invoices";
    private static final String SCHOOL_NAME = "COLEGIO INTERNACIONAL LITTLE HORIZONS S.A.S.";
    private static final String SCHOOL_NIT = "900.543.210-9";
    private static final String SCHOOL_ADDRESS = "Calle 127 # 45-12, Bogotá D.C., Colombia";
    private static final String SCHOOL_PHONE = "+57 (601) 745-8900";
    private static final String SCHOOL_EMAIL = "facturacion@littlehorizons.edu.co";
    private static final String DIAN_RESOLUTION = "Resolución DIAN No. 18764000129845 de 2024-01-10 (Rango LH-0001 al LH-9999)";

    public InvoiceTxtService() {
        ensureDirectoryExists();
    }

    private void ensureDirectoryExists() {
        File dir = new File(INVOICES_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    /**
     * Genera la representación en texto plano estructurado de la factura electrónica,
     * altamente legible para agentes de IA, terminales de consola y sistemas automatizados.
     */
    public String generateTxtContent(String invoiceNumber, LocalDate issueDate, String cufe,
                                     Student student, Guardian guardian, PaymentConcept concept,
                                     Payment payment, TaxBreakdown tax) {

        DecimalFormat df = new DecimalFormat("#,##0.00");
        DateTimeFormatter dtf = DateTimeFormatter.ISO_LOCAL_DATE;

        String studentDoc = (student != null && student.getDocumentNumber() != null)
                ? student.getDocumentType() + " " + student.getDocumentNumber()
                : "TI N/A";
        String studentName = (student != null && student.getFullName() != null)
                ? student.getFullName()
                : "Estudiante";

        String guardianDoc = (guardian != null && guardian.getDocumentNumber() != null)
                ? guardian.getDocumentType() + " " + guardian.getDocumentNumber()
                : "CC 222222222222";
        String guardianName = (guardian != null && guardian.getFullName() != null)
                ? guardian.getFullName()
                : "Consumidor Final";
        String guardianAddr = (guardian != null && guardian.getAddress() != null)
                ? guardian.getAddress()
                : "Bogotá D.C., Colombia";

        String conceptCode = (concept != null && concept.getConceptCode() != null) ? concept.getConceptCode() : "SRV-01";
        String conceptName = (concept != null && concept.getConceptName() != null) ? concept.getConceptName() : "Servicio Escolar";
        String receiptNum = (payment != null && payment.getReceiptNumber() != null) ? payment.getReceiptNumber() : "REC-001";
        String payMethod = (payment != null && payment.getPaymentMethod() != null) ? payment.getPaymentMethod() : "Transferencia";
        String period = (payment != null && payment.getAcademicPeriod() != null) ? payment.getAcademicPeriod() : "I";
        int year = (payment != null && payment.getAcademicYear() != null) ? payment.getAcademicYear() : LocalDate.now().getYear();

        StringBuilder sb = new StringBuilder();
        sb.append("========================================================================================\n");
        sb.append("                           FACTURA ELECTRÓNICA DE VENTA                                 \n");
        sb.append("                 SISTEMA MUNICIPAL Y NACIONAL DIAN - ESTÁNDAR MUISCA                    \n");
        sb.append("========================================================================================\n\n");

        sb.append("[1. DATOS DEL EMISOR O FACTURADOR ELECTRÓNICO - ART. 617 LIT. A, B, D]\n");
        sb.append(String.format("  Razón Social      : %s\n", SCHOOL_NAME));
        sb.append(String.format("  NIT               : %s (Régimen Común / Responsable de IVA)\n", SCHOOL_NIT));
        sb.append(String.format("  Dirección         : %s\n", SCHOOL_ADDRESS));
        sb.append(String.format("  Contacto          : Tel: %s | Email: %s\n", SCHOOL_PHONE, SCHOOL_EMAIL));
        sb.append(String.format("  Autorización DIAN : %s\n", DIAN_RESOLUTION));
        sb.append(String.format("  Actividad Económica: Código CIIU 8521 - Educación Media Académica\n\n"));

        sb.append("[2. DATOS DEL COMPROBANTE - ART. 617 LIT. C, F]\n");
        sb.append(String.format("  Número de Factura : %s\n", invoiceNumber));
        sb.append(String.format("  Fecha de Emisión  : %s\n", issueDate.format(dtf)));
        sb.append(String.format("  Fecha Vencimiento : %s\n", issueDate.plusDays(30).format(dtf)));
        sb.append(String.format("  Recibo de Caja Nº : %s\n", receiptNum));
        sb.append(String.format("  Medio de Pago     : %s\n", payMethod));
        sb.append(String.format("  Año / Período     : %d - Período %s\n\n", year, period));

        sb.append("[3. DATOS DEL ADQUIRENTE / CLIENTE - ART. 617 LIT. E]\n");
        sb.append(String.format("  Acudiente / Pagador: %s\n", guardianName));
        sb.append(String.format("  Documento Identidad: %s\n", guardianDoc));
        sb.append(String.format("  Dirección          : %s\n", guardianAddr));
        sb.append(String.format("  Estudiante Benefic.: %s (%s)\n\n", studentName, studentDoc));

        sb.append("[4. DETALLE DE BIENES Y SERVICIOS FACTURADOS]\n");
        sb.append(String.format("  %-10s | %-32s | %-6s | %-15s | %-15s\n", "CÓDIGO", "DESCRIPCIÓN DEL CONCEPTO", "CANT.", "VALOR UNIT.", "SUBTOTAL BASE"));
        sb.append("  " + "-".repeat(84) + "\n");
        sb.append(String.format("  %-10s | %-32s | %-6d | COP $%11s | COP $%11s\n",
                conceptCode, conceptName, 1, df.format(tax.getBaseAmount()), df.format(tax.getBaseAmount())));
        sb.append("  " + "-".repeat(84) + "\n\n");

        sb.append("[5. DISCRIMINACIÓN TRIBUTARIA - ESTATUTO TRIBUTARIO ART. 476 Y ART. 617]\n");
        sb.append(String.format("  Subtotal Base Gravable       : COP $%15s\n", df.format(tax.getBaseAmount())));
        sb.append(String.format("  Tarifa Impuesto a las Ventas : %s%%\n", tax.getTaxRate().multiply(new java.math.BigDecimal("100")).stripTrailingZeros().toPlainString()));
        sb.append(String.format("  Valor IVA Liquidado          : COP $%15s\n", df.format(tax.getTaxAmount())));
        sb.append(String.format("  TOTAL A PAGAR                : COP $%15s\n", df.format(tax.getTotalAmount())));
        sb.append(String.format("  Marco Legal E.T. Aplicable   : %s\n", tax.getLegalReference()));
        sb.append(String.format("  Clasificación MUISCA         : Categoría Tributaria DIAN [%s - %s]\n\n",
                tax.getDianTaxSchemeCode(), tax.getDianTaxSchemeName()));

        sb.append("[6. VALIDACIÓN TÉCNICA Y FIRMA DIAN]\n");
        sb.append(String.format("  CUFE (Código Único Factura)  : %s\n", cufe));
        sb.append(String.format("  Algoritmo de Hashing         : SHA-256 (MUISCA compliant)\n"));
        sb.append(String.format("  Firma Digital                : XAdES-EPES LittleHorizons-Certificate-2024-Verified\n"));
        sb.append(String.format("  Consulta Pública DIAN (QR)   : https://catalogo-vpfe.dian.gov.co/document/searchqr?documentkey=%s\n", cufe));
        sb.append("========================================================================================\n");
        sb.append("                   FIN DE DOCUMENTO - EXPEDIDO POR COMPUTADOR                           \n");
        sb.append("========================================================================================\n");

        return sb.toString();
    }

    /**
     * Escribe el archivo .txt en el sistema de archivos.
     */
    public boolean writeTxtFile(String filePath, String content) {
        try {
            ensureDirectoryExists();
            File file = new File(filePath);
            try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
                writer.write(content);
            }
            LoggerUtil.info("Generated TXT invoice successfully: " + filePath);
            return true;
        } catch (Exception e) {
            LoggerUtil.error("Failed to write TXT invoice file: " + filePath, e);
            return false;
        }
    }
}
