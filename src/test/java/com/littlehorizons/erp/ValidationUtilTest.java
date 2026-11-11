package com.littlehorizons.erp;

import com.littlehorizons.erp.util.ValidationUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class ValidationUtilTest {

    @Test
    @DisplayName("Grade validation must accept 0.0 to 5.0 and reject out of range")
    void testGradeValidation() {
        assertTrue(ValidationUtil.isValidGrade(new BigDecimal("0.0")));
        assertTrue(ValidationUtil.isValidGrade(new BigDecimal("3.5")));
        assertTrue(ValidationUtil.isValidGrade(new BigDecimal("5.0")));

        assertFalse(ValidationUtil.isValidGrade(new BigDecimal("-0.1")));
        assertFalse(ValidationUtil.isValidGrade(new BigDecimal("5.1")));
        assertFalse(ValidationUtil.isValidGrade(null));
    }

    @Test
    @DisplayName("Email validation must follow standard patterns")
    void testEmailValidation() {
        assertTrue(ValidationUtil.isValidEmail("carlos.ramirez@gmail.com"));
        assertTrue(ValidationUtil.isValidEmail("profesor@littlehorizons.edu.co"));

        assertFalse(ValidationUtil.isValidEmail("invalid-email"));
        assertFalse(ValidationUtil.isValidEmail(""));
        assertFalse(ValidationUtil.isValidEmail(null));
    }

    @Test
    @DisplayName("Date validation must enforce yyyy-MM-dd format")
    void testDateValidation() {
        assertTrue(ValidationUtil.isValidDate("2024-03-15"));
        assertTrue(ValidationUtil.isValidDate("2010-12-31"));

        assertFalse(ValidationUtil.isValidDate("15/03/2024"));
        assertFalse(ValidationUtil.isValidDate("2024-02-30")); // invalid date
        assertFalse(ValidationUtil.isValidDate("not-a-date"));
    }
}
