package com.littlehorizons.erp.service;

import com.littlehorizons.erp.dao.academic.StudentDAO;
import com.littlehorizons.erp.dao.finance.PaymentDAO;
import com.littlehorizons.erp.model.academic.Student;
import com.littlehorizons.erp.model.finance.Invoice;
import com.littlehorizons.erp.model.finance.Payment;
import com.littlehorizons.erp.model.finance.PaymentConcept;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service orchestrating financial payments, concept management,
 * electronic invoice issuance, and delinquent student detection.
 */
public class FinanceService {

    private final PaymentDAO paymentDAO;
    private final StudentDAO studentDAO;
    private final InvoicePdfService invoicePdfService;

    public FinanceService() {
        this.paymentDAO = new PaymentDAO();
        this.studentDAO = new StudentDAO();
        this.invoicePdfService = new InvoicePdfService();
    }

    public List<PaymentConcept> getAllConcepts() {
        return paymentDAO.findAllConcepts();
    }

    public Optional<PaymentConcept> getConceptById(int id) {
        return paymentDAO.findConceptById(id);
    }

    /**
     * Registers a payment, assigns an automated sequential receipt number,
     * and automatically generates an electronic PDF invoice with simulated CUFE and QR code.
     *
     * @param payment Payment details
     * @return Generated Invoice if successful, empty if failed
     */
    public Optional<Invoice> processPaymentAndInvoice(Payment payment) {
        if (payment.getAmountPaid() == null || payment.getAmountPaid().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto a pagar debe ser mayor a cero.");
        }

        // Generate receipt number if not present
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

    /**
     * Identifies active students who do not have a recorded payment
     * for a specific recurring concept (e.g. Pensión Mensual) in the given year/period.
     * Implemented using Java Stream API.
     */
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
