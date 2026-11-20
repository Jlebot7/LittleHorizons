package com.littlehorizons.erp.service.export;

import com.littlehorizons.erp.model.finance.Invoice;

import java.util.Optional;

public interface InvoiceExporter {
    String getSupportedFormat();
    Optional<String> exportInvoice(Invoice invoice);
}
