package com.littlehorizons.erp.util;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.regex.Pattern;

/**
 * Validation utility for validating inputs, business constraints,
 * and data types across the application.
 */
public final class ValidationUtil {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9+()\\s-]{7,20}$");
    private static final Pattern DOC_NUMBER_PATTERN = Pattern.compile("^[A-Za-z0-9-]{5,20}$");
    public static final DateTimeFormatter STANDARD_DATE_FORMATTER = DateTimeFormatter.ofPattern("uuuu-MM-dd")
            .withResolverStyle(ResolverStyle.STRICT);

    private static final BigDecimal MIN_GRADE = new BigDecimal("0.0");
    private static final BigDecimal MAX_GRADE = new BigDecimal("5.0");

    private ValidationUtil() {
        // Prevent instantiation
    }

    public static boolean isNotEmpty(String text) {
        return text != null && !text.trim().isEmpty();
    }

    public static boolean isValidEmail(String email) {
        if (!isNotEmpty(email)) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    public static boolean isValidPhone(String phone) {
        if (!isNotEmpty(phone)) {
            return false;
        }
        return PHONE_PATTERN.matcher(phone.trim()).matches();
    }

    public static boolean isValidDocumentNumber(String docNumber) {
        if (!isNotEmpty(docNumber)) {
            return false;
        }
        return DOC_NUMBER_PATTERN.matcher(docNumber.trim()).matches();
    }

    public static boolean isValidGrade(BigDecimal grade) {
        if (grade == null) {
            return false;
        }
        return grade.compareTo(MIN_GRADE) >= 0 && grade.compareTo(MAX_GRADE) <= 0;
    }

    public static boolean isValidDate(String dateStr) {
        if (!isNotEmpty(dateStr)) {
            return false;
        }
        try {
            LocalDate.parse(dateStr.trim(), STANDARD_DATE_FORMATTER);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    public static LocalDate parseDate(String dateStr) {
        if (!isValidDate(dateStr)) {
            return null;
        }
        return LocalDate.parse(dateStr.trim(), STANDARD_DATE_FORMATTER);
    }
}
