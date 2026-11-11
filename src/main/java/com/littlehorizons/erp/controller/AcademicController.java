package com.littlehorizons.erp.controller;

import com.littlehorizons.erp.model.academic.*;
import com.littlehorizons.erp.service.AcademicService;
import com.littlehorizons.erp.util.ConsoleInputReader;
import com.littlehorizons.erp.view.ConsoleHelper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class AcademicController {

    private final AcademicService academicService;

    public AcademicController(AcademicService academicService) {
        this.academicService = academicService;
    }

    public void run() {
        boolean back = false;
        while (!back) {
            ConsoleHelper.printHeader("MÓDULO DE GESTIÓN ACADÉMICA", "Colegio Little Horizons");
            System.out.println("1. Registrar calificación / nota a estudiante");
            System.out.println("2. Consultar historial académico de un estudiante");
            System.out.println("3. Calcular promedio general del estudiante en período (Stream API)");
            System.out.println("4. Calcular promedio de curso por materia y período (Stream API)");
            System.out.println("5. Registrar asistencia diaria de estudiante");
            System.out.println("6. Reporte de estudiantes con inasistencias preocupantes (>=3)");
            System.out.println("7. Inscribir estudiante en una materia");
            System.out.println("8. Listar catálogo de materias");
            System.out.println("0. Regresar al Menú Principal");
            System.out.println();

            int option = ConsoleInputReader.readInt("Seleccione una opción: ", 0, 8);
            switch (option) {
                case 1 -> recordGrade();
                case 2 -> viewAcademicHistory();
                case 3 -> calculateStudentPeriodAverage();
                case 4 -> calculateCourseSubjectAverage();
                case 5 -> recordAttendance();
                case 6 -> showExcessiveAbsencesReport();
                case 7 -> enrollStudentInSubject();
                case 8 -> listSubjects();
                case 0 -> back = true;
            }
        }
    }

    private void recordGrade() {
        ConsoleHelper.printSubHeader("REGISTRO DE CALIFICACIONES (ESCALA 0.0 A 5.0)");
        int studentId = ConsoleInputReader.readInt("ID del estudiante: ", 1, 99999);
        int subjectId = ConsoleInputReader.readInt("ID de la materia: ", 1, 99999);
        int teacherId = ConsoleInputReader.readInt("ID del profesor evaluador: ", 1, 99999);
        int year = ConsoleInputReader.readInt("Año académico (ej: 2024): ", 2020, 2030);
        String period = ConsoleInputReader.readString("Período académico (I / II / III / IV / Final): ").toUpperCase();

        BigDecimal grade = ConsoleInputReader.readBigDecimal("Nota numérica (0.0 a 5.0): ",
                new BigDecimal("0.0"), new BigDecimal("5.0"));
        String obs = ConsoleInputReader.readOptionalString("Observaciones cualitativas: ");

        AcademicRecord record = new AcademicRecord(null, studentId, subjectId, teacherId, year, period, grade, obs, LocalDate.now());
        boolean saved = academicService.recordGrade(record);
        if (saved) {
            ConsoleHelper.printSuccess(String.format("Calificación de %.1f registrada correctamente en período %s.", grade, period));
        } else {
            ConsoleHelper.printError("Error al registrar la calificación.");
        }
        ConsoleInputReader.waitForEnter();
    }

    private void viewAcademicHistory() {
        ConsoleHelper.printSubHeader("HISTORIAL ACADÉMICO DEL ESTUDIANTE");
        int studentId = ConsoleInputReader.readInt("Ingrese el ID del estudiante: ", 1, 99999);
        Optional<Student> stOpt = academicService.getStudentById(studentId);
        if (stOpt.isEmpty()) {
            ConsoleHelper.printError("Estudiante no encontrado.");
            ConsoleInputReader.waitForEnter();
            return;
        }

        System.out.println("Historial de notas para: " + stOpt.get().getFullName());
        List<AcademicRecord> history = academicService.getAcademicHistory(studentId);
        if (history.isEmpty()) {
            ConsoleHelper.printWarning("El estudiante no registra notas cargadas en el sistema.");
        } else {
            System.out.printf("%-6s | %-6s | %-8s | %-28s | %-6s | %-20s%n", "AÑO", "PER", "CÓDIGO", "MATERIA", "NOTA", "DOCENTE");
            System.out.println("─".repeat(85));
            for (AcademicRecord r : history) {
                System.out.printf("%-6d | %-6s | %-8s | %-28s | %-6.1f | %-20s%n",
                        r.getAcademicYear(), r.getAcademicPeriod(),
                        "SUB-" + r.getIdSubject(),
                        r.getSubjectName() != null ? r.getSubjectName() : ("ID " + r.getIdSubject()),
                        r.getGradeValue(),
                        r.getTeacherName() != null ? r.getTeacherName() : ("ID " + r.getIdTeacher()));
            }
            System.out.println("─".repeat(85));
        }
        ConsoleInputReader.waitForEnter();
    }

    private void calculateStudentPeriodAverage() {
        ConsoleHelper.printSubHeader("CÁLCULO AUTOMÁTICO DE PROMEDIO GENERAL (STREAM API)");
        int studentId = ConsoleInputReader.readInt("Ingrese el ID del estudiante: ", 1, 99999);
        int year = ConsoleInputReader.readInt("Año académico: ", 2020, 2030);
        String period = ConsoleInputReader.readString("Período (I / II / III / IV / Final): ").toUpperCase();

        BigDecimal average = academicService.calculateStudentPeriodAverage(studentId, year, period);
        Optional<Student> st = academicService.getStudentById(studentId);
        String name = st.map(Student::getFullName).orElse("Estudiante ID " + studentId);

        System.out.println();
        System.out.println("  ══════════════════════════════════════════════════");
        System.out.printf("  Estudiante        : %s%n", name);
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
        ConsoleInputReader.waitForEnter();
    }

    private void calculateCourseSubjectAverage() {
        ConsoleHelper.printSubHeader("CÁLCULO DE PROMEDIO DE CURSO POR MATERIA (STREAM API)");
        int subjectId = ConsoleInputReader.readInt("Ingrese el ID de la materia: ", 1, 99999);
        int year = ConsoleInputReader.readInt("Año académico: ", 2020, 2030);
        String period = ConsoleInputReader.readString("Período (I / II / III / IV / Final): ").toUpperCase();

        BigDecimal courseAvg = academicService.calculateSubjectCourseAverage(subjectId, year, period);
        System.out.println();
        System.out.println("  ══════════════════════════════════════════════════");
        System.out.printf("  Materia ID        : %d%n", subjectId);
        System.out.printf("  Año / Período     : %d - Período %s%n", year, period);
        System.out.printf("  Promedio del Grupo: %.2f / 5.00%n", courseAvg);
        System.out.println("  ══════════════════════════════════════════════════");
        ConsoleInputReader.waitForEnter();
    }

    private void recordAttendance() {
        ConsoleHelper.printSubHeader("CONTROL DIARIO DE ASISTENCIA");
        int studentId = ConsoleInputReader.readInt("ID del estudiante: ", 1, 99999);
        int subjectId = ConsoleInputReader.readInt("ID de la materia: ", 1, 99999);
        LocalDate date = ConsoleInputReader.readDate("Fecha de asistencia");

        System.out.println("Estados de asistencia:");
        System.out.println("  1. Presente\n  2. Ausente\n  3. Tardanza\n  4. Excusa Médica");
        int stOpt = ConsoleInputReader.readInt("Seleccione estado (1-4): ", 1, 4);
        String status = switch (stOpt) {
            case 1 -> "Presente";
            case 2 -> "Ausente";
            case 3 -> "Tardanza";
            default -> "Excusa Médica";
        };

        String obs = ConsoleInputReader.readOptionalString("Observaciones: ");
        int teacherId = ConsoleInputReader.readInt("ID del profesor que registra: ", 1, 99999);

        Attendance att = new Attendance(null, studentId, subjectId, date, status, obs, teacherId);
        boolean ok = academicService.registerAttendance(att);
        if (ok) {
            ConsoleHelper.printSuccess("Asistencia marcada como: " + status);
        } else {
            ConsoleHelper.printError("Error al registrar la asistencia.");
        }
        ConsoleInputReader.waitForEnter();
    }

    private void showExcessiveAbsencesReport() {
        ConsoleHelper.printSubHeader("REPORTE DE ESTUDIANTES CON INASISTENCIAS PREOCUPANTES");
        long threshold = 3;
        Map<Integer, Long> report = academicService.getStudentsWithExcessiveAbsences(threshold);

        if (report.isEmpty()) {
            ConsoleHelper.printSuccess("Excelente: No hay estudiantes que superen el límite de 3 ausencias registradas.");
        } else {
            System.out.println("Estudiantes con 3 o más inasistencias injustificadas:");
            System.out.printf("%-10s | %-35s | %-15s%n", "ID ESTUDIANTE", "NOMBRE COMPLETO", "TOTAL AUSENCIAS");
            System.out.println("─".repeat(65));
            report.forEach((id, count) -> {
                Optional<Student> st = academicService.getStudentById(id);
                String name = st.map(Student::getFullName).orElse("Desconocido");
                System.out.printf("%-13d | %-35s | %-15d%n", id, name, count);
            });
            System.out.println("─".repeat(65));
        }
        ConsoleInputReader.waitForEnter();
    }

    private void enrollStudentInSubject() {
        ConsoleHelper.printSubHeader("INSCRIPCIÓN DE ESTUDIANTE EN MATERIA");
        int studentId = ConsoleInputReader.readInt("ID del estudiante: ", 1, 99999);
        int subjectId = ConsoleInputReader.readInt("ID de la materia: ", 1, 99999);
        int year = ConsoleInputReader.readInt("Año académico: ", 2020, 2030);
        String period = ConsoleInputReader.readString("Período (I / II / III / IV): ").toUpperCase();
        String section = ConsoleInputReader.readOptionalString("Sección (A/B/C): ");

        boolean ok = academicService.enrollStudentInSubject(studentId, subjectId, year, period, section.isEmpty() ? "A" : section);
        if (ok) {
            ConsoleHelper.printSuccess("Estudiante inscrito satisfactoriamente en la materia.");
        } else {
            ConsoleHelper.printError("Error al inscribir estudiante en la materia.");
        }
        ConsoleInputReader.waitForEnter();
    }

    private void listSubjects() {
        ConsoleHelper.printSubHeader("CATÁLOGO DE MATERIAS POR GRADO");
        List<Subject> subjects = academicService.getAllSubjects();
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
        ConsoleInputReader.waitForEnter();
    }
}
