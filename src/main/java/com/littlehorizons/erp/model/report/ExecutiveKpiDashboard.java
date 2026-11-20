package com.littlehorizons.erp.model.report;

import java.math.BigDecimal;

public record ExecutiveKpiDashboard(
        int totalStudents,
        int totalTeachers,
        int totalClubs,
        int totalResources,
        long borrowedResources,
        double utilizationRate,
        BigDecimal totalRevenue,
        int totalTransactions
) {}
