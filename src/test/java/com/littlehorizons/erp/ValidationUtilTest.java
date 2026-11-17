package com.littlehorizons.erp;

import com.littlehorizons.erp.util.ValidationUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

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
        assertFalse(ValidationUtil.isValidDate("2024-02-30"));
        assertFalse(ValidationUtil.isValidDate("not-a-date"));
    }

    @Test
    @DisplayName("Person name validation must reject numbers and special symbols")
    void testPersonNameValidation() {
        assertTrue(ValidationUtil.isValidPersonName("Juan Carlos"));
        assertTrue(ValidationUtil.isValidPersonName("María José"));
        assertTrue(ValidationUtil.isValidPersonName("O'Connor"));
        assertTrue(ValidationUtil.isValidPersonName("Gómez-Mont"));
        assertTrue(ValidationUtil.isValidPersonName("Álvaro Peña"));

        assertFalse(ValidationUtil.isValidPersonName("Juan123"));
        assertFalse(ValidationUtil.isValidPersonName("Carlos2"));
        assertFalse(ValidationUtil.isValidPersonName("Ana!"));
        assertFalse(ValidationUtil.isValidPersonName("123456"));
        assertFalse(ValidationUtil.isValidPersonName(""));
        assertFalse(ValidationUtil.isValidPersonName(null));
        assertFalse(ValidationUtil.isValidPersonName("A"));
    }

    @Test
    @DisplayName("Document type validation must support educational inclusive spectrum")
    void testDocumentTypeValidation() {
        assertTrue(ValidationUtil.isValidDocumentType("RC", true));
        assertTrue(ValidationUtil.isValidDocumentType("TI", true));
        assertTrue(ValidationUtil.isValidDocumentType("CC", true));
        assertTrue(ValidationUtil.isValidDocumentType("CE", true));
        assertTrue(ValidationUtil.isValidDocumentType("Pasaporte", true));
        assertTrue(ValidationUtil.isValidDocumentType("PPT", true));
        assertTrue(ValidationUtil.isValidDocumentType("PEP", true));
        assertTrue(ValidationUtil.isValidDocumentType("NES", true));
        assertTrue(ValidationUtil.isValidDocumentType("VISA", true));

        assertFalse(ValidationUtil.isValidDocumentType("RC", false));
        assertTrue(ValidationUtil.isValidDocumentType("CC", false));
        assertTrue(ValidationUtil.isValidDocumentType("CE", false));
        assertTrue(ValidationUtil.isValidDocumentType("Pasaporte", false));
        assertTrue(ValidationUtil.isValidDocumentType("PPT", false));
        assertTrue(ValidationUtil.isValidDocumentType("PEP", false));
        assertTrue(ValidationUtil.isValidDocumentType("VISA", false));

        assertFalse(ValidationUtil.isValidDocumentType("XYZ", true));
        assertFalse(ValidationUtil.isValidDocumentType("", true));
        assertFalse(ValidationUtil.isValidDocumentType(null, true));
    }

    @Test
    @DisplayName("Differentiated document number validation rejects letters in CC/TI/RC")
    void testDifferentiatedDocumentNumberValidation() {
        assertTrue(ValidationUtil.isValidDocumentNumber("CC", "1098765432"));
        assertTrue(ValidationUtil.isValidDocumentNumber("TI", "1023456789"));
        assertTrue(ValidationUtil.isValidDocumentNumber("RC", "1234567890"));

        assertFalse(ValidationUtil.isValidDocumentNumber("CC", "1098A65432"));
        assertFalse(ValidationUtil.isValidDocumentNumber("TI", "TI-99999"));
        assertFalse(ValidationUtil.isValidDocumentNumber("RC", "123"));

        assertTrue(ValidationUtil.isValidDocumentNumber("Pasaporte", "A12345678"));
        assertTrue(ValidationUtil.isValidDocumentNumber("PPT", "PPT-9876543"));
        assertTrue(ValidationUtil.isValidDocumentNumber("CE", "CE123456"));
        assertTrue(ValidationUtil.isValidDocumentNumber("NES", "NES-2024-001"));
    }

    @Test
    @DisplayName("Blood type validation must match ABO and Rh factor")
    void testBloodTypeValidation() {
        assertTrue(ValidationUtil.isValidBloodType("O+"));
        assertTrue(ValidationUtil.isValidBloodType("o+"));
        assertTrue(ValidationUtil.isValidBloodType("O-"));
        assertTrue(ValidationUtil.isValidBloodType("A+"));
        assertTrue(ValidationUtil.isValidBloodType("A-"));
        assertTrue(ValidationUtil.isValidBloodType("B+"));
        assertTrue(ValidationUtil.isValidBloodType("B-"));
        assertTrue(ValidationUtil.isValidBloodType("AB+"));
        assertTrue(ValidationUtil.isValidBloodType("AB-"));

        assertFalse(ValidationUtil.isValidBloodType("C+"));
        assertFalse(ValidationUtil.isValidBloodType("roja"));
        assertFalse(ValidationUtil.isValidBloodType(""));
        assertFalse(ValidationUtil.isValidBloodType(null));
    }

    @Test
    @DisplayName("Student birth date validation must enforce valid age range")
    void testStudentBirthDateValidation() {
        LocalDate tenYearsAgo = LocalDate.now().minusYears(10);
        assertTrue(ValidationUtil.isValidStudentBirthDate(tenYearsAgo));

        assertFalse(ValidationUtil.isValidStudentBirthDate(LocalDate.now().plusDays(1)));
        assertFalse(ValidationUtil.isValidStudentBirthDate(LocalDate.now()));
        assertFalse(ValidationUtil.isValidStudentBirthDate(LocalDate.now().minusYears(1)));
        assertFalse(ValidationUtil.isValidStudentBirthDate(LocalDate.now().minusYears(60)));
        assertFalse(ValidationUtil.isValidStudentBirthDate(null));
    }

    @Test
    @DisplayName("Phone validation must reject letters and require minimum digits")
    void testPhoneValidation() {
        assertTrue(ValidationUtil.isValidPhone("+57 300 1234567"));
        assertTrue(ValidationUtil.isValidPhone("3101234567"));
        assertTrue(ValidationUtil.isValidPhone("(601) 2345678"));

        assertFalse(ValidationUtil.isValidPhone("300abc1234"));
        assertFalse(ValidationUtil.isValidPhone("tel-1234"));
        assertFalse(ValidationUtil.isValidPhone("12345"));
        assertFalse(ValidationUtil.isValidPhone(""));
        assertFalse(ValidationUtil.isValidPhone(null));
    }
}
