package com.littlehorizons.erp.controller;

import com.littlehorizons.erp.model.academic.Student;
import com.littlehorizons.erp.model.finance.Invoice;
import com.littlehorizons.erp.model.finance.Payment;
import com.littlehorizons.erp.model.finance.PaymentConcept;
import com.littlehorizons.erp.service.FinanceService;
import com.littlehorizons.erp.util.ConsoleInputReader;
import com.littlehorizons.erp.view.ConsoleHelper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Controller for Financial Management, Payments, and DIAN Electronic Invoices.
 */
public class FinanceController {

    private final FinanceService financeService;

    public FinanceController(FinanceService financeService) {
        this.financeService = financeService;
    }

    public void run() {
        boolean back = false;
        while (!back) {
            ConsoleHelper.printHeader("GESTIÓN FINANCIERA Y FACTURACIÓN ELECTRÓNICA", "Colegio Little Horizons - Cumplimiento DIAN");
            System.out.println("1. Registrar pago y emitir Factura Electrónica PDF (DIAN / CUFE / QR)");
            System.out.println("2. Consultar catálogo de conceptos de cobro");
            System.out.println("3. Consultar historial de pagos por estudiante");
            System.out.println("4. Consultar historial de pagos por acudiente");
            System.out.println("5. Listar facturas electrónicas generadas");
            System.out.println("6. Reporte de estudiantes morosos (Pensión pendiente)");
            System.out.println("0. Regresar al Menú Principal");
            System.out.println();

            int option = ConsoleInputReader.readInt("Seleccione una opción: ", 0, 6);
            switch (option) {
                case 1 -> processPaymentAndInvoice();
                case 2 -> listConcepts();
                case 3 -> viewStudentPaymentHistory();
                case 4 -> viewGuardianPaymentHistory();
                case 5 -> listInvoices();
                case 6 -> showDefaultersReport();
                case 0 -> back = true;
            }
        }
    }

    private void processPaymentAndInvoice() {
        ConsoleHelper.printSubHeader("REGISTRO DE PAGO Y FACTURA ELECTRÓNICA DIAN");
        int studentId = ConsoleInputReader.readInt("ID del estudiante: ", 1, 99999);
        int guardianId = ConsoleInputReader.readInt("ID del acudiente responsable del pago: ", 1, 99999);

        System.out.println("\nConceptos de pago disponibles:");
        List<PaymentConcept> concepts = financeService.getAllConcepts();
        for (PaymentConcept c : concepts) {
            System.out.printf("  [%d] %-25s - Base: COP $%,.2f%n", c.getIdConcept(), c.getConceptName(), c.getBaseAmount());
        }

        int conceptId = ConsoleInputReader.readInt("Seleccione el ID del concepto: ", 1, concepts.size());
        Optional<PaymentConcept> concOpt = financeService.getConceptById(conceptId);
        BigDecimal defaultAmount = concOpt.map(PaymentConcept::getBaseAmount).orElse(new BigDecimal("350000.00"));

        System.out.printf("El valor base es: COP $%,.2f%n", defaultAmount);
        boolean custom = ConsoleInputReader.readBoolean("¿Desea aplicar un valor personalizado (ej: descuento o beca)?");
        BigDecimal amountPaid = defaultAmount;
        if (custom) {
            amountPaid = ConsoleInputReader.readBigDecimal("Ingrese el monto acordado a pagar: ",
                    BigDecimal.ONE, new BigDecimal("100000000.00"));
        }

        System.out.println("Métodos de pago: 1. Efectivo | 2. Transferencia | 3. Tarjeta Débito | 4. Tarjeta Crédito");
        int mOpt = ConsoleInputReader.readInt("Seleccione método (1-4): ", 1, 4);
        String method = switch (mOpt) {
            case 1 -> "Efectivo";
            case 2 -> "Transferencia";
            case 3 -> "Tarjeta Débito";
            default -> "Tarjeta Crédito";
        };

        int year = ConsoleInputReader.readInt("Año lectivo (ej: 2024): ", 2020, 2030);
        String period = ConsoleInputReader.readString("Período (I / II / III / IV / Anual): ").toUpperCase();
        String notes = ConsoleInputReader.readOptionalString("Notas adicionales en factura: ");

        Payment payment = new Payment(null, studentId, guardianId, conceptId, amountPaid,
                LocalDate.now(), method, null, year, period, notes);

        try {
            Optional<Invoice> invoiceOpt = financeService.processPaymentAndInvoice(payment);
            if (invoiceOpt.isPresent()) {
                Invoice inv = invoiceOpt.get();
                ConsoleHelper.printSuccess("¡PAGO CONFIRMADO Y FACTURA ELECTRÓNICA GENERADA CON ÉXITO!");
                System.out.println();
                System.out.println("  ═════════════════════════════════════════════════════════════");
                System.out.printf("  Consecutivo Factura : %s%n", inv.getInvoiceNumber());
                System.out.printf("  Fecha de Emisión    : %s%n", inv.getIssueDate());
                System.out.printf("  Monto Pagado        : COP $%,.2f%n", amountPaid);
                System.out.printf("  CUFE (DIAN Simulado): %s%n", inv.getCufe());
                System.out.printf("  Archivo PDF Creado  : %s%n", inv.getFilePath());
                System.out.println("  ═════════════════════════════════════════════════════════════");
                System.out.println("  Nota: El PDF generado incluye la firma digital simulada y código QR.");
            } else {
                ConsoleHelper.printError("No se pudo procesar el pago o generar la factura.");
            }
        } catch (Exception e) {
            ConsoleHelper.printError("Error en procesamiento financiero: " + e.getMessage());
        }
        ConsoleInputReader.waitForEnter();
    }

