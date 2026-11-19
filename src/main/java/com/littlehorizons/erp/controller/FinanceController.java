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

public class FinanceController {

    private final FinanceService financeService;

    public FinanceController(FinanceService financeService) {
        this.financeService = financeService;
    }

    public void run() {
        boolean back = false;
        while (!back) {
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

            int option = ConsoleInputReader.readInt("Seleccione una opción: ", 0, 7);
            switch (option) {
                case 1 -> processPaymentAndInvoice();
                case 2 -> listConcepts();
                case 3 -> viewStudentPaymentHistory();
                case 4 -> viewGuardianPaymentHistory();
                case 5 -> listInvoices();
                case 6 -> viewInvoicePlainText();
                case 7 -> showDefaultersReport();
                case 0 -> back = true;
            }
        }
    }

    private void processPaymentAndInvoice() {
        ConsoleHelper.printSubHeader("REGISTRO DE PAGO Y FACTURA ELECTRÓNICA DIAN (PDF / TXT / XML)");
        int studentId = ConsoleInputReader.readInt("ID del estudiante: ", 1, 99999);
        int guardianId = ConsoleInputReader.readInt("ID del acudiente responsable del pago: ", 1, 99999);

        System.out.println("\nConceptos de pago disponibles:");
        List<PaymentConcept> concepts = financeService.getAllConcepts();
        for (PaymentConcept c : concepts) {
            System.out.printf("  [%d] %-25s - Base: COP $%,.2f | %s%n",
                    c.getIdConcept(), c.getConceptName(), c.getBaseAmount(), c.getTaxClassification());
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
        String period = ConsoleInputReader.readAcademicPeriod("Período (I / II / III / IV / Anual): ", false, true);
        String notes = ConsoleInputReader.readOptionalString("Notas adicionales en factura: ");

        Payment payment = new Payment(null, studentId, guardianId, conceptId, amountPaid,
                LocalDate.now(), method, null, year, period, notes);

        try {
            Optional<Invoice> invoiceOpt = financeService.processPaymentAndInvoice(payment);
            if (invoiceOpt.isPresent()) {
                Invoice inv = invoiceOpt.get();
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
            } else {
                ConsoleHelper.printError("No se pudo procesar el pago o generar la factura.");
            }
        } catch (IllegalArgumentException e) {
            ConsoleHelper.printError("Validación de pago fallida: " + e.getMessage());
        } catch (IllegalStateException e) {
            ConsoleHelper.printError(e.getMessage());
        } catch (Exception e) {
            ConsoleHelper.printError("Error en procesamiento financiero: " + e.getMessage());
        }
        ConsoleInputReader.waitForEnter();
    }

    private void listConcepts() {
        ConsoleHelper.printSubHeader("CATÁLOGO DE CONCEPTOS DE PAGO Y TRATAMIENTO TRIBUTARIO");
        List<PaymentConcept> concepts = financeService.getAllConcepts();
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
        ConsoleHelper.printSubHeader("REGISTRO DE FACTURAS ELECTRÓNICAS EMITIDAS (MULTI-FORMATO)");
        List<Invoice> invoices = financeService.getAllInvoices();
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
        ConsoleInputReader.waitForEnter();
    }

    private void viewInvoicePlainText() {
        ConsoleHelper.printSubHeader("VISUALIZACIÓN DE FACTURA EN TEXTO PLANO / XML (LEGIBLE PARA AGENTES)");
        System.out.println("Puede ingresar el número de factura (ej: FE-LH-2024-0001) o el ID numérico de la factura.");
        String input = ConsoleInputReader.readString("Ingrese el número o ID de factura (o '?' para listar las últimas): ").trim();

        if (input.equals("?")) {
            List<Invoice> list = financeService.getAllInvoices();
            int limit = Math.min(list.size(), 10);
            System.out.println("\nÚltimas facturas registradas:");
            for (int i = 0; i < limit; i++) {
                Invoice in = list.get(i);
                System.out.printf("  [%d] Consecutivo: %s | Acudiente: %s | Total: COP $%,.2f%n",
                        in.getIdInvoice(), in.getInvoiceNumber(), in.getGuardianName(), in.getAmount());
            }
            input = ConsoleInputReader.readString("\nIngrese el número o ID de factura a consultar: ").trim();
        }

        Optional<Invoice> invOpt = Optional.empty();
        try {
            int id = Integer.parseInt(input);
            invOpt = financeService.getInvoiceById(id);
        } catch (NumberFormatException e) {
            invOpt = financeService.getInvoiceByNumber(input);
        }

        if (invOpt.isEmpty()) {
            ConsoleHelper.printError("No se encontró ninguna factura con el criterio: " + input);
            ConsoleInputReader.waitForEnter();
            return;
        }

        Invoice invoice = invOpt.get();
        System.out.println("\nFormatos disponibles para " + invoice.getInvoiceNumber() + ":");
        System.out.println("1. Visualizar Formato TXT Estructurado (Estatuto Tributario Art. 617 / Consola / Agentes)");
        System.out.println("2. Visualizar Formato XML UBL 2.1 Estándar MUISCA / DIAN");
        System.out.println("3. Ver ambos formatos");
        int fOpt = ConsoleInputReader.readInt("Seleccione opción (1-3): ", 1, 3);

        if (fOpt == 1 || fOpt == 3) {
            System.out.println("\n" + "=".repeat(88));
            System.out.println("               VISUALIZACIÓN EN TEXTO PLANO (TXT) - INVOICE AGENT VIEW");
            System.out.println("=".repeat(88));
            Optional<String> txtOpt = financeService.getInvoiceTxtContent(invoice);
            if (txtOpt.isPresent()) {
                System.out.println(txtOpt.get());
            } else {
                ConsoleHelper.printError("No se pudo cargar el archivo TXT de la factura.");
            }
        }

        if (fOpt == 2 || fOpt == 3) {
            System.out.println("\n" + "=".repeat(88));
            System.out.println("               VISUALIZACIÓN EN XML UBL 2.1 (MUISCA / DIAN) - INVOICE XML VIEW");
            System.out.println("=".repeat(88));
            Optional<String> xmlOpt = financeService.getInvoiceXmlContent(invoice);
            if (xmlOpt.isPresent()) {
                System.out.println(xmlOpt.get());
            } else {
                ConsoleHelper.printError("No se pudo cargar el archivo XML de la factura.");
            }
        }

        ConsoleInputReader.waitForEnter();
    }

    private void showDefaultersReport() {
        ConsoleHelper.printSubHeader("REPORTE DE ESTUDIANTES CON PAGOS PENDIENTES (CARTERA MOROSA)");
        System.out.println("Identificación de estudiantes que NO han pagado la pensión escolar.");
        int year = ConsoleInputReader.readInt("Año lectivo: ", 2020, 2030);
        String period = ConsoleInputReader.readAcademicPeriod("Período (I / II / III / IV): ", false, false);

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