package com.littlehorizons.erp.model.report;

public record AttendanceSummaryReport(
        int idStudent,
        String studentName,
        int idSubject,
        String subjectName,
        long totalSessions,
        long presentCount,
        long absentCount,
        long tardyCount,
        long excuseCount,
        double attendancePercentage,
        String alertStatus
) {
}
