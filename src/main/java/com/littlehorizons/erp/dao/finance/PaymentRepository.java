package com.littlehorizons.erp.dao.finance;

import com.littlehorizons.erp.model.finance.Invoice;
import com.littlehorizons.erp.model.finance.Payment;
import com.littlehorizons.erp.model.finance.PaymentConcept;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository {
    Integer savePayment(Payment payment);
    Optional<Payment> findPaymentById(int id);
    List<Payment> findPaymentsByStudent(int studentId);
    List<Payment> findPaymentsByGuardian(int guardianId);
    List<Payment> findAllPayments();
    List<PaymentConcept> findAllConcepts();
    Optional<PaymentConcept> findConceptById(int id);
    boolean saveInvoice(Invoice invoice);
    Optional<Invoice> findInvoiceByNumber(String invoiceNumber);
    Optional<Invoice> findInvoiceById(int id);
    List<Invoice> findAllInvoices();
    int getNextInvoiceSequence();
}
