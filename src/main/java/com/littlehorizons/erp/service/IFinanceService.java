package com.littlehorizons.erp.service;

import com.littlehorizons.erp.model.academic.Student;
import com.littlehorizons.erp.model.finance.Invoice;
import com.littlehorizons.erp.model.finance.Payment;
import com.littlehorizons.erp.model.finance.PaymentConcept;

import java.math.BigDecimal;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface IFinanceService {
    List<PaymentConcept> getAllConcepts();
    Optional<PaymentConcept> getConceptById(int id);
    Optional<Invoice> processPaymentAndInvoice(Payment payment);
    List<Payment> getPaymentsByStudent(int studentId);
    List<Payment> getPaymentsByGuardian(int guardianId);
    List<Payment> getAllPayments();
    List<Invoice> getAllInvoices();
    Optional<Invoice> getInvoiceByNumber(String invoiceNumber);
    Optional<Invoice> getInvoiceById(int idInvoice);
    Optional<String> getInvoiceTxtContent(Invoice invoice);
    Optional<String> getInvoiceXmlContent(Invoice invoice);
    List<Student> getDefaulterStudents(int conceptId, int year, String period);
    Map<String, BigDecimal> getRevenueByPaymentMethod();
    Map<Integer, BigDecimal> getRevenueByConcept();
    BigDecimal getTotalTaxCollected();
    BigDecimal getTotalTaxableBase();
    DoubleSummaryStatistics getPaymentAmountStatistics();
    Map<Boolean, List<Payment>> partitionPaymentsByExemption(List<PaymentConcept> concepts);
}
