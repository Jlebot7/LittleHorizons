package com.littlehorizons.erp.model.academic;

public record AttendanceImportResult(
        int totalLines,
        int successLines,
        int errorLines,
        String errorMessage
) {
    public boolean hasError() {
        return errorMessage != null && !errorMessage.isBlank();
    }
}
