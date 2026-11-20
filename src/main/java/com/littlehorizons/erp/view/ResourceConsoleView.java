package com.littlehorizons.erp.view;

import com.littlehorizons.erp.model.resource.Resource;
import com.littlehorizons.erp.model.resource.ResourceCategory;
import com.littlehorizons.erp.model.resource.StudentLoan;
import com.littlehorizons.erp.model.resource.TeacherLoan;

import java.util.List;

public class ResourceConsoleView implements IResourceView {

    @Override
    public void showMenu() {
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
    }

    @Override
    public void showResources(List<Resource> resources) {
        ConsoleHelper.printSubHeader("INVENTARIO DE RECURSOS ESCOLARES");
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
    }

    @Override
    public void showRegisterResourceHeader() {
        ConsoleHelper.printSubHeader("REGISTRO DE NUEVO RECURSO");
    }

    @Override
    public void showCategories(List<ResourceCategory> categories) {
        System.out.println("\nCategorías disponibles:");
        for (ResourceCategory cat : categories) {
            System.out.printf("  [%d] %s - %s%n", cat.getIdCategory(), cat.getCategoryName(), cat.getDescription());
        }
    }

    @Override
    public void showLoanToStudentHeader() {
        ConsoleHelper.printSubHeader("PRÉSTAMO DE RECURSO A ESTUDIANTE");
    }

    @Override
    public void showReturnStudentLoanHeader() {
        ConsoleHelper.printSubHeader("DEVOLUCIÓN DE RECURSO (ESTUDIANTE)");
    }

    @Override
    public void showLoanToTeacherHeader() {
        ConsoleHelper.printSubHeader("PRÉSTAMO DE RECURSO A PROFESOR");
    }

    @Override
    public void showReturnTeacherLoanHeader() {
        ConsoleHelper.printSubHeader("DEVOLUCIÓN DE RECURSO (PROFESOR)");
    }

    @Override
    public void showOverdueLoansReport(List<StudentLoan> studentLoans, List<TeacherLoan> teacherLoans) {
        ConsoleHelper.printSubHeader("REPORTE DE PRÉSTAMOS VENCIDOS (NO DEVUELTOS A LA FECHA)");
        System.out.println("1. Préstamos Vencidos - Estudiantes:");
        if (studentLoans.isEmpty()) {
            ConsoleHelper.printSuccess("No hay préstamos vencidos de estudiantes.");
        } else {
            System.out.printf("%-4s | %-12s | %-32s | %-25s | %-12s | %-12s%n",
                    "ID", "REC-ID", "RECURSO", "ESTUDIANTE", "F. PRÉSTAMO", "F. ESPERADA");
            System.out.println("─".repeat(110));
            for (StudentLoan sl : studentLoans) {
                System.out.printf("%-4d | %-12d | %-32s | %-25s | %-12s | %-12s%n",
                        sl.getIdLoan(), sl.getIdResource(), sl.getResourceName(),
                        sl.getStudentName(), sl.getLoanDate(), sl.getExpectedReturnDate());
            }
            System.out.println("─".repeat(110));
        }

        System.out.println("\n2. Préstamos Vencidos - Profesores:");
        if (teacherLoans.isEmpty()) {
            ConsoleHelper.printSuccess("No hay préstamos vencidos de profesores.");
        } else {
            System.out.printf("%-4s | %-12s | %-32s | %-25s | %-12s | %-12s%n",
                    "ID", "REC-ID", "RECURSO", "PROFESOR", "F. PRÉSTAMO", "F. ESPERADA");
            System.out.println("─".repeat(110));
            for (TeacherLoan tl : teacherLoans) {
                System.out.printf("%-4d | %-12d | %-32s | %-25s | %-12s | %-12s%n",
                        tl.getIdLoan(), tl.getIdResource(), tl.getResourceName(),
                        tl.getTeacherName(), tl.getLoanDate(), tl.getExpectedReturnDate());
            }
            System.out.println("─".repeat(110));
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
