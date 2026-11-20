package com.littlehorizons.erp.controller;

import com.littlehorizons.erp.model.academic.Teacher;
import com.littlehorizons.erp.service.IAcademicService;
import com.littlehorizons.erp.util.ConsoleInputReader;
import com.littlehorizons.erp.view.ITeacherView;
import com.littlehorizons.erp.view.TeacherConsoleView;

import java.time.LocalDate;
import java.util.List;

public class TeacherController {

    private final IAcademicService academicService;
    private final ITeacherView teacherView;

    public TeacherController(IAcademicService academicService) {
        this(academicService, new TeacherConsoleView());
    }

    public TeacherController(IAcademicService academicService, ITeacherView teacherView) {
        this.academicService = academicService;
        this.teacherView = teacherView;
    }

    public void run() {
        boolean back = false;
        while (!back) {
            teacherView.showMenu();
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
        teacherView.showRegisterTeacherHeader();
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
                teacherView.showSuccess("Profesor registrado exitosamente con ID: " + id);
            } else {
                teacherView.showError("Error al registrar el profesor.");
            }
        } catch (IllegalArgumentException e) {
            teacherView.showError("Validación de campo fallida: " + e.getMessage());
        } catch (IllegalStateException e) {
            teacherView.showError(e.getMessage());
        } catch (Exception e) {
            teacherView.showError("Error al registrar el profesor: " + e.getMessage());
        }
        ConsoleInputReader.waitForEnter();
    }

    private void listTeachers() {
        List<Teacher> teachers = academicService.getAllTeachers();
        teacherView.showTeachers(teachers);
        ConsoleInputReader.waitForEnter();
    }

    private void assignSubjectToTeacher() {
        teacherView.showAssignSubjectHeader();
        int teacherId = ConsoleInputReader.readInt("ID del profesor: ", 1, 99999);
        int subjectId = ConsoleInputReader.readInt("ID de la materia a asignar: ", 1, 99999);
        int year = ConsoleInputReader.readInt("Año académico: ", 2020, 2030);
        String section = ConsoleInputReader.readOptionalString("Sección / Grupo (ej: A, B): ");

        try {
            boolean ok = academicService.assignTeacherToSubject(teacherId, subjectId, year, section.isEmpty() ? "A" : section);
            if (ok) {
                teacherView.showSuccess("Asignación académica registrada exitosamente.");
            } else {
                teacherView.showError("Error al registrar la asignación.");
            }
        } catch (IllegalArgumentException e) {
            teacherView.showError("Validación fallida: " + e.getMessage());
        } catch (IllegalStateException e) {
            teacherView.showError(e.getMessage());
        } catch (Exception e) {
            teacherView.showError("Error inesperado en asignación: " + e.getMessage());
        }
        ConsoleInputReader.waitForEnter();
    }
}
