package com.littlehorizons.erp.util;

import com.littlehorizons.erp.dao.academic.GuardianRepository;
import com.littlehorizons.erp.dao.academic.StudentRepository;
import com.littlehorizons.erp.dao.finance.PaymentRepository;
import com.littlehorizons.erp.model.academic.Guardian;
import com.littlehorizons.erp.model.academic.Student;
import com.littlehorizons.erp.model.finance.Invoice;
import com.littlehorizons.erp.model.finance.Payment;
import com.littlehorizons.erp.model.finance.PaymentConcept;

import java.math.BigDecimal;

public final class InvoiceContextResolver {

    private InvoiceContextResolver() {
    }

    public record ResolvedInvoiceContext(
            Student student,
            Guardian guardian,
            PaymentConcept concept,
            Payment payment,
            BigDecimal amount
    ) {}

    public static ResolvedInvoiceContext resolve(
            Invoice invoice,
            PaymentRepository paymentRepository,
            StudentRepository studentRepository,
            GuardianRepository guardianRepository) {

        Payment payment = null;
        Student student = null;
        Guardian guardian = null;
        PaymentConcept concept = null;

        if (invoice != null && invoice.getIdPayment() != null) {
            payment = paymentRepository.findPaymentById(invoice.getIdPayment()).orElse(null);
        }

        if (payment != null) {
            if (payment.getIdStudent() != null) {
                student = studentRepository.findById(payment.getIdStudent()).orElse(null);
            }
            if (payment.getIdGuardian() != null) {
                guardian = guardianRepository.findById(payment.getIdGuardian()).orElse(null);
            }
            if (payment.getIdConcept() != null) {
                concept = paymentRepository.findConceptById(payment.getIdConcept()).orElse(null);
            }
        }

        if (student == null) {
            student = new Student();
            student.setFirstName(invoice != null && invoice.getStudentName() != null ? invoice.getStudentName() : "Estudiante");
            student.setFirstLastName("");
            student.setDocumentType("TI");
            student.setDocumentNumber("1098000000");
            student.setAddress("Bogotá D.C.");
        }

        if (guardian == null) {
            guardian = new Guardian();
            guardian.setFirstName(invoice != null && invoice.getGuardianName() != null ? invoice.getGuardianName() : "Acudiente");
            guardian.setFirstLastName("");
            guardian.setDocumentType("CC");
            guardian.setDocumentNumber("222222222222");
            guardian.setAddress("Bogotá D.C.");
        }

        BigDecimal amount = invoice != null && invoice.getAmount() != null ? invoice.getAmount() : BigDecimal.ZERO;

        if (concept == null) {
            concept = new PaymentConcept();
            concept.setConceptName(invoice != null && invoice.getConceptName() != null ? invoice.getConceptName() : "Servicios Educativos");
            concept.setConceptCode("EDU-01");
            concept.setBaseAmount(amount);
        }

        return new ResolvedInvoiceContext(student, guardian, concept, payment, amount);
    }
}
