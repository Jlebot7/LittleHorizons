package com.littlehorizons.erp.view;

import com.littlehorizons.erp.model.academic.AcademicGrade;
import com.littlehorizons.erp.model.academic.Student;

import java.util.List;

public class StudentConsoleView implements IStudentView {

    @Override
    public void showMenu() {
        ConsoleHelper.printHeader("GESTIÓN DE ESTUDIANTES Y ACUDIENTES", "Colegio Little Horizons");
        System.out.println("1. Registrar nuevo estudiante");
        System.out.println("2. Listar todos los estudiantes");
        System.out.println("3. Consultar estudiante por número de documento");
        System.out.println("4. Registrar acudiente y vincular a estudiante");
        System.out.println("5. Matricular estudiante en un grado");
        System.out.println("0. Regresar al Menú Principal");
        System.out.println();
    }

    @Override
    public void showRegisterStudentHeader() {
        ConsoleHelper.printSubHeader("REGISTRO DE NUEVO ESTUDIANTE");
    }

    @Override
    public void showStudents(List<Student> students) {
        ConsoleHelper.printSubHeader("DIRECTORIO GENERAL DE ESTUDIANTES");
        if (students.isEmpty()) {
            ConsoleHelper.printWarning("No hay estudiantes registrados en la base de datos.");
        } else {
            System.out.printf("%-6s | %-12s | %-30s | %-12s | %-10s%n", "ID", "DOCUMENTO", "NOMBRE COMPLETO", "TELÉFONO", "ESTADO");
            System.out.println("─".repeat(80));
            for (Student s : students) {
                System.out.printf("%-6d | %-4s %-7s | %-30s | %-12s | %-10s%n",
                        s.getIdStudent(), s.getDocumentType(), s.getDocumentNumber(),
                        s.getFullName(), s.getPhone() != null ? s.getPhone() : "N/A", s.getStatus());
            }
            System.out.println("─".repeat(80));
            System.out.println("Total de estudiantes listados: " + students.size());
        }
    }

    @Override
    public void showSearchStudentHeader() {
        ConsoleHelper.printSubHeader("CONSULTA DE ESTUDIANTE");
    }

    @Override
    public void showStudentDetails(Student s) {
        System.out.println("\n  Información del Estudiante:");
        System.out.println("  ---------------------------");
        System.out.println("  ID Interno        : " + s.getIdStudent());
        System.out.println("  Nombre Completo   : " + s.getFullName());
        System.out.println("  Documento         : " + s.getDocumentType() + " " + s.getDocumentNumber());
        System.out.println("  Fecha Nacimiento  : " + s.getBirthDate());
        System.out.println("  Género            : " + s.getGender());
        System.out.println("  Dirección         : " + s.getAddress());
        System.out.println("  Teléfono          : " + s.getPhone());
        System.out.println("  Correo Electrónico: " + s.getEmail());
        System.out.println("  Tipo de Sangre    : " + s.getBloodType());
        System.out.println("  Alergias          : " + (s.getAllergies() != null ? s.getAllergies() : "Ninguna"));
        System.out.println("  Condiciones Méd.  : " + (s.getMedicalConditions() != null ? s.getMedicalConditions() : "Ninguna"));
        System.out.println("  Fecha de Ingreso  : " + s.getAdmissionDate());
        System.out.println("  Estado Actual     : " + s.getStatus());
    }

    @Override
    public void showStudentNotFound(String docNumber) {
        ConsoleHelper.printWarning("No se encontró ningún estudiante con el documento: " + docNumber);
    }

    @Override
    public void showRegisterGuardianHeader() {
        ConsoleHelper.printSubHeader("REGISTRO Y VINCULACIÓN DE ACUDIENTE");
    }

    @Override
    public void showLinkGuardianPrompt(String studentName) {
        System.out.println("Vinculando acudiente para: " + studentName);
    }

    @Override
    public void showEnrollGradeHeader() {
        ConsoleHelper.printSubHeader("MATRÍCULA DE ESTUDIANTE EN GRADO ACADÉMICO");
    }

    @Override
    public void showAvailableGrades(List<AcademicGrade> grades) {
        System.out.println("\nGrados Académicos Disponibles:");
        for (AcademicGrade gr : grades) {
            System.out.printf("  [%2d] %-15s (Nivel: %d)%n", gr.getIdGrade(), gr.getGradeName(), gr.getNumericLevel());
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
