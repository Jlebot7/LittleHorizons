package com.littlehorizons.erp;

import com.littlehorizons.erp.model.academic.AcademicRecord;
import com.littlehorizons.erp.model.academic.Student;
import com.littlehorizons.erp.model.extracurricular.Club;
import com.littlehorizons.erp.model.finance.Payment;
import com.littlehorizons.erp.model.resource.StudentLoan;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class StreamCalculationTest {

    @Test
    @DisplayName("Should accurately calculate average using Java Stream API")
    void testPeriodAverageCalculationWithStreams() {
        List<AcademicRecord> grades = Arrays.asList(
                new AcademicRecord(1, 1, 1, 1, 2024, "I", new BigDecimal("4.5"), "", LocalDate.now()),
                new AcademicRecord(2, 1, 2, 2, 2024, "I", new BigDecimal("3.5"), "", LocalDate.now()),
                new AcademicRecord(3, 1, 3, 3, 2024, "I", new BigDecimal("4.0"), "", LocalDate.now()),
                new AcademicRecord(4, 1, 4, 4, 2024, "II", new BigDecimal("5.0"), "", LocalDate.now())
        );

        BigDecimal averagePeriodI = grades.stream()
                .filter(r -> r.getAcademicYear() == 2024 && "I".equalsIgnoreCase(r.getAcademicPeriod()))
                .map(AcademicRecord::getGradeValue)
                .filter(Objects::nonNull)
                .map(BigDecimal::doubleValue)
                .mapToDouble(Double::doubleValue)
                .average()
                .stream()
                .mapToObj(avg -> BigDecimal.valueOf(avg).setScale(2, RoundingMode.HALF_UP))
                .findFirst()
                .orElse(BigDecimal.ZERO);

        assertEquals(new BigDecimal("4.00"), averagePeriodI);
    }

    @Test
    @DisplayName("Should group grades into qualitative ranges using groupingBy")
    void testGradeDistributionGrouping() {
        List<AcademicRecord> records = List.of(
                new AcademicRecord(1, 1, 1, 1, 2024, "I", new BigDecimal("4.8"), "", LocalDate.now()),
                new AcademicRecord(2, 2, 1, 1, 2024, "I", new BigDecimal("4.2"), "", LocalDate.now()),
                new AcademicRecord(3, 3, 1, 1, 2024, "I", new BigDecimal("3.4"), "", LocalDate.now()),
                new AcademicRecord(4, 4, 1, 1, 2024, "I", new BigDecimal("2.5"), "", LocalDate.now()),
                new AcademicRecord(5, 5, 1, 1, 2024, "I", new BigDecimal("4.9"), "", LocalDate.now())
        );

        Map<String, Long> distribution = records.stream()
                .filter(r -> r.getGradeValue() != null)
                .collect(Collectors.groupingBy(r -> {
                    double val = r.getGradeValue().doubleValue();
                    if (val >= 4.6) return "Sobresaliente";
                    if (val >= 4.0) return "Alto";
                    if (val >= 3.0) return "Básico";
                    return "Bajo";
                }, Collectors.counting()));

        assertEquals(2L, distribution.get("Sobresaliente"));
        assertEquals(1L, distribution.get("Alto"));
        assertEquals(1L, distribution.get("Básico"));
        assertEquals(1L, distribution.get("Bajo"));
    }

    @Test
    @DisplayName("Should calculate descriptive DoubleSummaryStatistics over grades")
    void testPerformanceSummaryStatistics() {
        List<AcademicRecord> records = List.of(
                new AcademicRecord(1, 1, 1, 1, 2024, "I", new BigDecimal("3.0"), "", LocalDate.now()),
                new AcademicRecord(2, 2, 1, 1, 2024, "I", new BigDecimal("4.0"), "", LocalDate.now()),
                new AcademicRecord(3, 3, 1, 1, 2024, "I", new BigDecimal("5.0"), "", LocalDate.now())
        );

        DoubleSummaryStatistics stats = records.stream()
                .map(AcademicRecord::getGradeValue)
                .filter(Objects::nonNull)
                .mapToDouble(BigDecimal::doubleValue)
                .summaryStatistics();

        assertEquals(3, stats.getCount());
        assertEquals(3.0, stats.getMin(), 0.001);
        assertEquals(5.0, stats.getMax(), 0.001);
        assertEquals(4.0, stats.getAverage(), 0.001);
        assertEquals(12.0, stats.getSum(), 0.001);
    }

    @Test
    @DisplayName("Should aggregate revenue by payment method using groupingBy and reducing")
    void testRevenueAggregationByMethod() {
        List<Payment> payments = List.of(
                new Payment(1, 1, 1, 1, new BigDecimal("100000.00"), LocalDate.now(), "Efectivo", "REC-01", 2024, "I", ""),
                new Payment(2, 2, 1, 1, new BigDecimal("150000.00"), LocalDate.now(), "Transferencia", "REC-02", 2024, "I", ""),
                new Payment(3, 3, 2, 1, new BigDecimal("200000.00"), LocalDate.now(), "Efectivo", "REC-03", 2024, "I", "")
        );

        Map<String, BigDecimal> revenueByMethod = payments.stream()
                .filter(p -> p.getPaymentMethod() != null && p.getAmountPaid() != null)
                .collect(Collectors.groupingBy(Payment::getPaymentMethod,
                        Collectors.reducing(BigDecimal.ZERO, Payment::getAmountPaid, BigDecimal::add)));

        assertEquals(new BigDecimal("300000.00"), revenueByMethod.get("Efectivo"));
        assertEquals(new BigDecimal("150000.00"), revenueByMethod.get("Transferencia"));
    }

    @Test
    @DisplayName("Should partition loans into overdue and current using partitioningBy")
    void testPartitionLoansByOverdue() {
        LocalDate today = LocalDate.now();
        List<StudentLoan> loans = List.of(
                new StudentLoan(1, 1, 1, today.minusDays(10), today.minusDays(2), null, "Prestado", "Tablet"),
                new StudentLoan(2, 2, 2, today.minusDays(1), today.plusDays(5), null, "Prestado", "Libro")
        );

        Map<Boolean, List<StudentLoan>> partitioned = loans.stream()
                .collect(Collectors.partitioningBy(l -> l.getExpectedReturnDate() != null && l.getExpectedReturnDate().isBefore(today)));

        assertEquals(1, partitioned.get(true).size());
        assertEquals(1, partitioned.get(false).size());
    }

    @Test
    @DisplayName("Should filter and sort clubs near capacity")
    void testClubsNearCapacityFilter() {
        Club c1 = new Club(1, "Robótica", "Club", 1, 20, "Viernes", true);
        c1.setEnrolledCount(19);

        Club c2 = new Club(2, "Teatro", "Club", 2, 20, "Jueves", true);
        c2.setEnrolledCount(10);

        List<Club> clubs = List.of(c1, c2);

        List<Club> nearCapacity = clubs.stream()
                .filter(c -> c.getMaxCapacity() > 0 && ((double) c.getEnrolledCount() / c.getMaxCapacity()) >= 0.90)
                .collect(Collectors.toList());

        assertEquals(1, nearCapacity.size());
        assertEquals("Robótica", nearCapacity.get(0).getClubName());
    }
}
