package com.littlehorizons.erp.util;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Thread-safe logging utility that persists application errors, warnings,
 * and operational diagnostics to 'error_log.txt'.
 */
public final class LoggerUtil {

    private static final String LOG_FILE_PATH = "error_log.txt";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    private static final Object LOCK = new Object();

    private LoggerUtil() {
        // Utility class, prevent instantiation
    }

    public enum LogLevel {
        INFO,
        WARN,
        ERROR,
        FATAL
    }

    /**
     * Logs an informational message.
     *
     * @param message Message description
     */
    public static void info(String message) {
        log(LogLevel.INFO, message, null);
    }

    /**
     * Logs a warning message.
     *
     * @param message Warning description
     */
    public static void warn(String message) {
        log(LogLevel.WARN, message, null);
    }

    /**
     * Logs an error message without exception.
     *
     * @param message Error description
     */
    public static void error(String message) {
        log(LogLevel.ERROR, message, null);
    }

    /**
     * Logs an error message with associated throwable exception.
     *
     * @param message Error description
     * @param throwable Exception caught
     */
    public static void error(String message, Throwable throwable) {
        log(LogLevel.ERROR, message, throwable);
    }

    private static void log(LogLevel level, String message, Throwable throwable) {
        String timestamp = LocalDateTime.now().format(DATE_FORMATTER);
        String threadName = Thread.currentThread().getName();
        StringBuilder logBuilder = new StringBuilder();

        logBuilder.append(String.format("[%s] [%s] [%s] %s%n", timestamp, level, threadName, message));

        if (throwable != null) {
            StringWriter sw = new StringWriter();
            try (PrintWriter pw = new PrintWriter(sw)) {
                throwable.printStackTrace(pw);
            }
            logBuilder.append(sw);
        }

        synchronized (LOCK) {
            try (FileWriter fileWriter = new FileWriter(LOG_FILE_PATH, true)) {
                fileWriter.write(logBuilder.toString());
            } catch (IOException e) {
                System.err.println("CRITICAL: Failed to write to log file: " + e.getMessage());
            }
        }
    }
}
