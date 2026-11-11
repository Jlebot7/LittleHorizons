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

/**
 * Controller for Resource Management (Library, Laboratory, Sports, Technology) and Loans.
 */
public class ResourceController {

    private final ResourceService resourceService;

    public ResourceController(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    public void run() {
        boolean back = false;
        while (!back) {
            ConsoleHelper.printHeader("GESTIÓN DE RECURSOS, BIBLIOTECA Y LABORATORIOS", "Colegio Little Horizons");
            System.out.println("1. Inventario general de recursos (Libros, Equipos, Tecnología)");
            System.out.println("2. Registrar nuevo recurso en inventario");
            System.out.println("3. Registrar préstamo a ESTUDIANTE (Validación de disponibilidad)");
            System.out.println("4. Registrar devolución de recurso por ESTUDIANTE");
            System.out.println("5. Registrar préstamo a PROFESOR");
            System.out.println("6. Registrar devolución de recurso por PROFESOR");
            System.out.println("7. Reporte de préstamos vencidos (Stream API)");
            System.out.println("8. Consultar préstamos activos (Estudiantes y Profesores)");
            System.out.println("0. Regresar al Menú Principal");
            System.out.println();

            int option = ConsoleInputReader.readInt("Seleccione una opción: ", 0, 8);
            switch (option) {
                case 1 -> listResources();
                case 2 -> registerResource();
                case 3 -> loanToStudent();
                case 4 -> returnStudentLoan();
                case 5 -> loanToTeacher();
                case 6 -> returnTeacherLoan();
                case 7 -> showOverdueLoansReport();
                case 8 -> listActiveLoans();
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
        Integer id = resourceService.registerResource(r);
        if (id != null) {
            ConsoleHelper.printSuccess("Recurso registrado exitosamente con ID: " + id);
        } else {
            ConsoleHelper.printError("Error al registrar el recurso.");
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
        } catch (Exception e) {
            ConsoleHelper.printError(e.getMessage());
        }
        ConsoleInputReader.waitForEnter();
    }

    private void returnStudentLoan() {
        ConsoleHelper.printSubHeader("DEVOLUCIÓN DE RECURSO (ESTUDIANTE)");
        int loanId = ConsoleInputReader.readInt("Ingrese el ID del préstamo a devolver: ", 1, 99999);
        boolean ok = resourceService.returnStudentLoan(loanId);
        if (ok) {
            ConsoleHelper.printSuccess("Devolución registrada con éxito. El recurso vuelve a estar 'Disponible' en inventario.");
        } else {
            ConsoleHelper.printError("No se pudo procesar la devolución.");
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
        } catch (Exception e) {
            ConsoleHelper.printError(e.getMessage());
        }
        ConsoleInputReader.waitForEnter();
    }

    private void returnTeacherLoan() {
        ConsoleHelper.printSubHeader("DEVOLUCIÓN DE RECURSO (PROFESOR)");
        int loanId = ConsoleInputReader.readInt("Ingrese el ID del préstamo docente a devolver: ", 1, 99999);
        boolean ok = resourceService.returnTeacherLoan(loanId);
        if (ok) {
            ConsoleHelper.printSuccess("Devolución docente registrada. El recurso está 'Disponible'.");
        } else {
            ConsoleHelper.printError("No se pudo procesar la devolución.");
        }
        ConsoleInputReader.waitForEnter();
    }

    private void showOverdueLoansReport() {
        ConsoleHelper.printSubHeader("REPORTE DE PRÉSTAMOS VENCIDOS (NO DEVUELTOS A LA FECHA)");
        List<StudentLoan> overdueStudents = resourceService.getOverdueStudentLoans();
        List<TeacherLoan> overdueTeachers = resourceService.getOverdueTeacherLoans();

        if (overdueStudents.isEmpty() && overdueTeachers.isEmpty()) {
            ConsoleHelper.printSuccess("¡Excelente! No hay préstamos con fecha de entrega vencida.");
        } else {
            if (!overdueStudents.isEmpty()) {
                System.out.println("\nPréstamos vencidos a ESTUDIANTES:");
                System.out.printf("%-5s | %-28s | %-25s | %-12s | %-10s%n", "ID", "RECURSO", "ESTUDIANTE", "FECHA ESPERADA", "ESTADO");
                System.out.println("─".repeat(85));
                for (StudentLoan sl : overdueStudents) {
                    System.out.printf("%-5d | %-28s | %-25s | %-14s | %-10s%n",
                            sl.getIdLoan(), sl.getResourceName(), sl.getStudentName(),
                            sl.getExpectedReturnDate(), sl.getStatus());
                }
                System.out.println("─".repeat(85));
            }

            if (!overdueTeachers.isEmpty()) {
                System.out.println("\nPréstamos vencidos a PROFESORES:");
                System.out.printf("%-5s | %-28s | %-25s | %-12s | %-10s%n", "ID", "RECURSO", "PROFESOR", "FECHA ESPERADA", "ESTADO");
                System.out.println("─".repeat(85));
                for (TeacherLoan tl : overdueTeachers) {
                    System.out.printf("%-5d | %-28s | %-25s | %-14s | %-10s%n",
                            tl.getIdLoan(), tl.getResourceName(), tl.getTeacherName(),
                            tl.getExpectedReturnDate(), tl.getStatus());
                }
                System.out.println("─".repeat(85));
            }
        }
        ConsoleInputReader.waitForEnter();
    }

    private void listActiveLoans() {
        ConsoleHelper.printSubHeader("PRÉSTAMOS ACTIVOS EN CIRCULACIÓN");
        List<StudentLoan> sLoans = resourceService.getActiveStudentLoans();
        List<TeacherLoan> tLoans = resourceService.getActiveTeacherLoans();

        System.out.println("Préstamos a Estudiantes:");
        if (sLoans.isEmpty()) {
            System.out.println("  (Sin préstamos activos de estudiantes)");
        } else {
            for (StudentLoan sl : sLoans) {
                System.out.println("  " + sl);
            }
        }

        System.out.println("\nPréstamos a Profesores:");
        if (tLoans.isEmpty()) {
            System.out.println("  (Sin préstamos activos de profesores)");
        } else {
            for (TeacherLoan tl : tLoans) {
                System.out.println("  " + tl);
            }
        }
        ConsoleInputReader.waitForEnter();
    }
}
