package com.littlehorizons.erp.util;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Scanner;

/**
 * Robust console input reader that validates user input, prevents infinite loops,
 * and handles data conversions cleanly.
 */
public final class ConsoleInputReader {

    private static final Scanner SCANNER = new Scanner(System.in);

    private ConsoleInputReader() {
        // Prevent instantiation
    }

    public static String readString(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = SCANNER.nextLine();
            if (input != null && !input.trim().isEmpty()) {
                return input.trim();
            }
            System.out.println("  [!] Error: El campo no puede estar vacío. Intente de nuevo.");
        }
    }

    public static String readOptionalString(String prompt) {
        System.out.print(prompt);
        String input = SCANNER.nextLine();
        return (input != null && !input.trim().isEmpty()) ? input.trim() : "";
    }

    public static int readInt(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String line = SCANNER.nextLine();
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
            String line = SCANNER.nextLine().replace(',', '.').trim();
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
            String line = SCANNER.nextLine().trim();
            if (ValidationUtil.isValidDate(line)) {
                return ValidationUtil.parseDate(line);
            }
            System.out.println("  [!] Error: Formato de fecha inválido. Debe ser AAAA-MM-DD (ej: 2024-03-15).");
        }
    }

    public static boolean readBoolean(String prompt) {
        while (true) {
            System.out.print(prompt + " (S/N): ");
            String line = SCANNER.nextLine().trim().toUpperCase();
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
        SCANNER.nextLine();
    }
}
