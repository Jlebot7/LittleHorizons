package com.littlehorizons.erp.service.export;

import com.littlehorizons.erp.model.finance.Invoice;

import java.io.File;
import java.util.Optional;

public class PdfInvoiceExporter implements InvoiceExporter {

    @Override
    public String getSupportedFormat() {
        return "PDF";
    }

    @Override
    public Optional<String> exportInvoice(Invoice invoice) {
        if (invoice == null || invoice.getFilePath() == null) {
            return Optional.empty();
        }
        File pdfFile = new File(invoice.getFilePath());
        if (pdfFile.exists()) {
            return Optional.of(pdfFile.getAbsolutePath());
        }
        return Optional.empty();
    }
}
