package com.littlehorizons.erp.view;

import com.littlehorizons.erp.model.academic.Student;
import com.littlehorizons.erp.model.finance.Invoice;
import com.littlehorizons.erp.model.finance.Payment;
import com.littlehorizons.erp.model.finance.PaymentConcept;

import java.math.BigDecimal;
import java.util.List;

public class FinanceConsoleView implements IFinanceView {

    @Override
    public void showMenu() {
        ConsoleHelper.printHeader("GESTIÓN FINANCIERA Y FACTURACIÓN ELECTRÓNICA", "Cumplimiento Estatuto Tributario y Estándar MUISCA DIAN");
        System.out.println("1. Registrar pago y emitir Factura Electrónica (PDF, TXT, XML - E.T. / DIAN)");
        System.out.println("2. Consultar catálogo de conceptos de cobro (Marco Fiscal E.T.)");
        System.out.println("3. Consultar historial de pagos por estudiante");
        System.out.println("4. Consultar historial de pagos por acudiente");
        System.out.println("5. Listar facturas electrónicas generadas (Desglose Tributario)");
        System.out.println("6. Visualizar factura en texto plano (Legible para Agentes / Consola)");
        System.out.println("7. Reporte de estudiantes morosos (Pensión pendiente)");
        System.out.println("0. Regresar al Menú Principal");
        System.out.println();
    }

    @Override
    public void showProcessPaymentHeader() {
        ConsoleHelper.printSubHeader("REGISTRO DE PAGO Y FACTURA ELECTRÓNICA DIAN (PDF / TXT / XML)");
    }

    @Override
    public void showAvailableConcepts(List<PaymentConcept> concepts) {
        System.out.println("\nConceptos de pago disponibles:");
        for (PaymentConcept c : concepts) {
            System.out.printf("  [%d] %-25s - Base: COP $%,.2f | %s%n",
                    c.getIdConcept(), c.getConceptName(), c.getBaseAmount(), c.getTaxClassification());
        }
    }

    @Override
    public void showBaseAmount(BigDecimal baseAmount) {
        System.out.printf("El valor base es: COP $%,.2f%n", baseAmount);
    }

    @Override
    public void showPaymentMethods() {
        System.out.println("Métodos de pago: 1. Efectivo | 2. Transferencia | 3. Tarjeta Débito | 4. Tarjeta Crédito");
    }

    @Override
    public void showInvoiceSuccess(Invoice inv, BigDecimal amountPaid) {
        ConsoleHelper.printSuccess("¡PAGO CONFIRMADO Y FACTURA MULTI-FORMATO GENERADA CON ÉXITO!");
        System.out.println();
        System.out.println("  ═════════════════════════════════════════════════════════════════════════");
        System.out.printf("  Consecutivo Factura   : %s%n", inv.getInvoiceNumber());
        System.out.printf("  Fecha de Emisión      : %s%n", inv.getIssueDate());
        System.out.printf("  Base Imponible        : COP $%,.2f%n", inv.getSubtotalAmount() != null ? inv.getSubtotalAmount() : amountPaid);
        System.out.printf("  IVA Liquidado         : COP $%,.2f%n", inv.getTaxAmount() != null ? inv.getTaxAmount() : BigDecimal.ZERO);
        System.out.printf("  Total Facturado       : COP $%,.2f%n", amountPaid);
        System.out.printf("  Marco Tributario E.T. : %s%n", inv.getTaxLegalReference() != null ? inv.getTaxLegalReference() : "Excluido Art. 476 E.T.");
        System.out.printf("  CUFE (DIAN SHA-256)   : %s%n", inv.getCufe());
        System.out.println("  -------------------------------------------------------------------------");
        System.out.printf("  [1] Archivo PDF       : %s%n", inv.getFilePath());
        System.out.printf("  [2] Archivo TXT Plano : %s (Ideal para lectura directa de agentes)%n", inv.getTxtFilePath());
        System.out.printf("  [3] Archivo XML MUISCA: %s (UBL 2.1 estándar DIAN)%n", inv.getXmlFilePath());
        System.out.println("  ═════════════════════════════════════════════════════════════════════════");
    }

