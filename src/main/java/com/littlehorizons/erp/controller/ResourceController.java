package com.littlehorizons.erp.controller;

import com.littlehorizons.erp.model.resource.Resource;
import com.littlehorizons.erp.model.resource.ResourceCategory;
import com.littlehorizons.erp.model.resource.StudentLoan;
import com.littlehorizons.erp.model.resource.TeacherLoan;
import com.littlehorizons.erp.service.ResourceService;
import com.littlehorizons.erp.util.ConsoleInputReader;
import com.littlehorizons.erp.view.ConsoleHelper;

import java.time.LocalDate;
import java.util.List;

public class ResourceController {

    private final ResourceService resourceService;

    public ResourceController(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    public void run() {
        boolean back = false;
        while (!back) {
            ConsoleHelper.printHeader("GESTIÓN DE RECURSOS (BIBLIOTECA Y LABORATORIOS)", "Colegio Little Horizons");
            System.out.println("1. Inventario general de recursos institucionales");
            System.out.println("2. Registrar nuevo recurso inventariable");
            System.out.println("3. Registrar préstamo a estudiante");
            System.out.println("4. Registrar devolución de recurso por estudiante");
            System.out.println("5. Registrar préstamo de equipo/material a docente");
            System.out.println("6. Registrar devolución de recurso por docente");
            System.out.println("7. Reporte de préstamos vencidos (No devueltos a la fecha)");
            System.out.println("0. Regresar al Menú Principal");
            System.out.println();

            int option = ConsoleInputReader.readInt("Seleccione una opción: ", 0, 7);
            switch (option) {
                case 1 -> listResources();
                case 2 -> registerResource();
                case 3 -> loanToStudent();
                case 4 -> returnStudentLoan();
                case 5 -> loanToTeacher();
                case 6 -> returnTeacherLoan();
                case 7 -> showOverdueLoansReport();
                case 0 -> back = true;
            }
        }
    }

    private void listResources() {
        ConsoleHelper.printSubHeader("INVENTARIO DE RECURSOS ESCOLARES");
        List<Resource> resources = resourceService.getAllResources();
        if (resources.isEmpty()) {
            ConsoleHelper.printWarning("No hay recursos inventariados.");
        } else {
            System.out.printf("%-4s | %-12s | %-36s | %-18s | %-12s | %-16s%n", "ID", "CÓDIGO", "NOMBRE DEL RECURSO", "CATEGORÍA", "ESTADO", "UBICACIÓN");
            System.out.println("─".repeat(110));
            for (Resource r : resources) {
                System.out.printf("%-4d | %-12s | %-36s | %-18s | %-12s | %-16s%n",
                        r.getIdResource(), r.getResourceCode(), r.getResourceName(),
                        r.getCategoryName() != null ? r.getCategoryName() : "Cat " + r.getIdCategory(),
                        r.getStatus(),
                        r.getPhysicalLocation() != null ? r.getPhysicalLocation() : "General");
            }
            System.out.println("─".repeat(110));
            System.out.println("Total de recursos en inventario: " + resources.size());
        }
        ConsoleInputReader.waitForEnter();
    }

    private void registerResource() {
        ConsoleHelper.printSubHeader("REGISTRO DE NUEVO RECURSO");
        String code = ConsoleInputReader.readString("Código de inventario único (ej: LAB-MIC-010): ");
        String name = ConsoleInputReader.readString("Nombre del recurso: ");

        System.out.println("\nCategorías disponibles:");
        List<ResourceCategory> categories = resourceService.getAllCategories();
        for (ResourceCategory cat : categories) {
            System.out.printf("  [%d] %s - %s%n", cat.getIdCategory(), cat.getCategoryName(), cat.getDescription());
        }
        int catId = ConsoleInputReader.readInt("Seleccione ID de categoría: ", 1, categories.size());
        String desc = ConsoleInputReader.readString("Descripción técnica / física: ");
        LocalDate acqDate = ConsoleInputReader.readDate("Fecha de adquisición");
        String loc = ConsoleInputReader.readString("Ubicación física (ej: Biblioteca - Estante A1): ");

        Resource r = new Resource(null, code, name, catId, desc, acqDate, loc, "Disponible");
        try {
            Integer id = resourceService.registerResource(r);
            if (id != null) {
                ConsoleHelper.printSuccess("Recurso registrado exitosamente con ID: " + id);
            } else {
                ConsoleHelper.printError("Error al registrar el recurso.");
            }
        } catch (IllegalArgumentException e) {
            ConsoleHelper.printError("Validación de recurso fallida: " + e.getMessage());
        } catch (IllegalStateException e) {
            ConsoleHelper.printError(e.getMessage());
        } catch (Exception e) {
            ConsoleHelper.printError("Error al registrar el recurso: " + e.getMessage());
        }
        ConsoleInputReader.waitForEnter();
    }

    private void loanToStudent() {
        ConsoleHelper.printSubHeader("PRÉSTAMO DE RECURSO A ESTUDIANTE");
        int resourceId = ConsoleInputReader.readInt("ID del recurso a prestar: ", 1, 99999);
        int studentId = ConsoleInputReader.readInt("ID del estudiante: ", 1, 99999);
        LocalDate loanDate = LocalDate.now();
        LocalDate returnDate = ConsoleInputReader.readDate("Fecha de devolución esperada");
        String obs = ConsoleInputReader.readOptionalString("Observaciones sobre estado del recurso: ");

        try {
            boolean ok = resourceService.loanToStudent(resourceId, studentId, loanDate, returnDate, obs);
            if (ok) {
                ConsoleHelper.printSuccess("Préstamo registrado exitosamente. El recurso ha cambiado su estado a 'Prestado'.");
            } else {
                ConsoleHelper.printError("No se pudo registrar el préstamo.");
            }
        } catch (IllegalArgumentException e) {
            ConsoleHelper.printError("Validación fallida: " + e.getMessage());
        } catch (IllegalStateException e) {
            ConsoleHelper.printError(e.getMessage());
        } catch (Exception e) {
            ConsoleHelper.printError(e.getMessage());
        }
        ConsoleInputReader.waitForEnter();
    }

    private void returnStudentLoan() {
        ConsoleHelper.printSubHeader("DEVOLUCIÓN DE RECURSO (ESTUDIANTE)");
        int loanId = ConsoleInputReader.readInt("Ingrese el ID del préstamo a devolver: ", 1, 99999);
        try {
            boolean ok = resourceService.returnStudentLoan(loanId);
            if (ok) {
                ConsoleHelper.printSuccess("Devolución registrada con éxito. El recurso vuelve a estar 'Disponible' en inventario.");
            } else {
                ConsoleHelper.printError("No se pudo procesar la devolución.");
            }
        } catch (Exception e) {
            ConsoleHelper.printError("Error al procesar devolución: " + e.getMessage());
        }
        ConsoleInputReader.waitForEnter();
    }

    private void loanToTeacher() {
        ConsoleHelper.printSubHeader("PRÉSTAMO DE RECURSO A PROFESOR");
        int resourceId = ConsoleInputReader.readInt("ID del recurso a prestar: ", 1, 99999);
        int teacherId = ConsoleInputReader.readInt("ID del profesor: ", 1, 99999);
        LocalDate loanDate = LocalDate.now();
        LocalDate returnDate = ConsoleInputReader.readDate("Fecha de devolución esperada");
        String obs = ConsoleInputReader.readOptionalString("Observaciones pedagógicas de uso: ");

        try {
            boolean ok = resourceService.loanToTeacher(resourceId, teacherId, loanDate, returnDate, obs);
            if (ok) {
                ConsoleHelper.printSuccess("Préstamo a profesor registrado con éxito. Estado: 'Prestado'.");
            } else {
                ConsoleHelper.printError("No se pudo registrar el préstamo.");
            }
        } catch (IllegalArgumentException e) {
            ConsoleHelper.printError("Validación fallida: " + e.getMessage());
        } catch (IllegalStateException e) {
            ConsoleHelper.printError(e.getMessage());
        } catch (Exception e) {
            ConsoleHelper.printError(e.getMessage());
        }
        ConsoleInputReader.waitForEnter();
    }

    private void returnTeacherLoan() {
        ConsoleHelper.printSubHeader("DEVOLUCIÓN DE RECURSO (PROFESOR)");
        int loanId = ConsoleInputReader.readInt("Ingrese el ID del préstamo docente a devolver: ", 1, 99999);
        try {
            boolean ok = resourceService.returnTeacherLoan(loanId);
            if (ok) {
                ConsoleHelper.printSuccess("Devolución docente registrada. El recurso está 'Disponible'.");
            } else {
                ConsoleHelper.printError("No se pudo procesar la devolución.");
            }
        } catch (Exception e) {
            ConsoleHelper.printError("Error al procesar devolución: " + e.getMessage());
        }
        ConsoleInputReader.waitForEnter();
    }

    private void showOverdueLoansReport() {
        ConsoleHelper.printSubHeader("REPORTE DE PRÉSTAMOS VENCIDOS (NO DEVUELTOS A LA FECHA)");
        List<StudentLoan> overdueStudentLoans = resourceService.getOverdueStudentLoans();
        List<TeacherLoan> overdueTeacherLoans = resourceService.getOverdueTeacherLoans();

        System.out.println("1. Préstamos Vencidos - Estudiantes:");
        if (overdueStudentLoans.isEmpty()) {
            ConsoleHelper.printSuccess("No hay préstamos vencidos de estudiantes.");
        } else {
            System.out.printf("%-4s | %-12s | %-32s | %-25s | %-12s | %-12s%n",
                    "ID", "REC-ID", "RECURSO", "ESTUDIANTE", "F. PRÉSTAMO", "F. ESPERADA");
            System.out.println("─".repeat(110));
            for (StudentLoan sl : overdueStudentLoans) {
                System.out.printf("%-4d | %-12d | %-32s | %-25s | %-12s | %-12s%n",
                        sl.getIdLoan(), sl.getIdResource(), sl.getResourceName(),
                        sl.getStudentName(), sl.getLoanDate(), sl.getExpectedReturnDate());
            }
            System.out.println("─".repeat(110));
        }

        System.out.println("\n2. Préstamos Vencidos - Profesores:");
        if (overdueTeacherLoans.isEmpty()) {
            ConsoleHelper.printSuccess("No hay préstamos vencidos de profesores.");
        } else {
            System.out.printf("%-4s | %-12s | %-32s | %-25s | %-12s | %-12s%n",
                    "ID", "REC-ID", "RECURSO", "PROFESOR", "F. PRÉSTAMO", "F. ESPERADA");
            System.out.println("─".repeat(110));
            for (TeacherLoan tl : overdueTeacherLoans) {
                System.out.printf("%-4d | %-12d | %-32s | %-25s | %-12s | %-12s%n",
                        tl.getIdLoan(), tl.getIdResource(), tl.getResourceName(),
                        tl.getTeacherName(), tl.getLoanDate(), tl.getExpectedReturnDate());
            }
            System.out.println("─".repeat(110));
        }
        ConsoleInputReader.waitForEnter();
    }
}