    private void listConcepts() {
        ConsoleHelper.printSubHeader("CATÁLOGO DE CONCEPTOS DE PAGO");
        List<PaymentConcept> concepts = financeService.getAllConcepts();
        System.out.printf("%-4s | %-12s | %-30s | %-18s | %-10s%n", "ID", "CÓDIGO", "NOMBRE DEL CONCEPTO", "VALOR BASE", "RECURRENTE");
        System.out.println("─".repeat(85));
        for (PaymentConcept c : concepts) {
            System.out.printf("%-4d | %-12s | %-30s | COP $%,12.2f | %-10s%n",
                    c.getIdConcept(), c.getConceptCode(), c.getConceptName(), c.getBaseAmount(),
                    c.getIsRecurring() ? "Sí (Mensual)" : "No (Único)");
        }
        System.out.println("─".repeat(85));
        ConsoleInputReader.waitForEnter();
    }

    private void viewStudentPaymentHistory() {
        ConsoleHelper.printSubHeader("HISTORIAL DE PAGOS DEL ESTUDIANTE");
        int studentId = ConsoleInputReader.readInt("Ingrese el ID del estudiante: ", 1, 99999);
        List<Payment> payments = financeService.getPaymentsByStudent(studentId);

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
        ConsoleInputReader.waitForEnter();
    }

    private void viewGuardianPaymentHistory() {
        ConsoleHelper.printSubHeader("HISTORIAL DE PAGOS POR ACUDIENTE");
        int guardianId = ConsoleInputReader.readInt("Ingrese el ID del acudiente: ", 1, 99999);
        List<Payment> payments = financeService.getPaymentsByGuardian(guardianId);

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
        ConsoleInputReader.waitForEnter();
    }

    private void listInvoices() {
        ConsoleHelper.printSubHeader("REGISTRO DE FACTURAS ELECTRÓNICAS EMITIDAS");
        List<Invoice> invoices = financeService.getAllInvoices();
        if (invoices.isEmpty()) {
            ConsoleHelper.printWarning("No se han emitido facturas electrónicas.");
        } else {
            System.out.printf("%-4s | %-16s | %-11s | %-25s | %-16s | %-20s%n", "ID", "Nº FACTURA", "FECHA", "ACUDIENTE", "VALOR TOTAL", "ARCHIVO PDF");
            System.out.println("─".repeat(105));
            for (Invoice inv : invoices) {
                System.out.printf("%-4d | %-16s | %-11s | %-25s | COP $%,10.2f | %-20s%n",
                        inv.getIdInvoice(), inv.getInvoiceNumber(), inv.getIssueDate(),
                        inv.getGuardianName() != null ? inv.getGuardianName() : "N/A",
                        inv.getAmount() != null ? inv.getAmount() : BigDecimal.ZERO,
                        inv.getPdfFileName());
            }
            System.out.println("─".repeat(105));
            System.out.println("Total facturas electrónicas registradas: " + invoices.size());
        }
        ConsoleInputReader.waitForEnter();
    }

    private void showDefaultersReport() {
        ConsoleHelper.printSubHeader("REPORTE DE ESTUDIANTES CON PAGOS PENDIENTES (CARTERA MOROSA)");
        System.out.println("Identificación de estudiantes que NO han pagado la pensión escolar.");
        int year = ConsoleInputReader.readInt("Año lectivo: ", 2020, 2030);
        String period = ConsoleInputReader.readString("Período (I / II / III / IV): ").toUpperCase();

        // 2 is Pensión Mensual in standard concepts
        List<Student> defaulters = financeService.getDefaulterStudents(2, year, period);

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
        ConsoleInputReader.waitForEnter();
    }
}
