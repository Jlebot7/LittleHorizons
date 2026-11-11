package com.littlehorizons.erp.controller;

import com.littlehorizons.erp.model.academic.AcademicGrade;
import com.littlehorizons.erp.model.academic.Guardian;
import com.littlehorizons.erp.model.academic.Student;
import com.littlehorizons.erp.service.AcademicService;
import com.littlehorizons.erp.util.ConsoleInputReader;
import com.littlehorizons.erp.view.ConsoleHelper;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Controller for Student Management and Guardianship operations.
 */
public class StudentController {

    private final AcademicService academicService;

    public StudentController(AcademicService academicService) {
        this.academicService = academicService;
    }

    public void run() {
        boolean back = false;
        while (!back) {
            ConsoleHelper.printHeader("GESTIÓN DE ESTUDIANTES Y ACUDIENTES", "Colegio Little Horizons");
            System.out.println("1. Registrar nuevo estudiante");
            System.out.println("2. Listar todos los estudiantes");
            System.out.println("3. Consultar estudiante por número de documento");
            System.out.println("4. Registrar acudiente y vincular a estudiante");
            System.out.println("5. Matricular estudiante en un grado");
            System.out.println("0. Regresar al Menú Principal");
            System.out.println();

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
        ConsoleHelper.printSubHeader("REGISTRO DE NUEVO ESTUDIANTE");

        String docType = ConsoleInputReader.readString("Tipo de documento (TI/CC/CE/Pasaporte): ").toUpperCase();
        String docNumber = ConsoleInputReader.readString("Número de documento: ");
        String firstName = ConsoleInputReader.readString("Primer nombre: ");
        String secondName = ConsoleInputReader.readOptionalString("Segundo nombre (opcional): ");
        String firstLastName = ConsoleInputReader.readString("Primer apellido: ");
        String secondLastName = ConsoleInputReader.readOptionalString("Segundo apellido (opcional): ");
        LocalDate birthDate = ConsoleInputReader.readDate("Fecha de nacimiento");
        String gender = ConsoleInputReader.readString("Género (M/F/Otro): ").toUpperCase();
        String address = ConsoleInputReader.readString("Dirección residencial: ");
        String phone = ConsoleInputReader.readString("Teléfono de contacto: ");
        String email = ConsoleInputReader.readString("Correo electrónico: ");
        String bloodType = ConsoleInputReader.readOptionalString("Tipo de sangre (ej: O+, A+): ");
        String allergies = ConsoleInputReader.readOptionalString("Alergias conocidas: ");
        String medicalConditions = ConsoleInputReader.readOptionalString("Condiciones médicas especiales: ");

        Student student = new Student(null, docType, docNumber, firstName, secondName,
                firstLastName, secondLastName, birthDate, gender, address, phone, email,
                bloodType, allergies, medicalConditions, LocalDate.now(), "Activo");

        try {
            Integer id = academicService.registerStudent(student);
            if (id != null) {
                ConsoleHelper.printSuccess("Estudiante registrado exitosamente con ID interno: " + id);
            } else {
                ConsoleHelper.printError("No se pudo registrar el estudiante. Verifique el número de documento.");
            }
        } catch (Exception e) {
            ConsoleHelper.printError("Error al registrar: " + e.getMessage());
        }
        ConsoleInputReader.waitForEnter();
    }

    private void listStudents() {
        ConsoleHelper.printSubHeader("DIRECTORIO GENERAL DE ESTUDIANTES");
        List<Student> students = academicService.getAllStudents();
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
        ConsoleInputReader.waitForEnter();
    }

    private void searchStudent() {
        ConsoleHelper.printSubHeader("CONSULTA DE ESTUDIANTE");
        String docNumber = ConsoleInputReader.readString("Ingrese el número de documento a buscar: ");
        Optional<Student> studentOpt = academicService.getStudentByDocument(docNumber);

        if (studentOpt.isPresent()) {
            Student s = studentOpt.get();
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
        } else {
            ConsoleHelper.printWarning("No se encontró ningún estudiante con el documento: " + docNumber);
        }
        ConsoleInputReader.waitForEnter();
    }

    private void registerAndLinkGuardian() {
        ConsoleHelper.printSubHeader("REGISTRO Y VINCULACIÓN DE ACUDIENTE");
        int studentId = ConsoleInputReader.readInt("Ingrese el ID del estudiante a vincular: ", 1, 99999);
        Optional<Student> stOpt = academicService.getStudentById(studentId);
        if (stOpt.isEmpty()) {
            ConsoleHelper.printError("El estudiante con ID " + studentId + " no existe.");
            ConsoleInputReader.waitForEnter();
            return;
        }

        System.out.println("Vinculando acudiente para: " + stOpt.get().getFullName());
        String docType = ConsoleInputReader.readString("Tipo documento acudiente (CC/CE/Pasaporte): ").toUpperCase();
        String docNum = ConsoleInputReader.readString("Número de documento: ");
        String fName = ConsoleInputReader.readString("Primer nombre: ");
        String sName = ConsoleInputReader.readOptionalString("Segundo nombre: ");
        String lName = ConsoleInputReader.readString("Primer apellido: ");
        String sLName = ConsoleInputReader.readOptionalString("Segundo apellido: ");
        String rel = ConsoleInputReader.readString("Parentesco (Padre/Madre/Tutor Legal/Abuelo/Tío): ");
        String mainPhone = ConsoleInputReader.readString("Teléfono principal: ");
        String secPhone = ConsoleInputReader.readOptionalString("Teléfono secundario: ");
        String email = ConsoleInputReader.readString("Correo electrónico: ");
        String address = ConsoleInputReader.readString("Dirección: ");
        String occ = ConsoleInputReader.readOptionalString("Ocupación: ");
        boolean finResp = ConsoleInputReader.readBoolean("¿Es responsable financiero de pagos?");
        boolean isPrimary = ConsoleInputReader.readBoolean("¿Es el acudiente principal para comunicaciones?");

        Guardian g = new Guardian(null, docType, docNum, fName, sName, lName, sLName, rel,
                mainPhone, secPhone, email, address, occ, finResp);

        Integer gId = academicService.registerGuardian(g);
        if (gId != null) {
            boolean linked = academicService.linkStudentToGuardian(studentId, gId, isPrimary, true);
            if (linked) {
                ConsoleHelper.printSuccess("Acudiente registrado (ID " + gId + ") y vinculado correctamente al estudiante.");
            } else {
                ConsoleHelper.printWarning("Acudiente registrado pero ocurrió un error al vincularlo.");
            }
        } else {
            ConsoleHelper.printError("No se pudo registrar el acudiente.");
        }
        ConsoleInputReader.waitForEnter();
    }

    private void enrollInGrade() {
        ConsoleHelper.printSubHeader("MATRÍCULA DE ESTUDIANTE EN GRADO ACADÉMICO");
        int studentId = ConsoleInputReader.readInt("Ingrese el ID del estudiante: ", 1, 99999);
        Optional<Student> stOpt = academicService.getStudentById(studentId);
        if (stOpt.isEmpty()) {
            ConsoleHelper.printError("Estudiante no encontrado.");
            ConsoleInputReader.waitForEnter();
            return;
        }

        System.out.println("\nGrados Académicos Disponibles:");
        List<AcademicGrade> grades = academicService.getAllGrades();
        for (AcademicGrade gr : grades) {
            System.out.printf("  [%2d] %-15s (Nivel: %d)%n", gr.getIdGrade(), gr.getGradeName(), gr.getNumericLevel());
        }

        int gradeId = ConsoleInputReader.readInt("Seleccione el ID del grado: ", 1, grades.size());
        int year = ConsoleInputReader.readInt("Año académico (ej: 2024): ", 2020, 2030);

        boolean enrolled = academicService.enrollStudentInGrade(studentId, gradeId, year);
        if (enrolled) {
            ConsoleHelper.printSuccess(String.format("Estudiante '%s' matriculado exitosamente en el grado seleccionado para el año %d.",
                    stOpt.get().getFullName(), year));
        } else {
            ConsoleHelper.printError("Error al procesar la matrícula.");
        }
        ConsoleInputReader.waitForEnter();
    }
}
