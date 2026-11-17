package com.littlehorizons.erp.controller;

import com.littlehorizons.erp.model.academic.Teacher;
import com.littlehorizons.erp.service.AcademicService;
import com.littlehorizons.erp.util.ConsoleInputReader;
import com.littlehorizons.erp.view.ConsoleHelper;

import java.time.LocalDate;
import java.util.List;

public class TeacherController {

    private final AcademicService academicService;

    public TeacherController(AcademicService academicService) {
        this.academicService = academicService;
    }

    public void run() {
        boolean back = false;
        while (!back) {
            ConsoleHelper.printHeader("GESTIÓN DE PROFESORES Y ASIGNACIONES", "Colegio Little Horizons");
            System.out.println("1. Registrar nuevo profesor");
            System.out.println("2. Listar todos los profesores");
            System.out.println("3. Asignar materia a profesor");
            System.out.println("0. Regresar al Menú Principal");
            System.out.println();

            int option = ConsoleInputReader.readInt("Seleccione una opción: ", 0, 3);
            switch (option) {
                case 1 -> registerTeacher();
                case 2 -> listTeachers();
                case 3 -> assignSubjectToTeacher();
                case 0 -> back = true;
            }
        }
    }

    private void registerTeacher() {
        ConsoleHelper.printSubHeader("REGISTRO DE PROFESOR");
        String docType = ConsoleInputReader.readAdultDocumentType("Tipo de documento (CC/CE/Pasaporte/PPT/PEP/VISA): ");
        String docNum = ConsoleInputReader.readDocumentNumber("Número de documento: ", docType);
        String fName = ConsoleInputReader.readPersonName("Primer nombre: ");
        String sName = ConsoleInputReader.readOptionalPersonName("Segundo nombre: ");
        String lName = ConsoleInputReader.readPersonName("Primer apellido: ");
        String sLName = ConsoleInputReader.readOptionalPersonName("Segundo apellido: ");
        String email = ConsoleInputReader.readEmail("Correo electrónico institucional: ", false);
        String phone = ConsoleInputReader.readPhone("Teléfono de contacto: ", false);
        String spec = ConsoleInputReader.readPersonName("Área de especialización docente: ");
        LocalDate hireDate = ConsoleInputReader.readDate("Fecha de contratación");

        Teacher t = new Teacher(null, docType, docNum, fName, sName, lName, sLName,
                email, phone, spec, hireDate, "Activo");

        try {
            Integer id = academicService.registerTeacher(t);
            if (id != null) {
                ConsoleHelper.printSuccess("Profesor registrado exitosamente con ID: " + id);
            } else {
                ConsoleHelper.printError("Error al registrar el profesor.");
            }
        } catch (Exception e) {
            ConsoleHelper.printError("Error al registrar el profesor: " + e.getMessage());
        }
        ConsoleInputReader.waitForEnter();
    }

    private void listTeachers() {
        ConsoleHelper.printSubHeader("DIRECTORIO GENERAL DE PROFESORES");
        List<Teacher> teachers = academicService.getAllTeachers();
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
        ConsoleInputReader.waitForEnter();
    }

    private void assignSubjectToTeacher() {
        ConsoleHelper.printSubHeader("ASIGNACIÓN DE CARGA ACADÉMICA");
        int teacherId = ConsoleInputReader.readInt("ID del profesor: ", 1, 99999);
        int subjectId = ConsoleInputReader.readInt("ID de la materia a asignar: ", 1, 99999);
        int year = ConsoleInputReader.readInt("Año académico: ", 2020, 2030);
        String section = ConsoleInputReader.readOptionalString("Sección / Grupo (ej: A, B): ");

        boolean ok = academicService.assignTeacherToSubject(teacherId, subjectId, year, section.isEmpty() ? "A" : section);
        if (ok) {
            ConsoleHelper.printSuccess("Asignación académica registrada exitosamente.");
        } else {
            ConsoleHelper.printError("Error al registrar la asignación.");
        }
        ConsoleInputReader.waitForEnter();
    }
}
