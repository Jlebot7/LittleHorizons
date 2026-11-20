package com.littlehorizons.erp.service.export;

import com.littlehorizons.erp.dao.academic.GuardianRepository;
import com.littlehorizons.erp.dao.academic.StudentRepository;
import com.littlehorizons.erp.dao.finance.PaymentRepository;
import com.littlehorizons.erp.model.finance.Invoice;
import com.littlehorizons.erp.service.InvoiceTxtService;
import com.littlehorizons.erp.util.InvoiceContextResolver;
import com.littlehorizons.erp.util.LoggerUtil;
import com.littlehorizons.erp.util.TaxCalculator;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Optional;

public class TxtInvoiceExporter implements InvoiceExporter {

    private final PaymentRepository paymentRepository;
    private final StudentRepository studentRepository;
    private final GuardianRepository guardianRepository;
    private final InvoiceTxtService invoiceTxtService;

    public TxtInvoiceExporter(PaymentRepository paymentRepository,
                              StudentRepository studentRepository,
                              GuardianRepository guardianRepository,
                              InvoiceTxtService invoiceTxtService) {
        this.paymentRepository = paymentRepository;
        this.studentRepository = studentRepository;
        this.guardianRepository = guardianRepository;
        this.invoiceTxtService = invoiceTxtService;
    }

    @Override
    public String getSupportedFormat() {
        return "TXT";
    }

    @Override
    public Optional<String> exportInvoice(Invoice invoice) {
        if (invoice == null) return Optional.empty();

        String txtPath = invoice.getTxtFilePath();
        if (txtPath != null) {
            File file = new File(txtPath);
            if (file.exists()) {
                try {
                    return Optional.of(Files.readString(Paths.get(txtPath), StandardCharsets.UTF_8));
                } catch (Exception e) {
                    LoggerUtil.error("Error reading TXT invoice: " + txtPath, e);
                }
            }
        }

        try {
            InvoiceContextResolver.ResolvedInvoiceContext ctx =
                    InvoiceContextResolver.resolve(invoice, paymentRepository, studentRepository, guardianRepository);

            TaxCalculator.TaxBreakdown tax = TaxCalculator.calculateTax(ctx.amount(), ctx.concept());

            String content = invoiceTxtService.generateTxtContent(
                    invoice.getInvoiceNumber(),
                    invoice.getIssueDate() != null ? invoice.getIssueDate() : LocalDate.now(),
                    invoice.getCufe() != null ? invoice.getCufe() : "00000000000000000000000000000000",
                    ctx.student(), ctx.guardian(), ctx.concept(), ctx.payment(), tax
            );

            if (txtPath != null) {
                invoiceTxtService.writeTxtFile(txtPath, content);
            }
            return Optional.of(content);
        } catch (Exception e) {
            LoggerUtil.error("Failed to generate TXT invoice for " + invoice.getInvoiceNumber(), e);
            return Optional.empty();
        }
    }
}
