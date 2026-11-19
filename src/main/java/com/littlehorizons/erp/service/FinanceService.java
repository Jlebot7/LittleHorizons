package com.littlehorizons.erp.service;

import com.littlehorizons.erp.dao.academic.GuardianDAO;
import com.littlehorizons.erp.dao.academic.StudentDAO;
import com.littlehorizons.erp.dao.finance.PaymentDAO;
import com.littlehorizons.erp.model.academic.Guardian;
import com.littlehorizons.erp.model.academic.Student;
import com.littlehorizons.erp.model.finance.Invoice;
import com.littlehorizons.erp.model.finance.Payment;
import com.littlehorizons.erp.model.finance.PaymentConcept;
import com.littlehorizons.erp.util.LoggerUtil;
import com.littlehorizons.erp.util.TaxCalculator;
import com.littlehorizons.erp.util.TaxCalculator.TaxBreakdown;

import java.io.File;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class FinanceService {

    private final PaymentDAO paymentDAO;
    private final StudentDAO studentDAO;
    private final GuardianDAO guardianDAO;
    private final InvoicePdfService invoicePdfService;
    private final InvoiceTxtService invoiceTxtService;
    private final InvoiceXmlService invoiceXmlService;

    public FinanceService() {
        this.paymentDAO = new PaymentDAO();
        this.studentDAO = new StudentDAO();
        this.guardianDAO = new GuardianDAO();
        this.invoicePdfService = new InvoicePdfService();
        this.invoiceTxtService = new InvoiceTxtService();
        this.invoiceXmlService = new InvoiceXmlService();
    }

    public List<PaymentConcept> getAllConcepts() {
        return paymentDAO.findAllConcepts();
    }

    public Optional<PaymentConcept> getConceptById(int id) {
        return paymentDAO.findConceptById(id);
    }

    public Optional<Invoice> processPaymentAndInvoice(Payment payment) {
        if (payment.getAmountPaid() == null || payment.getAmountPaid().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto a pagar debe ser mayor a cero.");
        }

        if (payment.getReceiptNumber() == null || payment.getReceiptNumber().trim().isEmpty()) {
            int seq = paymentDAO.findAllPayments().size() + 1;
            payment.setReceiptNumber(String.format("REC-%d-%05d",
                    payment.getAcademicYear() != null ? payment.getAcademicYear() : 2024, seq));
        }

        if (payment.getPaymentDate() == null) {
            payment.setPaymentDate(LocalDate.now());
        }

        Integer paymentId = paymentDAO.savePayment(payment);
        if (paymentId != null) {
            payment.setIdPago(paymentId);
            return invoicePdfService.generateInvoiceForPayment(payment);
        }
        return Optional.empty();
    }

    public List<Payment> getPaymentsByStudent(int studentId) {
        return paymentDAO.findPaymentsByStudent(studentId);
    }

    public List<Payment> getPaymentsByGuardian(int guardianId) {
        return paymentDAO.findPaymentsByGuardian(guardianId);
    }

    public List<Payment> getAllPayments() {
        return paymentDAO.findAllPayments();
    }

    public List<Invoice> getAllInvoices() {
        return paymentDAO.findAllInvoices();
    }

    public Optional<Invoice> getInvoiceByNumber(String invoiceNumber) {
        return paymentDAO.findInvoiceByNumber(invoiceNumber);
    }

    public Optional<Invoice> getInvoiceById(int idInvoice) {
        return paymentDAO.findInvoiceById(idInvoice);
    }

    public Optional<String> getInvoiceTxtContent(Invoice invoice) {
        if (invoice == null) return Optional.empty();

        String txtPath = invoice.getTxtFilePath();
        File file = new File(txtPath);
        if (file.exists()) {
            try {
                return Optional.of(Files.readString(Paths.get(txtPath), StandardCharsets.UTF_8));
            } catch (Exception e) {
                LoggerUtil.error("Error reading TXT invoice file: " + txtPath, e);
            }
        }

        try {
            Payment payment = paymentDAO.findPaymentById(invoice.getIdPayment()).orElse(null);
            Student student = null;
            Guardian guardian = null;
            PaymentConcept concept = null;

            if (payment != null) {
                student = studentDAO.findById(payment.getIdStudent()).orElse(null);
                guardian = guardianDAO.findById(payment.getIdGuardian()).orElse(null);
                concept = paymentDAO.findConceptById(payment.getIdConcept()).orElse(null);
            }

            if (student == null) {
                student = new Student();
                student.setFirstName(invoice.getStudentName() != null ? invoice.getStudentName() : "Estudiante");
                student.setFirstLastName("");
                student.setDocumentType("TI");
                student.setDocumentNumber("1098000000");
                student.setAddress("Bogotá D.C.");
            }

            if (guardian == null) {
                guardian = new Guardian();
                guardian.setFirstName(invoice.getGuardianName() != null ? invoice.getGuardianName() : "Acudiente");
                guardian.setFirstLastName("");
                guardian.setDocumentType("CC");
                guardian.setDocumentNumber("222222222222");
                guardian.setAddress("Bogotá D.C.");
            }

            if (concept == null) {
                concept = new PaymentConcept();
                concept.setConceptName(invoice.getConceptName() != null ? invoice.getConceptName() : "Servicios Educativos");
                concept.setConceptCode("EDU-01");
                concept.setBaseAmount(invoice.getAmount());
            }

            BigDecimal amount = invoice.getAmount() != null ? invoice.getAmount() : BigDecimal.ZERO;
            TaxBreakdown tax = TaxCalculator.calculateTax(amount, concept);

            String content = invoiceTxtService.generateTxtContent(
                    invoice.getInvoiceNumber(),
                    invoice.getIssueDate() != null ? invoice.getIssueDate() : LocalDate.now(),
                    invoice.getCufe() != null ? invoice.getCufe() : "00000000000000000000000000000000",
                    student, guardian, concept, payment, tax
            );

            invoiceTxtService.writeTxtFile(txtPath, content);
            return Optional.of(content);
        } catch (Exception e) {
            LoggerUtil.error("Failed to generate TXT invoice on-the-fly for " + invoice.getInvoiceNumber(), e);
            return Optional.empty();
        }
    }

    public Optional<String> getInvoiceXmlContent(Invoice invoice) {
        if (invoice == null) return Optional.empty();

        String xmlPath = invoice.getXmlFilePath();
        File file = new File(xmlPath);
        if (file.exists()) {
            try {
                return Optional.of(Files.readString(Paths.get(xmlPath), StandardCharsets.UTF_8));
            } catch (Exception e) {
                LoggerUtil.error("Error reading XML invoice file: " + xmlPath, e);
            }
        }

        try {
            Payment payment = paymentDAO.findPaymentById(invoice.getIdPayment()).orElse(null);
            Student student = null;
            Guardian guardian = null;
            PaymentConcept concept = null;

            if (payment != null) {
                student = studentDAO.findById(payment.getIdStudent()).orElse(null);
                guardian = guardianDAO.findById(payment.getIdGuardian()).orElse(null);
                concept = paymentDAO.findConceptById(payment.getIdConcept()).orElse(null);
            }

            if (student == null) {
                student = new Student();
                student.setFirstName(invoice.getStudentName() != null ? invoice.getStudentName() : "Estudiante");
                student.setFirstLastName("");
                student.setDocumentType("TI");
                student.setDocumentNumber("1098000000");
                student.setAddress("Bogotá D.C.");
            }

            if (guardian == null) {
                guardian = new Guardian();
                guardian.setFirstName(invoice.getGuardianName() != null ? invoice.getGuardianName() : "Acudiente");
                guardian.setFirstLastName("");
                guardian.setDocumentType("CC");
                guardian.setDocumentNumber("222222222222");
                guardian.setAddress("Bogotá D.C.");
            }

            if (concept == null) {
                concept = new PaymentConcept();
                concept.setConceptName(invoice.getConceptName() != null ? invoice.getConceptName() : "Servicios Educativos");
                concept.setConceptCode("EDU-01");
                concept.setBaseAmount(invoice.getAmount());
            }

            BigDecimal amount = invoice.getAmount() != null ? invoice.getAmount() : BigDecimal.ZERO;
            TaxBreakdown tax = TaxCalculator.calculateTax(amount, concept);

            String content = invoiceXmlService.generateXmlContent(
                    invoice.getInvoiceNumber(),
                    invoice.getIssueDate() != null ? invoice.getIssueDate() : LocalDate.now(),
                    invoice.getCufe() != null ? invoice.getCufe() : "00000000000000000000000000000000",
                    student, guardian, concept, payment, tax
            );

            invoiceXmlService.writeXmlFile(xmlPath, content);
            return Optional.of(content);
        } catch (Exception e) {
            LoggerUtil.error("Failed to generate XML invoice on-the-fly for " + invoice.getInvoiceNumber(), e);
            return Optional.empty();
        }
    }

    public List<Student> getDefaulterStudents(int conceptId, int year, String period) {
        List<Student> allStudents = studentDAO.findAll();
        List<Payment> allPayments = paymentDAO.findAllPayments();

        Set<Integer> paidStudentIds = allPayments.stream()
                .filter(p -> p.getIdConcept() == conceptId &&
                        p.getAcademicYear() != null && p.getAcademicYear() == year &&
                        p.getAcademicPeriod() != null && p.getAcademicPeriod().equalsIgnoreCase(period))
                .map(Payment::getIdStudent)
                .collect(Collectors.toSet());

        return allStudents.stream()
                .filter(s -> "Activo".equalsIgnoreCase(s.getStatus()))
                .filter(s -> !paidStudentIds.contains(s.getIdStudent()))
                .collect(Collectors.toList());
    }
}