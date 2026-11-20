package com.littlehorizons.erp.controller;

import com.littlehorizons.erp.model.report.ExecutiveKpiDashboard;
import com.littlehorizons.erp.service.*;
import com.littlehorizons.erp.util.ConsoleInputReader;
import com.littlehorizons.erp.view.IReportsView;
import com.littlehorizons.erp.view.ReportsConsoleView;

public class ReportsController {

    private final IReportsService reportsService;
    private final IReportsView reportsView;

    public ReportsController(IAcademicService academicService,
                             IExtracurricularService extracurricularService,
                             IFinanceService financeService,
                             IResourceService resourceService) {
        this(new ReportsService(academicService, extracurricularService, financeService, resourceService),
                new ReportsConsoleView());
    }

    public ReportsController(IReportsService reportsService, IReportsView reportsView) {
        this.reportsService = reportsService;
        this.reportsView = reportsView;
    }

    public void run() {
        ExecutiveKpiDashboard dashboard = reportsService.generateExecutiveDashboard();
        reportsView.showExecutiveKpiReport(dashboard);
        ConsoleInputReader.waitForEnter();
    }
}
