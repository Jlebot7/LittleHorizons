package com.littlehorizons.erp;

import com.littlehorizons.erp.dao.academic.AttendanceRepository;
import com.littlehorizons.erp.model.academic.Attendance;
import com.littlehorizons.erp.model.report.AttendanceSummaryReport;
import com.littlehorizons.erp.service.AttendanceReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AttendanceReportServiceTest {

    private AttendanceRepository fakeRepo;
    private AttendanceReportService reportService;
    private List<Attendance> sampleData;

    @BeforeEach
    void setUp() {
        sampleData = new ArrayList<>();

        Attendance a1 = new Attendance(1, 1, 5, LocalDate.of(2024, 10, 1), "Presente", null, 1);
        a1.setStudentName("Alejandro Morales");
        a1.setSubjectName("Matemáticas");
        sampleData.add(a1);

        Attendance a2 = new Attendance(2, 1, 5, LocalDate.of(2024, 10, 2), "Presente", null, 1);
        a2.setStudentName("Alejandro Morales");
        a2.setSubjectName("Matemáticas");
        sampleData.add(a2);

        Attendance a3 = new Attendance(3, 1, 5, LocalDate.of(2024, 10, 3), "Ausente", null, 1);
        a3.setStudentName("Alejandro Morales");
        a3.setSubjectName("Matemáticas");
        sampleData.add(a3);

        Attendance a4 = new Attendance(4, 1, 5, LocalDate.of(2024, 10, 4), "Tardanza", null, 1);
        a4.setStudentName("Alejandro Morales");
        a4.setSubjectName("Matemáticas");
        sampleData.add(a4);

        Attendance b1 = new Attendance(5, 2, 8, LocalDate.of(2024, 10, 1), "Ausente", null, 1);
        b1.setStudentName("Valentina Rojas");
        b1.setSubjectName("Ciencias");
        sampleData.add(b1);

        Attendance b2 = new Attendance(6, 2, 8, LocalDate.of(2024, 10, 2), "Ausente", null, 1);
        b2.setStudentName("Valentina Rojas");
        b2.setSubjectName("Ciencias");
        sampleData.add(b2);

        fakeRepo = new AttendanceRepository() {
            @Override
            public boolean recordAttendance(Attendance attendance) {
                sampleData.add(attendance);
                return true;
            }

            @Override
            public List<Attendance> findByStudent(int studentId) {
                return sampleData.stream().filter(a -> a.getIdStudent() == studentId).toList();
            }

            @Override
            public List<Attendance> findAllRecent() {
                return sampleData;
            }

            @Override
            public List<Attendance> findAll() {
                return sampleData;
            }
        };

        reportService = new AttendanceReportService(fakeRepo);
    }

    @Test
    void testGenerateConsolidatedReportMetrics() {
        List<AttendanceSummaryReport> reports = reportService.generateConsolidatedReport();
        assertNotNull(reports);
        assertEquals(2, reports.size());

        AttendanceSummaryReport student2 = reports.stream()
                .filter(r -> r.idStudent() == 2)
                .findFirst()
                .orElseThrow();
        assertEquals("Valentina Rojas", student2.studentName());
        assertEquals("Ciencias", student2.subjectName());
        assertEquals(2, student2.totalSessions());
        assertEquals(0, student2.presentCount());
        assertEquals(2, student2.absentCount());
        assertEquals(0.0, student2.attendancePercentage());
        assertEquals("CRÍTICO (ALERTA)", student2.alertStatus());

        AttendanceSummaryReport student1 = reports.stream()
                .filter(r -> r.idStudent() == 1)
                .findFirst()
                .orElseThrow();
        assertEquals(4, student1.totalSessions());
        assertEquals(2, student1.presentCount());
        assertEquals(1, student1.absentCount());
        assertEquals(1, student1.tardyCount());
        assertEquals(50.0, student1.attendancePercentage());
        assertEquals("CRÍTICO (ALERTA)", student1.alertStatus());
    }

    @Test
    void testFormatReportAsText() {
        List<AttendanceSummaryReport> reports = reportService.generateConsolidatedReport();
        String text = reportService.formatReportAsText(reports);

        assertNotNull(text);
        assertTrue(text.contains("COLEGIO LITTLE HORIZONS - REPORTE CONSOLIDADO DE ASISTENCIAS"));
        assertTrue(text.contains("Alejandro Morales"));
        assertTrue(text.contains("Valentina Rojas"));
        assertTrue(text.contains("Tasa Promedio de Asistencia Global"));
        assertTrue(text.contains("Fin del Reporte"));
    }

    @Test
    void testExportReportToTextFile(@TempDir Path tempDir) throws IOException {
        List<AttendanceSummaryReport> reports = reportService.generateConsolidatedReport();
        Path txtPath = tempDir.resolve("asistencias_test.txt");

        File file = reportService.exportReportToText(reports, txtPath.toString());
        assertNotNull(file);
        assertTrue(file.exists());
        assertTrue(file.length() > 100);

        String read = Files.readString(txtPath);
        assertTrue(read.contains("Alejandro Morales"));
        assertTrue(read.contains("Ciencias"));
    }
}
