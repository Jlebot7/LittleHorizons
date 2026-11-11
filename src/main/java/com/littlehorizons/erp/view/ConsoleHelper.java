package com.littlehorizons.erp.view;
public final class ConsoleHelper {

    private ConsoleHelper() {
    }

    public static void printHeader(String title, String subtitle) {
        int width = 56;
        System.out.println();
        System.out.println("╔" + "═".repeat(width - 2) + "╗");
        printCenteredLine(title, width);
        if (subtitle != null && !subtitle.isEmpty()) {
            printCenteredLine(subtitle, width);
        }
        System.out.println("╚" + "═".repeat(width - 2) + "╝");
        System.out.println();
    }

    public static void printSubHeader(String title) {
        System.out.println();
        System.out.println("─── " + title + " " + "─".repeat(Math.max(2, 50 - title.length())));
    }

    public static void printSuccess(String message) {
        System.out.println("\n  [✓ ÉXITO] " + message);
    }

    public static void printError(String message) {
        System.out.println("\n  [✗ ERROR] " + message);
    }

    public static void printWarning(String message) {
        System.out.println("\n  [⚠ AVISO] " + message);
    }

    public static void printInfo(String message) {
        System.out.println("  [ℹ INFO] " + message);
    }

    private static void printCenteredLine(String text, int totalWidth) {
        int contentWidth = totalWidth - 4;
        if (text.length() > contentWidth) {
            text = text.substring(0, contentWidth);
        }
        int leftPadding = (contentWidth - text.length()) / 2;
        int rightPadding = contentWidth - text.length() - leftPadding;
        System.out.println("║ " + " ".repeat(leftPadding) + text + " ".repeat(rightPadding) + " ║");
    }
}
