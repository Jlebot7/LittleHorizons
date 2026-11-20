package com.littlehorizons.erp.controller;

import com.littlehorizons.erp.model.academic.Student;
import com.littlehorizons.erp.model.finance.Invoice;
import com.littlehorizons.erp.model.finance.Payment;
import com.littlehorizons.erp.model.finance.PaymentConcept;
import com.littlehorizons.erp.service.IFinanceService;
import com.littlehorizons.erp.util.ConsoleInputReader;
import com.littlehorizons.erp.view.FinanceConsoleView;
import com.littlehorizons.erp.view.IFinanceView;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class FinanceController {

    private final IFinanceService financeService;
    private final IFinanceView financeView;

    public FinanceController(IFinanceService financeService) {
        this(financeService, new FinanceConsoleView());
    }

    public FinanceController(IFinanceService financeService, IFinanceView financeView) {
        this.financeService = financeService;
        this.financeView = financeView;
    }

    public void run() {
        boolean back = false;
        while (!back) {
            financeView.showMenu();
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
        financeView.showProcessPaymentHeader();
        int studentId = ConsoleInputReader.readInt("ID del estudiante: ", 1, 99999);
        int guardianId = ConsoleInputReader.readInt("ID del acudiente responsable del pago: ", 1, 99999);

        List<PaymentConcept> concepts = financeService.getAllConcepts();
        financeView.showAvailableConcepts(concepts);

        int conceptId = ConsoleInputReader.readInt("Seleccione el ID del concepto: ", 1, concepts.size());
        Optional<PaymentConcept> concOpt = financeService.getConceptById(conceptId);
        BigDecimal defaultAmount = concOpt.map(PaymentConcept::getBaseAmount).orElse(new BigDecimal("350000.00"));

        financeView.showBaseAmount(defaultAmount);
        boolean custom = ConsoleInputReader.readBoolean("¿Desea aplicar un valor personalizado (ej: descuento o beca)?");
        BigDecimal amountPaid = defaultAmount;
        if (custom) {
            amountPaid = ConsoleInputReader.readBigDecimal("Ingrese el monto acordado a pagar: ",
                    BigDecimal.ONE, new BigDecimal("100000000.00"));
        }

        financeView.showPaymentMethods();
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
                financeView.showInvoiceSuccess(invoiceOpt.get(), amountPaid);
            } else {
                financeView.showError("No se pudo procesar el pago o generar la factura.");
            }
        } catch (IllegalArgumentException e) {
            financeView.showError("Validación de pago fallida: " + e.getMessage());
        } catch (IllegalStateException e) {
            financeView.showError(e.getMessage());
        } catch (Exception e) {
            financeView.showError("Error en procesamiento financiero: " + e.getMessage());
        }
        ConsoleInputReader.waitForEnter();
    }

    private void listConcepts() {
        List<PaymentConcept> concepts = financeService.getAllConcepts();
        financeView.showConceptsCatalog(concepts);
        ConsoleInputReader.waitForEnter();
    }

    private void viewStudentPaymentHistory() {
        int studentId = ConsoleInputReader.readInt("Ingrese el ID del estudiante: ", 1, 99999);
        List<Payment> payments = financeService.getPaymentsByStudent(studentId);
        financeView.showStudentPaymentHistory(payments);
        ConsoleInputReader.waitForEnter();
    }

    private void viewGuardianPaymentHistory() {
        int guardianId = ConsoleInputReader.readInt("Ingrese el ID del acudiente: ", 1, 99999);
        List<Payment> payments = financeService.getPaymentsByGuardian(guardianId);
        financeView.showGuardianPaymentHistory(payments);
        ConsoleInputReader.waitForEnter();
    }

    private void listInvoices() {
        List<Invoice> invoices = financeService.getAllInvoices();
        financeView.showInvoices(invoices);
        ConsoleInputReader.waitForEnter();
    }

    private void viewInvoicePlainText() {
        financeView.showPlainTextInvoiceHeader();
        String input = ConsoleInputReader.readString("Ingrese el número o ID de factura (o '?' para listar las últimas): ").trim();

        if (input.equals("?")) {
            List<Invoice> list = financeService.getAllInvoices();
            financeView.showRecentInvoices(list);
            input = ConsoleInputReader.readString("\nIngrese el número o ID de factura a consultar: ").trim();
        }

        Optional<Invoice> invOpt;
        try {
            int id = Integer.parseInt(input);
            invOpt = financeService.getInvoiceById(id);
        } catch (NumberFormatException e) {
            invOpt = financeService.getInvoiceByNumber(input);
        }

        if (invOpt.isEmpty()) {
            financeView.showError("No se encontró ninguna factura con el criterio: " + input);
            ConsoleInputReader.waitForEnter();
            return;
        }

        Invoice invoice = invOpt.get();
        financeView.showAvailableInvoiceFormats(invoice.getInvoiceNumber());
        int fOpt = ConsoleInputReader.readInt("Seleccione opción (1-3): ", 1, 3);

        if (fOpt == 1 || fOpt == 3) {
            Optional<String> txtOpt = financeService.getInvoiceTxtContent(invoice);
            if (txtOpt.isPresent()) {
                financeView.showInvoiceTextContent(txtOpt.get());
            } else {
                financeView.showError("No se pudo cargar el archivo TXT de la factura.");
            }
        }

        if (fOpt == 2 || fOpt == 3) {
            Optional<String> xmlOpt = financeService.getInvoiceXmlContent(invoice);
            if (xmlOpt.isPresent()) {
                financeView.showInvoiceXmlContent(xmlOpt.get());
            } else {
                financeView.showError("No se pudo cargar el archivo XML de la factura.");
            }
        }

        ConsoleInputReader.waitForEnter();
    }

    private void showDefaultersReport() {
        financeView.showDefaultersHeader();
        int year = ConsoleInputReader.readInt("Año lectivo: ", 2020, 2030);
        String period = ConsoleInputReader.readAcademicPeriod("Período (I / II / III / IV): ", false, false);

        List<Student> defaulters = financeService.getDefaulterStudents(2, year, period);
        financeView.showDefaultersReport(period, year, defaulters);
        ConsoleInputReader.waitForEnter();
    }
}
