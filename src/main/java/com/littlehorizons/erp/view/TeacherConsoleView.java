package com.littlehorizons.erp.view;

import com.littlehorizons.erp.model.academic.Teacher;

import java.util.List;

public class TeacherConsoleView implements ITeacherView {

    @Override
    public void showMenu() {
        ConsoleHelper.printHeader("GESTIÓN DE PROFESORES Y CARGA ACADÉMICA", "Colegio Little Horizons");
        System.out.println("1. Registrar nuevo profesor");
        System.out.println("2. Directorio general de profesores");
        System.out.println("3. Asignar carga académica (Materia a profesor)");
        System.out.println("0. Regresar al Menú Principal");
        System.out.println();
    }

    @Override
    public void showRegisterTeacherHeader() {
        ConsoleHelper.printSubHeader("REGISTRO DE PROFESOR");
    }

    @Override
    public void showTeachers(List<Teacher> teachers) {
        ConsoleHelper.printSubHeader("DIRECTORIO GENERAL DE PROFESORES");
        if (teachers.isEmpty()) {
            ConsoleHelper.printWarning("No hay profesores registrados.");
        } else {
            System.out.printf("%-5s | %-12s | %-28s | %-30s | %-25s%n", "ID", "DOCUMENTO", "NOMBRE COMPLETO", "ESPECIALIZACIÓN", "CORREO");
            System.out.println("─".repeat(110));
            for (Teacher t : teachers) {
                System.out.printf("%-5d | %-4s %-7s | %-28s | %-30s | %-25s%n",
                        t.getIdTeacher(), t.getDocumentType(), t.getDocumentNumber(),
                        t.getFullName(), t.getSpecialization(), t.getEmail());
            }
            System.out.println("─".repeat(110));
            System.out.println("Total de docentes activos: " + teachers.size());
        }
    }

    @Override
    public void showAssignSubjectHeader() {
        ConsoleHelper.printSubHeader("ASIGNACIÓN DE CARGA ACADÉMICA");
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
