package com.littlehorizons.erp.service;

import com.littlehorizons.erp.model.academic.AttendanceImportResult;

import java.nio.file.Path;

public interface IAttendanceImportService {
    AttendanceImportResult importFromCsv(Path filePath);
    AttendanceImportResult importDefaultCsv();
}
