package com.littlehorizons.erp.util;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
public final class LoggerUtil {

    private static final String LOG_FILE_PATH = "error_log.txt";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    private static final Object LOCK = new Object();

    private LoggerUtil() {
    }

    public enum LogLevel {
        INFO,
        WARN,
        ERROR,
        FATAL
    }
    public static void info(String message) {
        log(LogLevel.INFO, message, null);
    }
    public static void warn(String message) {
        log(LogLevel.WARN, message, null);
    }
    public static void error(String message) {
        log(LogLevel.ERROR, message, null);
    }
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
