package com.littlehorizons.erp.view;

import com.littlehorizons.erp.model.academic.Student;
import com.littlehorizons.erp.model.finance.Invoice;
import com.littlehorizons.erp.model.finance.Payment;
import com.littlehorizons.erp.model.finance.PaymentConcept;

import java.math.BigDecimal;
import java.util.List;

public interface IFinanceView {
    void showMenu();
    void showProcessPaymentHeader();
    void showAvailableConcepts(List<PaymentConcept> concepts);
    void showBaseAmount(BigDecimal baseAmount);
    void showPaymentMethods();
    void showInvoiceSuccess(Invoice invoice, BigDecimal amountPaid);
    void showConceptsCatalog(List<PaymentConcept> concepts);
    void showStudentPaymentHistory(List<Payment> payments);
    void showGuardianPaymentHistory(List<Payment> payments);
    void showInvoices(List<Invoice> invoices);
    void showPlainTextInvoiceHeader();
    void showRecentInvoices(List<Invoice> invoices);
    void showAvailableInvoiceFormats(String invoiceNumber);
    void showInvoiceTextContent(String content);
    void showInvoiceXmlContent(String content);
    void showDefaultersHeader();
    void showDefaultersReport(String period, int year, List<Student> defaulters);
    void showSuccess(String message);
    void showError(String message);
    void showWarning(String message);
}
