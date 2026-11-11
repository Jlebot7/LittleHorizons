package com.littlehorizons.erp;

import com.littlehorizons.erp.model.academic.AcademicRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

class StreamCalculationTest {

    @Test
    @DisplayName("Should accurately calculate average using Java Stream API")
    void testPeriodAverageCalculationWithStreams() {
        List<AcademicRecord> grades = Arrays.asList(
                new AcademicRecord(1, 1, 1, 1, 2024, "I", new BigDecimal("4.5"), "", LocalDate.now()),
                new AcademicRecord(2, 1, 2, 2, 2024, "I", new BigDecimal("3.5"), "", LocalDate.now()),
                new AcademicRecord(3, 1, 3, 3, 2024, "I", new BigDecimal("4.0"), "", LocalDate.now()),
                new AcademicRecord(4, 1, 4, 4, 2024, "II", new BigDecimal("5.0"), "", LocalDate.now()) // Different period
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

        // Expected: (4.5 + 3.5 + 4.0) / 3 = 12.0 / 3 = 4.00
        assertEquals(new BigDecimal("4.00"), averagePeriodI);
    }
}
