package com.littlehorizons.erp.util;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Scanner;

public final class ConsoleInputReader {

    private static final Scanner SCANNER = new Scanner(System.in);

    private ConsoleInputReader() {
    }

    private static String nextLineOrNull() {
        if (!SCANNER.hasNextLine()) {
            return null;
        }
        return SCANNER.nextLine();
    }

    public static String readString(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = nextLineOrNull();
            if (input == null) {
                return "";
            }
            if (!input.trim().isEmpty()) {
                return input.trim();
            }
            System.out.println("  [!] Error: El campo no puede estar vacío. Intente de nuevo.");
        }
    }

    public static String readOptionalString(String prompt) {
        System.out.print(prompt);
        String input = nextLineOrNull();
        if (input == null) {
            return "";
        }
        return input.trim();
    }

    public static String readPersonName(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = nextLineOrNull();
            if (input == null) {
                return "";
            }
            String trimmed = input.trim();
            if (ValidationUtil.isValidPersonName(trimmed)) {
                return trimmed;
            }
            System.out.println("  [!] Error: El nombre no puede contener números ni caracteres especiales (solo letras, espacios, guiones y tildes).");
        }
    }

    public static String readOptionalPersonName(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = nextLineOrNull();
            if (input == null) {
                return "";
            }
            String trimmed = input.trim();
            if (trimmed.isEmpty()) {
                return "";
            }
            if (ValidationUtil.isValidPersonName(trimmed)) {
                return trimmed;
            }
            System.out.println("  [!] Error: Si ingresa un nombre/apellido, este no puede contener números ni caracteres inválidos.");
        }
    }

    public static String readStudentDocumentType(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = nextLineOrNull();
            if (input == null) {
                return "TI";
            }
            String trimmed = input.trim().toUpperCase();
            if (ValidationUtil.isValidDocumentType(trimmed, true)) {
                return trimmed;
            }
            System.out.println("  [!] Error: Tipo de documento no válido para estudiante. Opciones admitidas: RC, TI, CC, CE, Pasaporte, PPT, PEP, NES, VISA.");
        }
    }

    public static String readAdultDocumentType(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = nextLineOrNull();
            if (input == null) {
                return "CC";
            }
            String trimmed = input.trim().toUpperCase();
            if (ValidationUtil.isValidDocumentType(trimmed, false)) {
                return trimmed;
            }
            System.out.println("  [!] Error: Tipo de documento no válido. Opciones admitidas: CC, CE, Pasaporte, PPT, PEP, VISA.");
        }
    }

    public static String readDocumentNumber(String prompt, String docType) {
        while (true) {
            System.out.print(prompt);
            String input = nextLineOrNull();
            if (input == null) {
                return "";
            }
            String trimmed = input.trim();
            if (ValidationUtil.isValidDocumentNumber(docType, trimmed)) {
                return trimmed;
            }
            String type = docType != null ? docType.trim().toUpperCase() : "";
            if (type.equals("CC") || type.equals("TI") || type.equals("RC")) {
                System.out.println("  [!] Error: Para " + type + " el número de documento debe contener exclusivamente de 7 a 10 dígitos numéricos (sin letras).");
            } else {
                System.out.println("  [!] Error: Formato de documento inválido (debe contener entre 5 y 20 caracteres alfanuméricos).");
            }
        }
    }

    public static String readGender(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = nextLineOrNull();
            if (input == null) {
                return "M";
            }
            String trimmed = input.trim().toUpperCase();
            if (trimmed.equals("M") || trimmed.equals("F") || trimmed.equals("OTRO") || trimmed.equals("O")) {
                if (trimmed.equals("O")) {
                    return "Otro";
                }
                if (trimmed.equals("OTRO")) {
                    return "Otro";
                }
                return trimmed;
            }
            System.out.println("  [!] Error: Género inválido. Opciones permitidas: M (Masculino), F (Femenino), Otro.");
        }
    }

    public static String readAcademicPeriod(String prompt, boolean allowFinal, boolean allowAnnual) {
        while (true) {
            System.out.print(prompt);
            String input = nextLineOrNull();
            if (input == null) {
                return "I";
            }
            String trimmed = input.trim().toUpperCase();
            if (trimmed.equals("I") || trimmed.equals("II") || trimmed.equals("III") || trimmed.equals("IV")) {
                return trimmed;
            }
            if (allowFinal && trimmed.equals("FINAL")) {
                return "Final";
            }
            if (allowAnnual && (trimmed.equals("ANUAL") || trimmed.equals("ANNUAL"))) {
                return "Anual";
            }
            StringBuilder sb = new StringBuilder("  [!] Error: Período inválido. Valores válidos: I, II, III, IV");
            if (allowFinal) sb.append(", Final");
            if (allowAnnual) sb.append(", Anual");
            sb.append(".");
            System.out.println(sb.toString());
        }
    }

    public static String readPhone(String prompt, boolean optional) {
        while (true) {
            System.out.print(prompt);
            String input = nextLineOrNull();
            if (input == null) {
                return "";
            }
            String trimmed = input.trim();
            if (trimmed.isEmpty()) {
                if (optional) {
                    return "";
                }
                System.out.println("  [!] Error: El teléfono es obligatorio.");
                continue;
            }
            if (ValidationUtil.isValidPhone(trimmed)) {
                return trimmed;
            }
            System.out.println("  [!] Error: Teléfono inválido. No debe contener letras y debe tener al menos 7 dígitos numéricos.");
        }
    }

    public static String readEmail(String prompt, boolean optional) {
        while (true) {
            System.out.print(prompt);
            String input = nextLineOrNull();
            if (input == null) {
                return "";
            }
            String trimmed = input.trim();
            if (trimmed.isEmpty()) {
                if (optional) {
                    return "";
                }
                System.out.println("  [!] Error: El correo electrónico es obligatorio.");
                continue;
            }
            if (ValidationUtil.isValidEmail(trimmed)) {
                return trimmed;
            }
            System.out.println("  [!] Error: Formato de correo electrónico inválido (ejemplo: usuario@dominio.com).");
        }
    }

    public static String readBloodType(String prompt, boolean optional) {
        while (true) {
            System.out.print(prompt);
            String input = nextLineOrNull();
            if (input == null) {
                return "";
            }
            String trimmed = input.trim().toUpperCase();
            if (trimmed.isEmpty()) {
                if (optional) {
                    return "";
                }
                System.out.println("  [!] Error: El tipo de sangre es obligatorio.");
                continue;
            }
            if (ValidationUtil.isValidBloodType(trimmed)) {
                return trimmed;
            }
            System.out.println("  [!] Error: Tipo de sangre no reconocido. Valores válidos: O+, O-, A+, A-, B+, B-, AB+, AB-.");
        }
    }

    public static LocalDate readStudentBirthDate(String prompt) {
        while (true) {
            LocalDate date = readDate(prompt);
            if (ValidationUtil.isValidStudentBirthDate(date)) {
                return date;
            }
            System.out.println("  [!] Error: La fecha de nacimiento debe estar en el pasado y corresponder a un estudiante entre 3 y 25 años.");
        }
    }

    public static int readInt(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String line = nextLineOrNull();
            if (line == null) {
                return max;
            }
            try {
                int value = Integer.parseInt(line.trim());
                if (value >= min && value <= max) {
                    return value;
                }
                System.out.printf("  [!] Error: El número debe estar entre %d y %d.%n", min, max);
            } catch (NumberFormatException e) {
                System.out.println("  [!] Error: Ingrese un número entero válido.");
            }
        }
    }

    public static BigDecimal readBigDecimal(String prompt, BigDecimal min, BigDecimal max) {
        while (true) {
            System.out.print(prompt);
            String line = nextLineOrNull();
            if (line == null) {
                return min;
            }
            line = line.replace(',', '.').trim();
            try {
                BigDecimal value = new BigDecimal(line);
                if (value.compareTo(min) >= 0 && value.compareTo(max) <= 0) {
                    return value;
                }
                System.out.printf("  [!] Error: El valor debe estar entre %s y %s.%n", min.toPlainString(), max.toPlainString());
            } catch (NumberFormatException e) {
                System.out.println("  [!] Error: Ingrese una cantidad numérica válida (ej: 4.5 o 350000).");
            }
        }
    }

    public static LocalDate readDate(String prompt) {
        while (true) {
            System.out.print(prompt + " (YYYY-MM-DD): ");
            String line = nextLineOrNull();
            if (line == null) {
                return LocalDate.now();
            }
            line = line.trim();
            if (ValidationUtil.isValidDate(line)) {
                return ValidationUtil.parseDate(line);
            }
            System.out.println("  [!] Error: Formato de fecha inválido. Debe ser AAAA-MM-DD (ej: 2024-03-15).");
        }
    }

    public static boolean readBoolean(String prompt) {
        while (true) {
            System.out.print(prompt + " (S/N): ");
            String line = nextLineOrNull();
            if (line == null) {
                return false;
            }
            line = line.trim().toUpperCase();
            if (line.equals("S") || line.equals("SI") || line.equals("Y") || line.equals("YES")) {
                return true;
            }
            if (line.equals("N") || line.equals("NO")) {
                return false;
            }
            System.out.println("  [!] Error: Por favor responda con 'S' (Sí) o 'N' (No).");
        }
    }

    public static void waitForEnter() {
        System.out.print("\nPresione [ENTER] para continuar...");
        nextLineOrNull();
    }
}