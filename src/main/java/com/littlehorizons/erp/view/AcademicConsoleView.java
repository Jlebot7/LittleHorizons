package com.littlehorizons.erp.view;

import com.littlehorizons.erp.model.academic.AcademicRecord;
import com.littlehorizons.erp.model.academic.Student;
import com.littlehorizons.erp.model.academic.Subject;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class AcademicConsoleView implements IAcademicView {

    @Override
    public void showMenu() {
        ConsoleHelper.printHeader("MÓDULO DE GESTIÓN ACADÉMICA", "Colegio Little Horizons");
        System.out.println("1. Registrar calificación / nota a estudiante");
        System.out.println("2. Consultar historial académico de un estudiante");
        System.out.println("3. Calcular promedio general del estudiante en período (Stream API)");
        System.out.println("4. Calcular promedio de curso por materia y período (Stream API)");
        System.out.println("5. Registrar asistencia diaria de estudiante");
        System.out.println("6. Reporte de estudiantes con inasistencias preocupantes (>=3)");
        System.out.println("7. Inscribir estudiante en una materia");
        System.out.println("8. Listar catálogo de materias");
        System.out.println("9. Generar y exportar reporte consolidado de asistencia (TXT con Stream API)");
        System.out.println("0. Regresar al Menú Principal");
        System.out.println();
    }

    @Override
    public void showRecordGradeHeader() {
        ConsoleHelper.printSubHeader("REGISTRO DE CALIFICACIONES (ESCALA 0.0 A 5.0)");
    }

    @Override
    public void showAcademicHistoryHeader() {
        ConsoleHelper.printSubHeader("HISTORIAL ACADÉMICO DEL ESTUDIANTE");
    }

    @Override
    public void showAcademicHistory(String studentName, List<AcademicRecord> history) {
        System.out.println("Historial de notas para: " + studentName);
        if (history.isEmpty()) {
            ConsoleHelper.printWarning("El estudiante no registra notas cargadas en el sistema.");
        } else {
            System.out.printf("%-6s | %-6s | %-8s | %-28s | %-6s | %-20s%n", "AÑO", "PER", "CÓDIGO", "MATERIA", "NOTA", "DOCENTE");
            System.out.println("─".repeat(85));
            for (AcademicRecord r : history) {
                System.out.printf("%-6d | %-6s | %-8s | %-28s | %-6s | %-20s%n",
                        r.getAcademicYear(), r.getAcademicPeriod(),
                        "SUB-" + r.getIdSubject(),
                        r.getSubjectName() != null ? r.getSubjectName() : ("ID " + r.getIdSubject()),
                        r.getGradeValue() != null ? r.getGradeValue().toString() : "-",
                        r.getTeacherName() != null ? r.getTeacherName() : ("ID " + r.getIdTeacher()));
            }
            System.out.println("─".repeat(85));
        }
    }

    @Override
    public void showStudentPeriodAverage(String studentName, int year, String period, BigDecimal average) {
        System.out.println();
        System.out.println("  ══════════════════════════════════════════════════");
        System.out.printf("  Estudiante        : %s%n", studentName);
        System.out.printf("  Año / Período     : %d - Período %s%n", year, period);
        System.out.printf("  Promedio Ponderado: %.2f / 5.00%n", average);
        if (average.compareTo(new BigDecimal("3.5")) >= 0) {
            System.out.println("  Estado Desempeño  : [APROBADO]");
        } else if (average.compareTo(BigDecimal.ZERO) == 0) {
            System.out.println("  Estado Desempeño  : [SIN REGISTROS]");
        } else {
            System.out.println("  Estado Desempeño  : [EN RIESGO ACADÉMICO / REPROBADO]");
        }
        System.out.println("  ══════════════════════════════════════════════════");
    }

    @Override
    public void showCourseSubjectAverage(int subjectId, int year, String period, BigDecimal courseAvg) {
        System.out.println();
        System.out.println("  ══════════════════════════════════════════════════");
        System.out.printf("  Materia ID        : %d%n", subjectId);
        System.out.printf("  Año / Período     : %d - Período %s%n", year, period);
        System.out.printf("  Promedio del Grupo: %.2f / 5.00%n", courseAvg);
        System.out.println("  ══════════════════════════════════════════════════");
    }

    @Override
    public void showRecordAttendanceHeader() {
        ConsoleHelper.printSubHeader("CONTROL DIARIO DE ASISTENCIA");
    }

    @Override
    public void showAttendanceStates() {
        System.out.println("Estados de asistencia:");
        System.out.println("  1. Presente\n  2. Ausente\n  3. Tardanza\n  4. Excusa Médica");
    }

    @Override
    public void showExcessiveAbsencesReport(Map<Integer, Long> absences, Function<Integer, Optional<Student>> studentResolver) {
        if (absences.isEmpty()) {
            ConsoleHelper.printSuccess("Excelente: No hay estudiantes que superen el límite de 3 ausencias registradas.");
        } else {
            System.out.println("Estudiantes con 3 o más inasistencias injustificadas:");
            System.out.printf("%-13s | %-35s | %-15s%n", "ID ESTUDIANTE", "NOMBRE COMPLETO", "TOTAL AUSENCIAS");
            System.out.println("─".repeat(65));
            absences.forEach((id, count) -> {
                Optional<Student> st = studentResolver.apply(id);
                String name = st.map(Student::getFullName).orElse("Desconocido");
                System.out.printf("%-13d | %-35s | %-15d%n", id, name, count);
            });
            System.out.println("─".repeat(65));
        }
    }

    @Override
    public void showEnrollSubjectHeader() {
        ConsoleHelper.printSubHeader("INSCRIPCIÓN DE ESTUDIANTE EN MATERIA");
    }

    @Override
    public void showSubjects(List<Subject> subjects) {
        ConsoleHelper.printSubHeader("CATÁLOGO DE MATERIAS POR GRADO");
        if (subjects.isEmpty()) {
            ConsoleHelper.printWarning("No hay materias registradas.");
        } else {
            System.out.printf("%-5s | %-12s | %-32s | %-6s | %-6s%n", "ID", "CÓDIGO", "NOMBRE MATERIA", "GRADO", "H/SEM");
            System.out.println("─".repeat(75));
            for (Subject s : subjects) {
                System.out.printf("%-5d | %-12s | %-32s | %-6d | %-6d%n",
                        s.getIdSubject(), s.getSubjectCode(), s.getSubjectName(), s.getIdGrade(), s.getWeeklyHours());
            }
            System.out.println("─".repeat(75));
        }
    }

    @Override
    public void showAttendanceReport(String reportText, String savedFilePath) {
        ConsoleHelper.printSubHeader("REPORTE CONSOLIDADO DE ASISTENCIAS");
        System.out.println(reportText);
        if (savedFilePath != null && !savedFilePath.isBlank()) {
            ConsoleHelper.printSuccess("Archivo TXT generado exitosamente en: " + savedFilePath);
        }
    }

    @Override
    public void showSuccess(String message) {
        ConsoleHelper.printSuccess(message);
    }

    @Override
    public void showError(String message) {
        ConsoleHelper.printError(message);
    }

    @Override
    public void showWarning(String message) {
        ConsoleHelper.printWarning(message);
    }
}
