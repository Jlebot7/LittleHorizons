package com.littlehorizons.erp.util;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Set;
import java.util.regex.Pattern;

public final class ValidationUtil {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*\\.[A-Za-z]{2,}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9+()\\s-]{7,20}$");
    private static final Pattern DOC_NUMBER_PATTERN = Pattern.compile("^[A-Za-z0-9-]{5,20}$");
    private static final Pattern STRICT_NUMERIC_DOC_PATTERN = Pattern.compile("^[0-9]{7,10}$");
    private static final Pattern ALPHANUMERIC_DOC_PATTERN = Pattern.compile("^[A-Za-z0-9-]{5,20}$");
    private static final Pattern PERSON_NAME_PATTERN = Pattern.compile("^[a-zA-ZáéíóúÁÉÍÓÚñÑüÜ\\s'-]{2,100}$");

    public static final DateTimeFormatter STANDARD_DATE_FORMATTER = DateTimeFormatter.ofPattern("uuuu-MM-dd")
            .withResolverStyle(ResolverStyle.STRICT);

    private static final BigDecimal MIN_GRADE = new BigDecimal("0.0");
    private static final BigDecimal MAX_GRADE = new BigDecimal("5.0");

    private static final Set<String> STUDENT_DOC_TYPES = Set.of(
            "RC", "TI", "CC", "CE", "PASAPORTE", "PPT", "PEP", "NES", "VISA"
    );

    private static final Set<String> ADULT_DOC_TYPES = Set.of(
            "CC", "CE", "PASAPORTE", "PPT", "PEP", "VISA"
    );

    private static final Set<String> BLOOD_TYPES = Set.of(
            "O+", "O-", "A+", "A-", "B+", "B-", "AB+", "AB-"
    );

    private ValidationUtil() {
    }

    public static boolean isNotEmpty(String text) {
        return text != null && !text.trim().isEmpty();
    }

    public static boolean isValidPersonName(String name) {
        if (!isNotEmpty(name)) {
            return false;
        }
        String trimmed = name.trim();
        if (!PERSON_NAME_PATTERN.matcher(trimmed).matches()) {
            return false;
        }
        return trimmed.chars().anyMatch(Character::isLetter);
    }

    public static boolean isValidDocumentType(String docType, boolean isStudent) {
        if (!isNotEmpty(docType)) {
            return false;
        }
        String normalized = docType.trim().toUpperCase();
        return isStudent ? STUDENT_DOC_TYPES.contains(normalized) : ADULT_DOC_TYPES.contains(normalized);
    }

    public static Set<String> getStudentDocumentTypes() {
        return STUDENT_DOC_TYPES;
    }

    public static Set<String> getAdultDocumentTypes() {
        return ADULT_DOC_TYPES;
    }

    public static boolean isValidDocumentNumber(String docNumber) {
        if (!isNotEmpty(docNumber)) {
            return false;
        }
        return DOC_NUMBER_PATTERN.matcher(docNumber.trim()).matches();
    }

    public static boolean isValidDocumentNumber(String docType, String docNumber) {
        if (!isNotEmpty(docNumber)) {
            return false;
        }
        String trimmedNum = docNumber.trim();
        if (docType == null) {
            return isValidDocumentNumber(docNumber);
        }
        String type = docType.trim().toUpperCase();
        switch (type) {
            case "CC", "TI", "RC":
                return STRICT_NUMERIC_DOC_PATTERN.matcher(trimmedNum).matches();
            case "PASAPORTE", "CE", "PPT", "PEP", "NES", "VISA":
                return ALPHANUMERIC_DOC_PATTERN.matcher(trimmedNum).matches();
            default:
                return isValidDocumentNumber(docNumber);
        }
    }

    public static boolean isValidBloodType(String bloodType) {
        if (!isNotEmpty(bloodType)) {
            return false;
        }
        return BLOOD_TYPES.contains(bloodType.trim().toUpperCase());
    }

    public static boolean isValidStudentBirthDate(LocalDate birthDate) {
        if (birthDate == null) {
            return false;
        }
        LocalDate today = LocalDate.now();
        if (!birthDate.isBefore(today)) {
            return false;
        }
        int age = Period.between(birthDate, today).getYears();
        return age >= 3 && age <= 25;
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
        String trimmed = phone.trim();
        if (!PHONE_PATTERN.matcher(trimmed).matches()) {
            return false;
        }
        return trimmed.chars().filter(Character::isDigit).count() >= 7;
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