    @Override
    public void showConceptsCatalog(List<PaymentConcept> concepts) {
        ConsoleHelper.printSubHeader("CATÁLOGO DE CONCEPTOS DE PAGO Y TRATAMIENTO TRIBUTARIO");
        System.out.printf("%-4s | %-10s | %-28s | %-16s | %-10s | %-32s%n",
                "ID", "CÓDIGO", "NOMBRE DEL CONCEPTO", "VALOR BASE", "TARIFA IVA", "MARCO ESTATUTO TRIBUTARIO");
        System.out.println("─".repeat(115));
        for (PaymentConcept c : concepts) {
            String ivaStr = Boolean.TRUE.equals(c.getIsTaxExempt()) ? "0% (Excluido)" : "19% (Gravado)";
            String marcoStr = Boolean.TRUE.equals(c.getIsTaxExempt()) ? "Art. 476 Num. 6 E.T. (Educación)" : "Art. 468 E.T. (General)";
            System.out.printf("%-4d | %-10s | %-28s | COP $%10.2f | %-10s | %-32s%n",
                    c.getIdConcept(), c.getConceptCode(), c.getConceptName(), c.getBaseAmount(),
                    ivaStr, marcoStr);
        }
        System.out.println("─".repeat(115));
    }

    @Override
    public void showStudentPaymentHistory(List<Payment> payments) {
        ConsoleHelper.printSubHeader("HISTORIAL DE PAGOS DEL ESTUDIANTE");
        if (payments.isEmpty()) {
            ConsoleHelper.printWarning("No se registran pagos para este estudiante.");
        } else {
            System.out.printf("%-5s | %-16s | %-12s | %-25s | %-16s | %-15s%n", "ID", "RECIBO", "FECHA", "CONCEPTO", "MONTO", "MÉTODO");
            System.out.println("─".repeat(95));
            for (Payment p : payments) {
                System.out.printf("%-5d | %-16s | %-12s | %-25s | COP $%,10.2f | %-15s%n",
                        p.getIdPago(), p.getReceiptNumber(), p.getPaymentDate(),
                        p.getConceptName(), p.getAmountPaid(), p.getPaymentMethod());
            }
            System.out.println("─".repeat(95));
        }
    }

    @Override
    public void showGuardianPaymentHistory(List<Payment> payments) {
        ConsoleHelper.printSubHeader("HISTORIAL DE PAGOS POR ACUDIENTE");
        if (payments.isEmpty()) {
            ConsoleHelper.printWarning("No se registran pagos para este acudiente.");
        } else {
            System.out.printf("%-5s | %-16s | %-25s | %-20s | %-16s%n", "ID", "RECIBO", "ESTUDIANTE", "CONCEPTO", "MONTO");
            System.out.println("─".repeat(90));
            for (Payment p : payments) {
                System.out.printf("%-5d | %-16s | %-25s | %-20s | COP $%,10.2f%n",
                        p.getIdPago(), p.getReceiptNumber(), p.getStudentName(),
                        p.getConceptName(), p.getAmountPaid());
            }
            System.out.println("─".repeat(90));
        }
    }

    @Override
    public void showInvoices(List<Invoice> invoices) {
        ConsoleHelper.printSubHeader("REGISTRO DE FACTURAS ELECTRÓNICAS EMITIDAS (MULTI-FORMATO)");
        if (invoices.isEmpty()) {
            ConsoleHelper.printWarning("No se han emitido facturas electrónicas.");
        } else {
            System.out.printf("%-4s | %-16s | %-11s | %-22s | %-15s | %-12s | %-15s | %-18s%n",
                    "ID", "Nº FACTURA", "FECHA", "ACUDIENTE", "BASE IMPONIBLE", "IVA LIQUIDADO", "TOTAL COP", "FORMATOS");
            System.out.println("─".repeat(125));
            for (Invoice inv : invoices) {
                BigDecimal base = inv.getSubtotalAmount() != null ? inv.getSubtotalAmount() : inv.getAmount();
                BigDecimal tax = inv.getTaxAmount() != null ? inv.getTaxAmount() : BigDecimal.ZERO;
                BigDecimal total = inv.getAmount() != null ? inv.getAmount() : BigDecimal.ZERO;

                System.out.printf("%-4d | %-16s | %-11s | %-22s | COP $%9.2f | COP $%7.2f | COP $%9.2f | PDF / TXT / XML%n",
                        inv.getIdInvoice(), inv.getInvoiceNumber(), inv.getIssueDate(),
                        inv.getGuardianName() != null ? inv.getGuardianName() : "N/A",
                        base, tax, total);
            }
            System.out.println("─".repeat(125));
            System.out.println("Total facturas electrónicas registradas: " + invoices.size());
            System.out.println("Nota: Para inspeccionar el contenido completo use la Opción 6 del menú.");
        }
    }

