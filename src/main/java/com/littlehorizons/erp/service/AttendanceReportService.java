package com.littlehorizons.erp.service;

import com.littlehorizons.erp.dao.academic.AttendanceDAO;
import com.littlehorizons.erp.dao.academic.AttendanceRepository;
import com.littlehorizons.erp.model.academic.Attendance;
import com.littlehorizons.erp.model.report.AttendanceSummaryReport;
import com.littlehorizons.erp.util.LoggerUtil;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class AttendanceReportService implements IAttendanceReportService {

    private final AttendanceRepository attendanceRepository;

    public AttendanceReportService() {
        this(new AttendanceDAO());
    }

    public AttendanceReportService(AttendanceRepository attendanceRepository) {
        this.attendanceRepository = Objects.requireNonNull(attendanceRepository, "AttendanceRepository cannot be null");
    }

    private record GroupKey(int studentId, String studentName, int subjectId, String subjectName) {}

    @Override
    public List<AttendanceSummaryReport> generateConsolidatedReport() {
        List<Attendance> attendances = attendanceRepository.findAll();
        return processAttendancesWithStream(attendances);
    }

    @Override
    public List<AttendanceSummaryReport> generateReportForStudent(int studentId) {
        List<Attendance> attendances = attendanceRepository.findByStudent(studentId);
        return processAttendancesWithStream(attendances);
    }

    private List<AttendanceSummaryReport> processAttendancesWithStream(List<Attendance> attendances) {
        if (attendances == null || attendances.isEmpty()) {
            return List.of();
        }

        Map<GroupKey, List<Attendance>> grouped = attendances.stream()
                .collect(Collectors.groupingBy(a -> new GroupKey(
                        a.getIdStudent() != null ? a.getIdStudent() : 0,
                        a.getStudentName() != null ? a.getStudentName() : "ID #" + a.getIdStudent(),
                        a.getIdSubject() != null ? a.getIdSubject() : 0,
                        a.getSubjectName() != null ? a.getSubjectName() : "Materia #" + a.getIdSubject()
                )));

        return grouped.entrySet().stream()
                .map(entry -> {
                    GroupKey key = entry.getKey();
                    List<Attendance> records = entry.getValue();

                    long total = records.size();
                    long presents = records.stream()
                            .filter(r -> "Presente".equalsIgnoreCase(r.getStatus()))
                            .count();
                    long absents = records.stream()
                            .filter(r -> "Ausente".equalsIgnoreCase(r.getStatus()))
                            .count();
                    long tardies = records.stream()
                            .filter(r -> "Tardanza".equalsIgnoreCase(r.getStatus()))
                            .count();
                    long excuses = records.stream()
                            .filter(r -> "Excusa Médica".equalsIgnoreCase(r.getStatus()) || "Excusa Medica".equalsIgnoreCase(r.getStatus()))
                            .count();

                    double percentage = total > 0 ? (presents * 100.0) / total : 0.0;

                    String alertStatus;
                    if (percentage >= 90.0) {
                        alertStatus = "EXCELENTE";
                    } else if (percentage >= 80.0) {
                        alertStatus = "BUENO";
                    } else if (percentage >= 75.0) {
                        alertStatus = "REGULAR";
                    } else {
                        alertStatus = "CRÍTICO (ALERTA)";
                    }

                    return new AttendanceSummaryReport(
                            key.studentId(),
                            key.studentName(),
                            key.subjectId(),
                            key.subjectName(),
                            total,
                            presents,
                            absents,
                            tardies,
                            excuses,
                            percentage,
                            alertStatus
                    );
                })
                .sorted(Comparator.comparingDouble(AttendanceSummaryReport::attendancePercentage)
                        .thenComparing(AttendanceSummaryReport::studentName))
                .collect(Collectors.toList());
    }

    @Override
    public String formatReportAsText(List<AttendanceSummaryReport> reports) {
        StringBuilder sb = new StringBuilder();
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        sb.append("========================================================================================\n");
        sb.append("                 COLEGIO LITTLE HORIZONS - REPORTE CONSOLIDADO DE ASISTENCIAS           \n");
        sb.append("========================================================================================\n");
        sb.append("Fecha de Emisión: ").append(timestamp).append("\n");
        sb.append("Total Grupos Analizados: ").append(reports.size()).append("\n");

        if (!reports.isEmpty()) {
            double avgRate = reports.stream()
                    .mapToDouble(AttendanceSummaryReport::attendancePercentage)
                    .average()
                    .orElse(0.0);
            long criticalAlertCount = reports.stream()
                    .filter(r -> "CRÍTICO (ALERTA)".equals(r.alertStatus()))
                    .count();

            sb.append(String.format("Tasa Promedio de Asistencia Global : %.2f%%\n", avgRate));
            sb.append(String.format("Casos en Alerta Crítica (< 75%%)    : %d\n", criticalAlertCount));
        }

        sb.append("----------------------------------------------------------------------------------------\n");
        sb.append(String.format("%-6s | %-25s | %-20s | %-4s | %-4s | %-4s | %-4s | %-7s | %-15s\n",
                "ID", "ESTUDIANTE", "MATERIA", "TOT", "PRES", "AUS", "TARD", "% ASIS", "ESTADO"));
        sb.append("----------------------------------------------------------------------------------------\n");

        if (reports.isEmpty()) {
            sb.append("No se registran datos de asistencia en el sistema.\n");
        } else {
            for (AttendanceSummaryReport r : reports) {
                sb.append(String.format("%-6d | %-25s | %-20s | %-4d | %-4d | %-4d | %-4d | %6.1f%% | %-15s\n",
                        r.idStudent(),
                        truncate(r.studentName(), 25),
                        truncate(r.subjectName(), 20),
                        r.totalSessions(),
                        r.presentCount(),
                        r.absentCount(),
                        r.tardyCount(),
                        r.attendancePercentage(),
                        r.alertStatus()));
            }
        }

        sb.append("========================================================================================\n");
        sb.append("Fin del Reporte. Generado automáticamente por Little Horizons ERP.\n");
        return sb.toString();
    }

    @Override
    public File exportReportToText(List<AttendanceSummaryReport> reports, String destinationPath) {
        String content = formatReportAsText(reports);
        File targetFile = new File(destinationPath != null && !destinationPath.isBlank() ? destinationPath : "reporte_asistencias.txt");

        File parent = targetFile.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        try (FileWriter writer = new FileWriter(targetFile, StandardCharsets.UTF_8)) {
            writer.write(content);
            LoggerUtil.info("Reporte de asistencias exportado exitosamente a: " + targetFile.getAbsolutePath());
            return targetFile;
        } catch (IOException e) {
            LoggerUtil.error("Error al exportar reporte de asistencia a texto: " + targetFile.getAbsolutePath(), e);
            throw new RuntimeException("Error al guardar reporte en formato TXT: " + e.getMessage(), e);
        }
    }

    private String truncate(String val, int maxLen) {
        if (val == null) {
            return "";
        }
        return val.length() <= maxLen ? val : val.substring(0, maxLen - 3) + "...";
    }
}
