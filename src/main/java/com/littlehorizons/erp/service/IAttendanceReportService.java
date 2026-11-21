package com.littlehorizons.erp.service;

import com.littlehorizons.erp.model.report.AttendanceSummaryReport;

import java.io.File;
import java.util.List;

public interface IAttendanceReportService {
    List<AttendanceSummaryReport> generateConsolidatedReport();
    List<AttendanceSummaryReport> generateReportForStudent(int studentId);
    File exportReportToText(List<AttendanceSummaryReport> reports, String destinationPath);
    String formatReportAsText(List<AttendanceSummaryReport> reports);
}
