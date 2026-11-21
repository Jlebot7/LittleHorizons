package com.littlehorizons.erp.controller;

import com.littlehorizons.erp.model.academic.AcademicRecord;
import com.littlehorizons.erp.model.academic.Attendance;
import com.littlehorizons.erp.model.academic.Student;
import com.littlehorizons.erp.model.academic.Subject;
import com.littlehorizons.erp.model.report.AttendanceSummaryReport;
import com.littlehorizons.erp.service.AttendanceReportService;
import com.littlehorizons.erp.service.IAcademicService;
import com.littlehorizons.erp.service.IAttendanceReportService;
import com.littlehorizons.erp.util.ConsoleInputReader;
import com.littlehorizons.erp.view.AcademicConsoleView;
import com.littlehorizons.erp.view.IAcademicView;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class AcademicController {

    private final IAcademicService academicService;
    private final IAcademicView academicView;
    private final IAttendanceReportService attendanceReportService;

    public AcademicController(IAcademicService academicService) {
        this(academicService, new AcademicConsoleView(), new AttendanceReportService());
    }

    public AcademicController(IAcademicService academicService, IAcademicView academicView) {
        this(academicService, academicView, new AttendanceReportService());
    }

    public AcademicController(IAcademicService academicService, IAcademicView academicView, IAttendanceReportService attendanceReportService) {
        this.academicService = academicService;
        this.academicView = academicView;
        this.attendanceReportService = attendanceReportService;
    }

    public void run() {
        boolean back = false;
        while (!back) {
            academicView.showMenu();
            int option = ConsoleInputReader.readInt("Seleccione una opción: ", 0, 9);
            switch (option) {
                case 1 -> recordGrade();
                case 2 -> viewAcademicHistory();
                case 3 -> calculateStudentPeriodAverage();
                case 4 -> calculateCourseSubjectAverage();
                case 5 -> recordAttendance();
                case 6 -> showExcessiveAbsencesReport();
                case 7 -> enrollStudentInSubject();
                case 8 -> listSubjects();
                case 9 -> generateAttendanceReport();
                case 0 -> back = true;
            }
        }
    }

    private void recordGrade() {
        academicView.showRecordGradeHeader();
        int studentId = ConsoleInputReader.readInt("ID del estudiante: ", 1, 99999);
        int subjectId = ConsoleInputReader.readInt("ID de la materia: ", 1, 99999);
        int teacherId = ConsoleInputReader.readInt("ID del profesor evaluador: ", 1, 99999);
        int year = ConsoleInputReader.readInt("Año académico (ej: 2024): ", 2020, 2030);
        String period = ConsoleInputReader.readAcademicPeriod("Período académico (I / II / III / IV / Final): ", true, false);

        BigDecimal grade = ConsoleInputReader.readBigDecimal("Nota numérica (0.0 a 5.0): ",
                new BigDecimal("0.0"), new BigDecimal("5.0"));
        String obs = ConsoleInputReader.readOptionalString("Observaciones cualitativas: ");

        try {
            AcademicRecord record = new AcademicRecord(null, studentId, subjectId, teacherId, year, period, grade, obs, LocalDate.now());
            boolean saved = academicService.recordGrade(record);
            if (saved) {
                academicView.showSuccess(String.format("Calificación de %.1f registrada correctamente en período %s.", grade, period));
            } else {
                academicView.showError("Error al registrar la calificación.");
            }
        } catch (IllegalArgumentException e) {
            academicView.showError("Validación de nota fallida: " + e.getMessage());
        } catch (IllegalStateException e) {
            academicView.showError(e.getMessage());
        } catch (Exception e) {
            academicView.showError("Error al registrar la calificación: " + e.getMessage());
        }
        ConsoleInputReader.waitForEnter();
    }

    private void viewAcademicHistory() {
        academicView.showAcademicHistoryHeader();
        int studentId = ConsoleInputReader.readInt("Ingrese el ID del estudiante: ", 1, 99999);
        Optional<Student> stOpt = academicService.getStudentById(studentId);
        if (stOpt.isEmpty()) {
            academicView.showError("Estudiante no encontrado.");
            ConsoleInputReader.waitForEnter();
            return;
        }

        List<AcademicRecord> history = academicService.getAcademicHistory(studentId);
        academicView.showAcademicHistory(stOpt.get().getFullName(), history);
        ConsoleInputReader.waitForEnter();
    }

    private void calculateStudentPeriodAverage() {
        int studentId = ConsoleInputReader.readInt("Ingrese el ID del estudiante: ", 1, 99999);
        int year = ConsoleInputReader.readInt("Año académico: ", 2020, 2030);
        String period = ConsoleInputReader.readAcademicPeriod("Período (I / II / III / IV / Final): ", true, false);

        BigDecimal average = academicService.calculateStudentPeriodAverage(studentId, year, period);
        Optional<Student> st = academicService.getStudentById(studentId);
        String name = st.map(Student::getFullName).orElse("Estudiante ID " + studentId);
        academicView.showStudentPeriodAverage(name, year, period, average);
        ConsoleInputReader.waitForEnter();
    }

    private void calculateCourseSubjectAverage() {
        int subjectId = ConsoleInputReader.readInt("Ingrese el ID de la materia: ", 1, 99999);
        int year = ConsoleInputReader.readInt("Año académico: ", 2020, 2030);
        String period = ConsoleInputReader.readAcademicPeriod("Período (I / II / III / IV / Final): ", true, false);

        BigDecimal courseAvg = academicService.calculateSubjectCourseAverage(subjectId, year, period);
        academicView.showCourseSubjectAverage(subjectId, year, period, courseAvg);
        ConsoleInputReader.waitForEnter();
    }

    private void recordAttendance() {
        academicView.showRecordAttendanceHeader();
        int studentId = ConsoleInputReader.readInt("ID del estudiante: ", 1, 99999);
        int subjectId = ConsoleInputReader.readInt("ID de la materia: ", 1, 99999);
        LocalDate date = ConsoleInputReader.readDate("Fecha de asistencia");

        academicView.showAttendanceStates();
        int stOpt = ConsoleInputReader.readInt("Seleccione estado (1-4): ", 1, 4);
        String status = switch (stOpt) {
            case 1 -> "Presente";
            case 2 -> "Ausente";
            case 3 -> "Tardanza";
            default -> "Excusa Médica";
        };

        String obs = ConsoleInputReader.readOptionalString("Observaciones: ");
        int teacherId = ConsoleInputReader.readInt("ID del profesor que registra: ", 1, 99999);

        try {
            Attendance att = new Attendance(null, studentId, subjectId, date, status, obs, teacherId);
            boolean ok = academicService.registerAttendance(att);
            if (ok) {
                academicView.showSuccess("Asistencia marcada como: " + status);
            } else {
                academicView.showError("Error al registrar la asistencia.");
            }
        } catch (IllegalArgumentException e) {
            academicView.showError("Validación de asistencia fallida: " + e.getMessage());
        } catch (IllegalStateException e) {
            academicView.showError(e.getMessage());
        } catch (Exception e) {
            academicView.showError("Error al registrar asistencia: " + e.getMessage());
        }
        ConsoleInputReader.waitForEnter();
    }

    private void showExcessiveAbsencesReport() {
        long threshold = 3;
        Map<Integer, Long> report = academicService.getStudentsWithExcessiveAbsences(threshold);
        academicView.showExcessiveAbsencesReport(report, academicService::getStudentById);
        ConsoleInputReader.waitForEnter();
    }

    private void enrollStudentInSubject() {
        academicView.showEnrollSubjectHeader();
        int studentId = ConsoleInputReader.readInt("ID del estudiante: ", 1, 99999);
        int subjectId = ConsoleInputReader.readInt("ID de la materia: ", 1, 99999);
        int year = ConsoleInputReader.readInt("Año académico: ", 2020, 2030);
        String period = ConsoleInputReader.readAcademicPeriod("Período (I / II / III / IV): ", false, false);
        String section = ConsoleInputReader.readOptionalString("Sección (A/B/C): ");

        try {
            boolean ok = academicService.enrollStudentInSubject(studentId, subjectId, year, period, section.isEmpty() ? "A" : section);
            if (ok) {
                academicView.showSuccess("Estudiante inscrito satisfactoriamente en la materia.");
            } else {
                academicView.showError("Error al inscribir estudiante en la materia.");
            }
        } catch (IllegalArgumentException e) {
            academicView.showError("Validación de inscripción fallida: " + e.getMessage());
        } catch (IllegalStateException e) {
            academicView.showError(e.getMessage());
        } catch (Exception e) {
            academicView.showError("Error al inscribir estudiante: " + e.getMessage());
        }
        ConsoleInputReader.waitForEnter();
    }

    private void listSubjects() {
        List<Subject> subjects = academicService.getAllSubjects();
        academicView.showSubjects(subjects);
        ConsoleInputReader.waitForEnter();
    }

    private void generateAttendanceReport() {
        try {
            List<AttendanceSummaryReport> reports = attendanceReportService.generateConsolidatedReport();
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String destinationPath = "reportes/reporte_asistencias_" + timestamp + ".txt";
            File savedFile = attendanceReportService.exportReportToText(reports, destinationPath);
            String reportText = attendanceReportService.formatReportAsText(reports);
            academicView.showAttendanceReport(reportText, savedFile.getPath());
        } catch (Exception e) {
            academicView.showError("Error al generar reporte de asistencia: " + e.getMessage());
        }
        ConsoleInputReader.waitForEnter();
    }
}
