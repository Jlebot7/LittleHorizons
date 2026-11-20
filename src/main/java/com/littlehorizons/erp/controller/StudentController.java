package com.littlehorizons.erp.controller;

import com.littlehorizons.erp.model.academic.AcademicGrade;
import com.littlehorizons.erp.model.academic.Guardian;
import com.littlehorizons.erp.model.academic.Student;
import com.littlehorizons.erp.service.IAcademicService;
import com.littlehorizons.erp.util.ConsoleInputReader;
import com.littlehorizons.erp.view.IStudentView;
import com.littlehorizons.erp.view.StudentConsoleView;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class StudentController {

    private final IAcademicService academicService;
    private final IStudentView studentView;

    public StudentController(IAcademicService academicService) {
        this(academicService, new StudentConsoleView());
    }

    public StudentController(IAcademicService academicService, IStudentView studentView) {
        this.academicService = academicService;
        this.studentView = studentView;
    }

    public void run() {
        boolean back = false;
        while (!back) {
            studentView.showMenu();
            int option = ConsoleInputReader.readInt("Seleccione una opción: ", 0, 5);
            switch (option) {
                case 1 -> registerNewStudent();
                case 2 -> listStudents();
                case 3 -> searchStudent();
                case 4 -> registerAndLinkGuardian();
                case 5 -> enrollInGrade();
                case 0 -> back = true;
            }
        }
    }

    private void registerNewStudent() {
        studentView.showRegisterStudentHeader();

        String docType = ConsoleInputReader.readStudentDocumentType("Tipo de documento (RC/TI/CC/CE/Pasaporte/PPT/PEP/NES/VISA): ");
        String docNumber = ConsoleInputReader.readDocumentNumber("Número de documento: ", docType);
        String firstName = ConsoleInputReader.readPersonName("Primer nombre: ");
        String secondName = ConsoleInputReader.readOptionalPersonName("Segundo nombre (opcional): ");
        String firstLastName = ConsoleInputReader.readPersonName("Primer apellido: ");
        String secondLastName = ConsoleInputReader.readOptionalPersonName("Segundo apellido (opcional): ");
        LocalDate birthDate = ConsoleInputReader.readStudentBirthDate("Fecha de nacimiento");
        String gender = ConsoleInputReader.readGender("Género (M/F/Otro): ");
        String address = ConsoleInputReader.readString("Dirección residencial: ");
        String phone = ConsoleInputReader.readPhone("Teléfono de contacto: ", false);
        String email = ConsoleInputReader.readEmail("Correo electrónico: ", false);
        String bloodType = ConsoleInputReader.readBloodType("Tipo de sangre (ej: O+, A+): ", true);
        String allergies = ConsoleInputReader.readOptionalString("Alergias conocidas: ");
        String medicalConditions = ConsoleInputReader.readOptionalString("Condiciones médicas especiales: ");

        Student student = new Student(null, docType, docNumber, firstName, secondName,
                firstLastName, secondLastName, birthDate, gender, address, phone, email,
                bloodType, allergies, medicalConditions, LocalDate.now(), "Activo");

        try {
            Integer id = academicService.registerStudent(student);
            if (id != null) {
                studentView.showSuccess("Estudiante registrado exitosamente con ID interno: " + id);
            } else {
                studentView.showError("No se pudo completar el registro del estudiante en el sistema.");
            }
        } catch (IllegalArgumentException e) {
            studentView.showError("Validación de campo fallida: " + e.getMessage());
        } catch (IllegalStateException e) {
            studentView.showError(e.getMessage());
        } catch (Exception e) {
            studentView.showError("Error inesperado al registrar el estudiante: " + e.getMessage());
        }
        ConsoleInputReader.waitForEnter();
    }

    private void listStudents() {
        List<Student> students = academicService.getAllStudents();
        studentView.showStudents(students);
        ConsoleInputReader.waitForEnter();
    }

    private void searchStudent() {
        studentView.showSearchStudentHeader();
        String docNumber = ConsoleInputReader.readString("Ingrese el número de documento a buscar: ");
        Optional<Student> studentOpt = academicService.getStudentByDocument(docNumber);

        if (studentOpt.isPresent()) {
            studentView.showStudentDetails(studentOpt.get());
        } else {
            studentView.showStudentNotFound(docNumber);
        }
        ConsoleInputReader.waitForEnter();
    }

    private void registerAndLinkGuardian() {
        studentView.showRegisterGuardianHeader();
        int studentId = ConsoleInputReader.readInt("Ingrese el ID del estudiante a vincular: ", 1, 99999);
        Optional<Student> stOpt = academicService.getStudentById(studentId);
        if (stOpt.isEmpty()) {
            studentView.showError("El estudiante con ID " + studentId + " no existe.");
            ConsoleInputReader.waitForEnter();
            return;
        }

        studentView.showLinkGuardianPrompt(stOpt.get().getFullName());
        String docType = ConsoleInputReader.readAdultDocumentType("Tipo documento acudiente (CC/CE/Pasaporte/PPT/PEP/VISA): ");
        String docNum = ConsoleInputReader.readDocumentNumber("Número de documento: ", docType);
        String fName = ConsoleInputReader.readPersonName("Primer nombre: ");
        String sName = ConsoleInputReader.readOptionalPersonName("Segundo nombre: ");
        String lName = ConsoleInputReader.readPersonName("Primer apellido: ");
        String sLName = ConsoleInputReader.readOptionalPersonName("Segundo apellido: ");
        String rel = ConsoleInputReader.readString("Parentesco (Padre/Madre/Tutor Legal/Abuelo/Tío): ");
        String mainPhone = ConsoleInputReader.readPhone("Teléfono principal: ", false);
        String secPhone = ConsoleInputReader.readPhone("Teléfono secundario (opcional): ", true);
        String email = ConsoleInputReader.readEmail("Correo electrónico: ", false);
        String address = ConsoleInputReader.readString("Dirección: ");
        String occ = ConsoleInputReader.readOptionalString("Ocupación: ");
        boolean finResp = ConsoleInputReader.readBoolean("¿Es responsable financiero de pagos?");
        boolean isPrimary = ConsoleInputReader.readBoolean("¿Es el acudiente principal para comunicaciones?");

        Guardian g = new Guardian(null, docType, docNum, fName, sName, lName, sLName, rel,
                mainPhone, secPhone, email, address, occ, finResp);

        try {
            Integer gId = academicService.registerGuardian(g);
            if (gId != null) {
                boolean linked = academicService.linkStudentToGuardian(studentId, gId, isPrimary, true);
                if (linked) {
                    studentView.showSuccess("Acudiente registrado (ID " + gId + ") y vinculado correctamente al estudiante.");
                } else {
                    studentView.showWarning("Acudiente registrado pero ocurrió un error al vincularlo.");
                }
            } else {
                studentView.showError("No se pudo registrar el acudiente.");
            }
        } catch (IllegalArgumentException e) {
            studentView.showError("Validación fallida: " + e.getMessage());
        } catch (IllegalStateException e) {
            studentView.showError(e.getMessage());
        } catch (Exception e) {
            studentView.showError("Error al registrar acudiente: " + e.getMessage());
        }
        ConsoleInputReader.waitForEnter();
    }

    private void enrollInGrade() {
        studentView.showEnrollGradeHeader();
        int studentId = ConsoleInputReader.readInt("Ingrese el ID del estudiante: ", 1, 99999);
        Optional<Student> stOpt = academicService.getStudentById(studentId);
        if (stOpt.isEmpty()) {
            studentView.showError("Estudiante no encontrado.");
            ConsoleInputReader.waitForEnter();
            return;
        }

        List<AcademicGrade> grades = academicService.getAllGrades();
        studentView.showAvailableGrades(grades);

        int gradeId = ConsoleInputReader.readInt("Seleccione el ID del grado: ", 1, grades.size());
        int year = ConsoleInputReader.readInt("Año académico (ej: 2024): ", 2020, 2030);

        try {
            boolean enrolled = academicService.enrollStudentInGrade(studentId, gradeId, year);
            if (enrolled) {
                studentView.showSuccess(String.format("Estudiante '%s' matriculado exitosamente en el grado seleccionado para el año %d.",
                        stOpt.get().getFullName(), year));
            } else {
                studentView.showError("Error al procesar la matrícula.");
            }
        } catch (Exception e) {
            studentView.showError("Error al matricular: " + e.getMessage());
        }
        ConsoleInputReader.waitForEnter();
    }
}
