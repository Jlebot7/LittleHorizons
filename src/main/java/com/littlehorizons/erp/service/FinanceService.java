package com.littlehorizons.erp.service;

import com.littlehorizons.erp.dao.academic.GuardianDAO;
import com.littlehorizons.erp.dao.academic.GuardianRepository;
import com.littlehorizons.erp.dao.academic.StudentDAO;
import com.littlehorizons.erp.dao.academic.StudentRepository;
import com.littlehorizons.erp.dao.finance.PaymentDAO;
import com.littlehorizons.erp.dao.finance.PaymentRepository;
import com.littlehorizons.erp.model.academic.Student;
import com.littlehorizons.erp.model.finance.Invoice;
import com.littlehorizons.erp.model.finance.Payment;
import com.littlehorizons.erp.model.finance.PaymentConcept;
import com.littlehorizons.erp.service.export.InvoiceExporterFactory;
import com.littlehorizons.erp.service.export.PdfInvoiceExporter;
import com.littlehorizons.erp.service.export.TxtInvoiceExporter;
import com.littlehorizons.erp.service.export.XmlInvoiceExporter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class FinanceService implements IFinanceService {

    private final PaymentRepository paymentDAO;
    private final StudentRepository studentDAO;
    private final GuardianRepository guardianDAO;
    private final InvoicePdfService invoicePdfService;
    private final InvoiceTxtService invoiceTxtService;
    private final InvoiceXmlService invoiceXmlService;
    private final InvoiceExporterFactory invoiceExporterFactory;

    public FinanceService() {
        this(new PaymentDAO(), new StudentDAO(), new GuardianDAO(),
                new InvoicePdfService(), new InvoiceTxtService(), new InvoiceXmlService());
    }

    public FinanceService(PaymentRepository paymentDAO,
                          StudentRepository studentDAO,
                          GuardianRepository guardianDAO,
                          InvoicePdfService invoicePdfService,
                          InvoiceTxtService invoiceTxtService,
                          InvoiceXmlService invoiceXmlService) {
        this.paymentDAO = paymentDAO;
        this.studentDAO = studentDAO;
        this.guardianDAO = guardianDAO;
        this.invoicePdfService = invoicePdfService;
        this.invoiceTxtService = invoiceTxtService;
        this.invoiceXmlService = invoiceXmlService;

        this.invoiceExporterFactory = new InvoiceExporterFactory();
        this.invoiceExporterFactory.registerExporter(new PdfInvoiceExporter());
        this.invoiceExporterFactory.registerExporter(new TxtInvoiceExporter(paymentDAO, studentDAO, guardianDAO, invoiceTxtService));
        this.invoiceExporterFactory.registerExporter(new XmlInvoiceExporter(paymentDAO, studentDAO, guardianDAO, invoiceXmlService));
    }

    @Override
    public List<PaymentConcept> getAllConcepts() {
        return paymentDAO.findAllConcepts();
    }

    @Override
    public Optional<PaymentConcept> getConceptById(int id) {
        return paymentDAO.findConceptById(id);
    }

    @Override
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

    @Override
    public List<Payment> getPaymentsByStudent(int studentId) {
        return paymentDAO.findPaymentsByStudent(studentId);
    }

    @Override
    public List<Payment> getPaymentsByGuardian(int guardianId) {
        return paymentDAO.findPaymentsByGuardian(guardianId);
    }

    @Override
    public List<Payment> getAllPayments() {
        return paymentDAO.findAllPayments();
    }

    @Override
    public List<Invoice> getAllInvoices() {
        return paymentDAO.findAllInvoices();
    }

    @Override
    public Optional<Invoice> getInvoiceByNumber(String invoiceNumber) {
        return paymentDAO.findInvoiceByNumber(invoiceNumber);
    }

    @Override
    public Optional<Invoice> getInvoiceById(int idInvoice) {
        return paymentDAO.findInvoiceById(idInvoice);
    }

    @Override
    public Optional<String> getInvoiceTxtContent(Invoice invoice) {
        return invoiceExporterFactory.getExporter("TXT")
                .flatMap(exporter -> exporter.exportInvoice(invoice));
    }

    @Override
    public Optional<String> getInvoiceXmlContent(Invoice invoice) {
        return invoiceExporterFactory.getExporter("XML")
                .flatMap(exporter -> exporter.exportInvoice(invoice));
    }

    @Override
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

    @Override
    public Map<String, BigDecimal> getRevenueByPaymentMethod() {
        return getAllPayments().stream()
                .filter(p -> p.getPaymentMethod() != null && p.getAmountPaid() != null)
                .collect(Collectors.groupingBy(Payment::getPaymentMethod,
                        Collectors.reducing(BigDecimal.ZERO, Payment::getAmountPaid, BigDecimal::add)));
    }

    @Override
    public Map<Integer, BigDecimal> getRevenueByConcept() {
        return getAllPayments().stream()
                .filter(p -> p.getIdConcept() != null && p.getAmountPaid() != null)
                .collect(Collectors.groupingBy(Payment::getIdConcept,
                        Collectors.reducing(BigDecimal.ZERO, Payment::getAmountPaid, BigDecimal::add)));
    }

    @Override
    public BigDecimal getTotalTaxCollected() {
        return getAllInvoices().stream()
                .map(Invoice::getTaxAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public BigDecimal getTotalTaxableBase() {
        return getAllInvoices().stream()
                .map(Invoice::getSubtotalAmount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public DoubleSummaryStatistics getPaymentAmountStatistics() {
        return getAllPayments().stream()
                .map(Payment::getAmountPaid)
                .filter(Objects::nonNull)
                .mapToDouble(BigDecimal::doubleValue)
                .summaryStatistics();
    }

    @Override
    public Map<Boolean, List<Payment>> partitionPaymentsByExemption(List<PaymentConcept> concepts) {
        Set<Integer> exemptIds = concepts.stream()
                .filter(c -> Boolean.TRUE.equals(c.getIsTaxExempt()))
                .map(PaymentConcept::getIdConcept)
                .collect(Collectors.toSet());

        return getAllPayments().stream()
                .filter(p -> p.getIdConcept() != null)
                .collect(Collectors.partitioningBy(p -> exemptIds.contains(p.getIdConcept())));
    }
}
