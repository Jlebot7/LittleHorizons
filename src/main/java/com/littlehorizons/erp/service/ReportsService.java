package com.littlehorizons.erp.service;

import com.littlehorizons.erp.model.academic.Student;
import com.littlehorizons.erp.model.academic.Teacher;
import com.littlehorizons.erp.model.extracurricular.Club;
import com.littlehorizons.erp.model.finance.Payment;
import com.littlehorizons.erp.model.report.ExecutiveKpiDashboard;
import com.littlehorizons.erp.model.resource.Resource;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

public class ReportsService implements IReportsService {

    private final IAcademicService academicService;
    private final IExtracurricularService extracurricularService;
    private final IFinanceService financeService;
    private final IResourceService resourceService;

    public ReportsService(IAcademicService academicService,
                          IExtracurricularService extracurricularService,
                          IFinanceService financeService,
                          IResourceService resourceService) {
        this.academicService = academicService;
        this.extracurricularService = extracurricularService;
        this.financeService = financeService;
        this.resourceService = resourceService;
    }

    @Override
    public ExecutiveKpiDashboard generateExecutiveDashboard() {
        List<Student> students = academicService.getAllStudents();
        List<Teacher> teachers = academicService.getAllTeachers();
        List<Club> clubs = extracurricularService.getAllClubs();
        List<Resource> resources = resourceService.getAllResources();
        List<Payment> payments = financeService.getAllPayments();

        BigDecimal totalCollected = payments.stream()
                .map(Payment::getAmountPaid)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long borrowedResources = resources.stream()
                .filter(r -> "Prestado".equalsIgnoreCase(r.getStatus()))
                .count();

        double resourceUtilization = resources.isEmpty() ? 0.0 : ((double) borrowedResources / resources.size()) * 100.0;

        return new ExecutiveKpiDashboard(
                students.size(),
                teachers.size(),
                clubs.size(),
                resources.size(),
                borrowedResources,
                resourceUtilization,
                totalCollected,
                payments.size()
        );
    }
}
