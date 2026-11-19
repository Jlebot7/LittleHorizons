package com.littlehorizons.erp;

import com.littlehorizons.erp.model.academic.Student;
import com.littlehorizons.erp.model.academic.Teacher;
import com.littlehorizons.erp.model.academic.Guardian;
import com.littlehorizons.erp.model.academic.AcademicRecord;
import com.littlehorizons.erp.model.finance.Payment;
import com.littlehorizons.erp.service.AcademicService;
import com.littlehorizons.erp.service.FinanceService;
import com.littlehorizons.erp.service.ExtracurricularService;
import com.littlehorizons.erp.service.ResourceService;
import com.littlehorizons.erp.util.ValidationUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class DataInputResilienceAuditTest {

    private final AcademicService academicService = new AcademicService();
    private final FinanceService financeService = new FinanceService();
    private final ExtracurricularService extracurricularService = new ExtracurricularService();
    private final ResourceService resourceService = new ResourceService();

    @Test
    @DisplayName("Auditoría: Validar nombres con caracteres no alfabéticos, nulos o vacíos")
    void auditPersonNameValidation() {
        assertFalse(ValidationUtil.isValidPersonName(null), "Null name must be rejected");
        assertFalse(ValidationUtil.isValidPersonName(""), "Empty name must be rejected");
        assertFalse(ValidationUtil.isValidPersonName("   "), "Whitespace name must be rejected");
        assertFalse(ValidationUtil.isValidPersonName("Juan123"), "Name with numbers must be rejected");
        assertFalse(ValidationUtil.isValidPersonName("Ana_Maria"), "Name with underscores must be rejected");
        assertFalse(ValidationUtil.isValidPersonName("J"), "Single character name must be rejected");
        assertFalse(ValidationUtil.isValidPersonName("<script>alert(1)</script>"), "HTML/XSS name must be rejected");

        assertTrue(ValidationUtil.isValidPersonName("José"), "Name with accent marks must be accepted");
        assertTrue(ValidationUtil.isValidPersonName("María-Fernanda"), "Hyphenated name must be accepted");
        assertTrue(ValidationUtil.isValidPersonName("O'Connor"), "Apostrophe name must be accepted");
        assertTrue(ValidationUtil.isValidPersonName("De La Rosa"), "Multi-word name must be accepted");
    }

    @ParameterizedTest
    @ValueSource(strings = {"12345", "12345678901", "abcdef", "1234a567", ""})
    @DisplayName("Auditoría: Validar documentos numéricos estrictos (CC, TI, RC)")
    void auditStrictNumericDocuments(String invalidDoc) {
        assertFalse(ValidationUtil.isValidDocumentNumber("CC", invalidDoc), "Invalid CC: " + invalidDoc);
        assertFalse(ValidationUtil.isValidDocumentNumber("TI", invalidDoc), "Invalid TI: " + invalidDoc);
        assertFalse(ValidationUtil.isValidDocumentNumber("RC", invalidDoc), "Invalid RC: " + invalidDoc);
    }

    @ParameterizedTest
    @ValueSource(strings = {"1234567", "12345678", "123456789", "1234567890"})
    @DisplayName("Auditoría: Aceptar documentos numéricos válidos (7 a 10 dígitos)")
    void auditValidStrictNumericDocuments(String validDoc) {
        assertTrue(ValidationUtil.isValidDocumentNumber("CC", validDoc), "Valid CC: " + validDoc);
        assertTrue(ValidationUtil.isValidDocumentNumber("TI", validDoc), "Valid TI: " + validDoc);
        assertTrue(ValidationUtil.isValidDocumentNumber("RC", validDoc), "Valid RC: " + validDoc);
    }

    @Test
    @DisplayName("Auditoría: Validar fechas de nacimiento fuera del rango estudiantil (3 a 25 años)")
    void auditStudentBirthDateLimits() {
        assertFalse(ValidationUtil.isValidStudentBirthDate(null), "Null birth date must be rejected");
        assertFalse(ValidationUtil.isValidStudentBirthDate(LocalDate.now()), "Birth date today must be rejected");
        assertFalse(ValidationUtil.isValidStudentBirthDate(LocalDate.now().plusDays(1)), "Future date must be rejected");
        assertFalse(ValidationUtil.isValidStudentBirthDate(LocalDate.now().minusYears(2)), "2-year-old child must be rejected");
        assertFalse(ValidationUtil.isValidStudentBirthDate(LocalDate.now().minusYears(26)), "26-year-old adult must be rejected");

        assertTrue(ValidationUtil.isValidStudentBirthDate(LocalDate.now().minusYears(5)), "5-year-old must be accepted");
        assertTrue(ValidationUtil.isValidStudentBirthDate(LocalDate.now().minusYears(15)), "15-year-old must be accepted");
        assertTrue(ValidationUtil.isValidStudentBirthDate(LocalDate.now().minusYears(24)), "24-year-old must be accepted");
    }

    @ParameterizedTest
    @ValueSource(strings = {"not-an-email", "@missinguser.com", "user@", "user@domain", "user@.com", ""})
    @DisplayName("Auditoría: Validar correos electrónicos inválidos")
    void auditInvalidEmails(String invalidEmail) {
        assertFalse(ValidationUtil.isValidEmail(invalidEmail), "Invalid email: " + invalidEmail);
    }

    @ParameterizedTest
    @ValueSource(strings = {"john@example.com", "maria.perez@colegio.edu.co", "user+tag@domain.org"})
    @DisplayName("Auditoría: Aceptar correos electrónicos válidos")
    void auditValidEmails(String validEmail) {
        assertTrue(ValidationUtil.isValidEmail(validEmail), "Valid email: " + validEmail);
    }

    @ParameterizedTest
    @ValueSource(strings = {"123", "abcdef", "12345a", "000", ""})
    @DisplayName("Auditoría: Validar teléfonos no válidos (mínimo 7 dígitos)")
    void auditInvalidPhones(String invalidPhone) {
        assertFalse(ValidationUtil.isValidPhone(invalidPhone), "Invalid phone: " + invalidPhone);
    }

    @ParameterizedTest
    @ValueSource(strings = {"3001234567", "+57 300 123 4567", "(601) 2345678", "7654321"})
    @DisplayName("Auditoría: Aceptar teléfonos válidos")
    void auditValidPhones(String validPhone) {
        assertTrue(ValidationUtil.isValidPhone(validPhone), "Valid phone: " + validPhone);
    }

    @ParameterizedTest
    @ValueSource(strings = {"Z+", "O", "AB", "C-", "123", ""})
    @DisplayName("Auditoría: Validar tipos de sangre inválidos")
    void auditInvalidBloodTypes(String invalidBloodType) {
        assertFalse(ValidationUtil.isValidBloodType(invalidBloodType), "Invalid blood type: " + invalidBloodType);
    }

    @ParameterizedTest
    @ValueSource(strings = {"O+", "O-", "A+", "A-", "B+", "B-", "AB+", "AB-"})
    @DisplayName("Auditoría: Aceptar tipos de sangre válidos")
    void auditValidBloodTypes(String validBloodType) {
        assertTrue(ValidationUtil.isValidBloodType(validBloodType), "Valid blood type: " + validBloodType);
    }

    @ParameterizedTest
    @ValueSource(strings = {"-0.1", "5.1", "-5.0", "10.0", "100.0"})
    @DisplayName("Auditoría: Validar calificaciones fuera de escala (0.0 a 5.0)")
    void auditInvalidGrades(String invalidGrade) {
        assertFalse(ValidationUtil.isValidGrade(new BigDecimal(invalidGrade)), "Invalid grade: " + invalidGrade);
    }

    @ParameterizedTest
    @ValueSource(strings = {"0.0", "2.5", "3.0", "4.8", "5.0"})
    @DisplayName("Auditoría: Aceptar calificaciones dentro de escala (0.0 a 5.0)")
    void auditValidGrades(String validGrade) {
        assertTrue(ValidationUtil.isValidGrade(new BigDecimal(validGrade)), "Valid grade: " + validGrade);
    }

    @Test
    @DisplayName("Auditoría: AcademicService debe rechazar estudiantes nulos o corruptos con IllegalArgumentException descriptivo")
    void auditAcademicServiceStudentValidation() {
        assertThrows(IllegalArgumentException.class, () -> academicService.registerStudent(null));

        Student corrupt1 = new Student();
        corrupt1.setFirstName("");
        IllegalArgumentException ex1 = assertThrows(IllegalArgumentException.class, () -> academicService.registerStudent(corrupt1));
        assertTrue(ex1.getMessage().contains("primer nombre"), "Must mention first name");

        Student corrupt2 = new Student();
        corrupt2.setFirstName("Valido");
        corrupt2.setFirstLastName("123Invalido");
        IllegalArgumentException ex2 = assertThrows(IllegalArgumentException.class, () -> academicService.registerStudent(corrupt2));
        assertTrue(ex2.getMessage().contains("primer apellido"), "Must mention first last name");

        Student corrupt3 = new Student();
        corrupt3.setFirstName("Valido");
        corrupt3.setFirstLastName("Valido");
        corrupt3.setDocumentType("PASAPORTE_XYZ");
        IllegalArgumentException ex3 = assertThrows(IllegalArgumentException.class, () -> academicService.registerStudent(corrupt3));
        assertTrue(ex3.getMessage().contains("Tipo de documento"), "Must mention document type");

        Student corrupt4 = new Student();
        corrupt4.setFirstName("Valido");
        corrupt4.setFirstLastName("Valido");
        corrupt4.setDocumentType("TI");
        corrupt4.setDocumentNumber("ABC-123");
        IllegalArgumentException ex4 = assertThrows(IllegalArgumentException.class, () -> academicService.registerStudent(corrupt4));
        assertTrue(ex4.getMessage().contains("número de documento"), "Must mention document number");
    }

    @Test
    @DisplayName("Auditoría: FinanceService debe rechazar pagos con montos <= 0 o nulos")
    void auditFinanceServicePaymentValidation() {
        Payment pNullAmount = new Payment();
        pNullAmount.setAmountPaid(null);
        assertThrows(IllegalArgumentException.class, () -> financeService.processPaymentAndInvoice(pNullAmount));

        Payment pZeroAmount = new Payment();
        pZeroAmount.setAmountPaid(BigDecimal.ZERO);
        assertThrows(IllegalArgumentException.class, () -> financeService.processPaymentAndInvoice(pZeroAmount));

        Payment pNegativeAmount = new Payment();
        pNegativeAmount.setAmountPaid(new BigDecimal("-50000"));
        assertThrows(IllegalArgumentException.class, () -> financeService.processPaymentAndInvoice(pNegativeAmount));
    }

    @Test
    @DisplayName("Auditoría: AcademicService debe rechazar calificaciones inválidas")
    void auditAcademicServiceGradeValidation() {
        AcademicRecord recOver = new AcademicRecord(null, 1, 1, 1, 2024, "I", new BigDecimal("5.5"), "", LocalDate.now());
        assertThrows(IllegalArgumentException.class, () -> academicService.recordGrade(recOver));

        AcademicRecord recNeg = new AcademicRecord(null, 1, 1, 1, 2024, "I", new BigDecimal("-1.0"), "", LocalDate.now());
        assertThrows(IllegalArgumentException.class, () -> academicService.recordGrade(recNeg));
    }
}
