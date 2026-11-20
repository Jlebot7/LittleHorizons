package com.littlehorizons.erp.view;

import com.littlehorizons.erp.model.report.ExecutiveKpiDashboard;

public interface IReportsView {
    void showExecutiveKpiReport(ExecutiveKpiDashboard dashboard);
    void showSuccess(String message);
    void showError(String message);
}
