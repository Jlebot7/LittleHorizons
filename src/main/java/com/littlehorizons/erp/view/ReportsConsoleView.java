package com.littlehorizons.erp.view;

import com.littlehorizons.erp.model.report.ExecutiveKpiDashboard;

public class ReportsConsoleView implements IReportsView {

    @Override
    public void showExecutiveKpiReport(ExecutiveKpiDashboard d) {
        ConsoleHelper.printHeader("TABLERO DE REPORTES Y ESTADÍSTICAS EJECUTIVAS", "Colegio Little Horizons");
        System.out.println("  INDICADORES CLAVE DE GESTIÓN (KPIs):");
        System.out.println("  ════════════════════════════════════════════════════════════");
        System.out.printf("  • Total Estudiantes Registrados : %d%n", d.totalStudents());
        System.out.printf("  • Total Profesores en Planta    : %d%n", d.totalTeachers());
        System.out.printf("  • Total Clubes Extracurriculares: %d%n", d.totalClubs());
        System.out.printf("  • Total Recursos en Inventario  : %d%n", d.totalResources());
        System.out.printf("  • Recursos en Préstamo Activo   : %d (%.1f%% ocupación)%n", d.borrowedResources(), d.utilizationRate());
        System.out.printf("  • Total Recaudado en Finanzas   : COP $%,.2f%n", d.totalRevenue());
        System.out.printf("  • Transacciones de Pago Totales : %d%n", d.totalTransactions());
        System.out.println("  ════════════════════════════════════════════════════════════");
    }

    @Override
    public void showSuccess(String message) {
        ConsoleHelper.printSuccess(message);
    }

    @Override
    public void showError(String message) {
        ConsoleHelper.printError(message);
    }
}
