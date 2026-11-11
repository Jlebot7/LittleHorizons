package com.littlehorizons.erp.controller;

import com.littlehorizons.erp.model.academic.Student;
import com.littlehorizons.erp.model.academic.Teacher;
import com.littlehorizons.erp.model.extracurricular.Club;
import com.littlehorizons.erp.model.finance.Payment;
import com.littlehorizons.erp.model.resource.Resource;
import com.littlehorizons.erp.service.AcademicService;
import com.littlehorizons.erp.service.ExtracurricularService;
import com.littlehorizons.erp.service.FinanceService;
import com.littlehorizons.erp.service.ResourceService;
import com.littlehorizons.erp.util.ConsoleInputReader;
import com.littlehorizons.erp.view.ConsoleHelper;

import java.math.BigDecimal;
import java.util.List;

/**
 * Controller for General Reports and Institutional Metrics Dashboard.
 */
public class ReportsController {

    private final AcademicService academicService;
    private final ExtracurricularService extracurricularService;
    private final FinanceService financeService;
    private final ResourceService resourceService;

    public ReportsController(AcademicService academicService,
                             ExtracurricularService extracurricularService,
                             FinanceService financeService,
                             ResourceService resourceService) {
        this.academicService = academicService;
        this.extracurricularService = extracurricularService;
        this.financeService = financeService;
        this.resourceService = resourceService;
    }

    public void run() {
        ConsoleHelper.printHeader("TABLERO DE REPORTES Y ESTADÍSTICAS EJECUTIVAS", "Colegio Little Horizons");

        List<Student> students = academicService.getAllStudents();
        List<Teacher> teachers = academicService.getAllTeachers();
        List<Club> clubs = extracurricularService.getAllClubs();
        List<Resource> resources = resourceService.getAllResources();
        List<Payment> payments = financeService.getAllPayments();

        BigDecimal totalCollected = payments.stream()
                .map(Payment::getAmountPaid)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long borrowedResources = resources.stream()
                .filter(r -> "Prestado".equalsIgnoreCase(r.getStatus()))
                .count();

        double resourceUtilization = resources.isEmpty() ? 0.0 : ((double) borrowedResources / resources.size()) * 100.0;

        System.out.println("  INDICADORES CLAVE DE GESTIÓN (KPIs):");
        System.out.println("  ════════════════════════════════════════════════════════════");
        System.out.printf("  • Total Estudiantes Registrados : %d%n", students.size());
        System.out.printf("  • Total Profesores en Planta    : %d%n", teachers.size());
        System.out.printf("  • Total Clubes Extracurriculares: %d%n", clubs.size());
        System.out.printf("  • Total Recursos en Inventario  : %d%n", resources.size());
        System.out.printf("  • Recursos en Préstamo Activo   : %d (%.1f%% ocupación)%n", borrowedResources, resourceUtilization);
        System.out.printf("  • Total Recaudado en Finanzas   : COP $%,.2f%n", totalCollected);
        System.out.printf("  • Transacciones de Pago Totales : %d%n", payments.size());
        System.out.println("  ════════════════════════════════════════════════════════════");

        ConsoleInputReader.waitForEnter();
    }
}
