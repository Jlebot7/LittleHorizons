package com.littlehorizons.erp.service.export;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class InvoiceExporterFactory {

    private final Map<String, InvoiceExporter> exporters = new HashMap<>();

    public void registerExporter(InvoiceExporter exporter) {
        if (exporter != null && exporter.getSupportedFormat() != null) {
            exporters.put(exporter.getSupportedFormat().toUpperCase(), exporter);
        }
    }

    public Optional<InvoiceExporter> getExporter(String format) {
        if (format == null) return Optional.empty();
        return Optional.ofNullable(exporters.get(format.toUpperCase()));
    }
}
