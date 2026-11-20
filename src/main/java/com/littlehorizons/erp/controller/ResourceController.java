package com.littlehorizons.erp.controller;

import com.littlehorizons.erp.model.resource.Resource;
import com.littlehorizons.erp.model.resource.ResourceCategory;
import com.littlehorizons.erp.model.resource.StudentLoan;
import com.littlehorizons.erp.model.resource.TeacherLoan;
import com.littlehorizons.erp.service.IResourceService;
import com.littlehorizons.erp.util.ConsoleInputReader;
import com.littlehorizons.erp.view.IResourceView;
import com.littlehorizons.erp.view.ResourceConsoleView;

import java.time.LocalDate;
import java.util.List;

public class ResourceController {

    private final IResourceService resourceService;
    private final IResourceView resourceView;

    public ResourceController(IResourceService resourceService) {
        this(resourceService, new ResourceConsoleView());
    }

    public ResourceController(IResourceService resourceService, IResourceView resourceView) {
        this.resourceService = resourceService;
        this.resourceView = resourceView;
    }

    public void run() {
        boolean back = false;
        while (!back) {
            resourceView.showMenu();
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
        List<Resource> resources = resourceService.getAllResources();
        resourceView.showResources(resources);
        ConsoleInputReader.waitForEnter();
    }

    private void registerResource() {
        resourceView.showRegisterResourceHeader();
        String code = ConsoleInputReader.readString("Código de inventario único (ej: LAB-MIC-010): ");
        String name = ConsoleInputReader.readString("Nombre del recurso: ");

        List<ResourceCategory> categories = resourceService.getAllCategories();
        resourceView.showCategories(categories);
        int catId = ConsoleInputReader.readInt("Seleccione ID de categoría: ", 1, categories.size());
        String desc = ConsoleInputReader.readString("Descripción técnica / física: ");
        LocalDate acqDate = ConsoleInputReader.readDate("Fecha de adquisición");
        String loc = ConsoleInputReader.readString("Ubicación física (ej: Biblioteca - Estante A1): ");

        Resource r = new Resource(null, code, name, catId, desc, acqDate, loc, "Disponible");
        try {
            Integer id = resourceService.registerResource(r);
            if (id != null) {
                resourceView.showSuccess("Recurso registrado exitosamente con ID: " + id);
            } else {
                resourceView.showError("Error al registrar el recurso.");
            }
        } catch (IllegalArgumentException e) {
            resourceView.showError("Validación de recurso fallida: " + e.getMessage());
        } catch (IllegalStateException e) {
            resourceView.showError(e.getMessage());
        } catch (Exception e) {
            resourceView.showError("Error al registrar el recurso: " + e.getMessage());
        }
        ConsoleInputReader.waitForEnter();
    }

    private void loanToStudent() {
        resourceView.showLoanToStudentHeader();
        int resourceId = ConsoleInputReader.readInt("ID del recurso a prestar: ", 1, 99999);
        int studentId = ConsoleInputReader.readInt("ID del estudiante: ", 1, 99999);
        LocalDate loanDate = LocalDate.now();
        LocalDate returnDate = ConsoleInputReader.readDate("Fecha de devolución esperada");
        String obs = ConsoleInputReader.readOptionalString("Observaciones sobre estado del recurso: ");

        try {
            boolean ok = resourceService.loanToStudent(resourceId, studentId, loanDate, returnDate, obs);
            if (ok) {
                resourceView.showSuccess("Préstamo registrado exitosamente. El recurso ha cambiado su estado a 'Prestado'.");
            } else {
                resourceView.showError("No se pudo registrar el préstamo.");
            }
        } catch (IllegalArgumentException e) {
            resourceView.showError("Validación fallida: " + e.getMessage());
        } catch (IllegalStateException e) {
            resourceView.showError(e.getMessage());
        } catch (Exception e) {
            resourceView.showError(e.getMessage());
        }
        ConsoleInputReader.waitForEnter();
    }

    private void returnStudentLoan() {
        resourceView.showReturnStudentLoanHeader();
        int loanId = ConsoleInputReader.readInt("Ingrese el ID del préstamo a devolver: ", 1, 99999);
        try {
            boolean ok = resourceService.returnStudentLoan(loanId);
            if (ok) {
                resourceView.showSuccess("Devolución registrada con éxito. El recurso vuelve a estar 'Disponible' en inventario.");
            } else {
                resourceView.showError("No se pudo procesar la devolución.");
            }
        } catch (Exception e) {
            resourceView.showError("Error al procesar devolución: " + e.getMessage());
        }
        ConsoleInputReader.waitForEnter();
    }

    private void loanToTeacher() {
        resourceView.showLoanToTeacherHeader();
        int resourceId = ConsoleInputReader.readInt("ID del recurso a prestar: ", 1, 99999);
        int teacherId = ConsoleInputReader.readInt("ID del profesor: ", 1, 99999);
        LocalDate loanDate = LocalDate.now();
        LocalDate returnDate = ConsoleInputReader.readDate("Fecha de devolución esperada");
        String obs = ConsoleInputReader.readOptionalString("Observaciones pedagógicas de uso: ");

        try {
            boolean ok = resourceService.loanToTeacher(resourceId, teacherId, loanDate, returnDate, obs);
            if (ok) {
                resourceView.showSuccess("Préstamo a profesor registrado con éxito. Estado: 'Prestado'.");
            } else {
                resourceView.showError("No se pudo registrar el préstamo.");
            }
        } catch (IllegalArgumentException e) {
            resourceView.showError("Validación fallida: " + e.getMessage());
        } catch (IllegalStateException e) {
            resourceView.showError(e.getMessage());
        } catch (Exception e) {
            resourceView.showError(e.getMessage());
        }
        ConsoleInputReader.waitForEnter();
    }

    private void returnTeacherLoan() {
        resourceView.showReturnTeacherLoanHeader();
        int loanId = ConsoleInputReader.readInt("Ingrese el ID del préstamo docente a devolver: ", 1, 99999);
        try {
            boolean ok = resourceService.returnTeacherLoan(loanId);
            if (ok) {
                resourceView.showSuccess("Devolución docente registrada. El recurso está 'Disponible'.");
            } else {
                resourceView.showError("No se pudo procesar la devolución.");
            }
        } catch (Exception e) {
            resourceView.showError("Error al procesar devolución: " + e.getMessage());
        }
        ConsoleInputReader.waitForEnter();
    }

    private void showOverdueLoansReport() {
        List<StudentLoan> overdueStudentLoans = resourceService.getOverdueStudentLoans();
        List<TeacherLoan> overdueTeacherLoans = resourceService.getOverdueTeacherLoans();
        resourceView.showOverdueLoansReport(overdueStudentLoans, overdueTeacherLoans);
        ConsoleInputReader.waitForEnter();
    }
}