    @Override
    public void showPlainTextInvoiceHeader() {
        ConsoleHelper.printSubHeader("VISUALIZACIÓN DE FACTURA EN TEXTO PLANO / XML (LEGIBLE PARA AGENTES)");
        System.out.println("Puede ingresar el número de factura (ej: FE-LH-2024-0001) o el ID numérico de la factura.");
    }

    @Override
    public void showRecentInvoices(List<Invoice> invoices) {
        int limit = Math.min(invoices.size(), 10);
        System.out.println("\nÚltimas facturas registradas:");
        for (int i = 0; i < limit; i++) {
            Invoice in = invoices.get(i);
            System.out.printf("  [%d] Consecutivo: %s | Acudiente: %s | Total: COP $%,.2f%n",
                    in.getIdInvoice(), in.getInvoiceNumber(), in.getGuardianName(), in.getAmount());
        }
    }

    @Override
    public void showAvailableInvoiceFormats(String invoiceNumber) {
        System.out.println("\nFormatos disponibles para " + invoiceNumber + ":");
        System.out.println("1. Visualizar Formato TXT Estructurado (Estatuto Tributario Art. 617 / Consola / Agentes)");
        System.out.println("2. Visualizar Formato XML UBL 2.1 Estándar MUISCA / DIAN");
        System.out.println("3. Ver ambos formatos");
    }

    @Override
    public void showInvoiceTextContent(String content) {
        System.out.println("\n" + "=".repeat(88));
        System.out.println("               VISUALIZACIÓN EN TEXTO PLANO (TXT) - INVOICE AGENT VIEW");
        System.out.println("=".repeat(88));
        System.out.println(content);
    }

    @Override
    public void showInvoiceXmlContent(String content) {
        System.out.println("\n" + "=".repeat(88));
        System.out.println("               VISUALIZACIÓN EN XML UBL 2.1 (MUISCA / DIAN) - INVOICE XML VIEW");
        System.out.println("=".repeat(88));
        System.out.println(content);
    }

    @Override
    public void showDefaultersHeader() {
        ConsoleHelper.printSubHeader("REPORTE DE ESTUDIANTES CON PAGOS PENDIENTES (CARTERA MOROSA)");
        System.out.println("Identificación de estudiantes que NO han pagado la pensión escolar.");
    }

    @Override
    public void showDefaultersReport(String period, int year, List<Student> defaulters) {
        if (defaulters.isEmpty()) {
            ConsoleHelper.printSuccess("¡Al día! No se encontraron estudiantes con mora para el período consultado.");
        } else {
            System.out.println("\nEstudiantes con Pensión pendiente para el período " + period + " - " + year + ":");
            System.out.printf("%-6s | %-14s | %-32s | %-15s | %-10s%n", "ID", "DOCUMENTO", "NOMBRE COMPLETO", "TELÉFONO", "ESTADO");
            System.out.println("─".repeat(85));
            for (Student s : defaulters) {
                System.out.printf("%-6d | %-4s %-9s | %-32s | %-15s | %-10s%n",
                        s.getIdStudent(), s.getDocumentType(), s.getDocumentNumber(),
                        s.getFullName(), s.getPhone(), s.getStatus());
            }
            System.out.println("─".repeat(85));
            System.out.println("Total estudiantes en mora: " + defaulters.size());
        }
    }

    @Override
    public void showSuccess(String message) {
        ConsoleHelper.printSuccess(message);
    }

    @Override
    public void showError(String message) {
        ConsoleHelper.printError(message);
    }

    @Override
    public void showWarning(String message) {
        ConsoleHelper.printWarning(message);
    }
}
