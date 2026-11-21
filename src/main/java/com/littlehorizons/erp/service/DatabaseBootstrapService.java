package com.littlehorizons.erp.service;

import com.littlehorizons.erp.config.DatabaseConfig;
import com.littlehorizons.erp.util.LoggerUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DatabaseBootstrapService implements IDatabaseBootstrapService {

    private static final String DEFAULT_HOST_URL = "jdbc:mysql://localhost:3306/?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Bogota&characterEncoding=UTF-8";
    private static final String DB_NAME = "erp_little_horizons";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASS = "root";

    @Override
    public boolean bootstrapDatabase() {
        LoggerUtil.info("Iniciando auto-verificación de base de datos...");
        if (DatabaseConfig.testConnection()) {
            boolean tablesOk = verifyAndSeedTables();
            if (tablesOk) {
                LoggerUtil.info("Base de datos conectada y verificada exitosamente.");
                return true;
            }
        }

        tryStartLocalService();

        boolean serverOk = ensureDatabaseCreated();
        if (serverOk) {
            verifyAndSeedTables();
        }

        boolean finalOk = DatabaseConfig.testConnection();
        if (finalOk) {
            LoggerUtil.info("Auto-arranque de base de datos completado exitosamente.");
        } else {
            LoggerUtil.warn("No fue posible auto-conectar con MySQL en localhost:3306. El sistema continuará en modo resiliente.");
        }
        return finalOk;
    }

    @Override
    public boolean ensureSchemaAndTablesExist() {
        return ensureDatabaseCreated() && verifyAndSeedTables();
    }

    private void tryStartLocalService() {
        String os = System.getProperty("os.name", "").toLowerCase();
        if (!os.contains("win")) {
            return;
        }

        try {
            ProcessBuilder wslPb = new ProcessBuilder("wsl", "-e", "service", "mysql", "start");
            wslPb.redirectErrorStream(true);
            Process wslProcess = wslPb.start();
            wslProcess.waitFor();
        } catch (Exception ignored) {
        }

        String[] serviceNames = {"MySQL80", "MySQL", "MySQL84", "mariadb"};
        for (String serviceName : serviceNames) {
            try {
                ProcessBuilder pb = new ProcessBuilder("net", "start", serviceName);
                pb.redirectErrorStream(true);
                Process process = pb.start();
                process.waitFor();
            } catch (Exception ignored) {
            }
        }
    }

    private boolean ensureDatabaseCreated() {
        String rootUser = System.getenv("DB_USER") != null ? System.getenv("DB_USER") : DatabaseConfig.getConfiguredUser();
        if (rootUser == null || rootUser.isBlank()) {
            rootUser = DEFAULT_USER;
        }
        String rootPass = System.getenv("DB_PASSWORD") != null ? System.getenv("DB_PASSWORD") : DEFAULT_PASS;

        try (Connection conn = DriverManager.getConnection(DEFAULT_HOST_URL, rootUser, rootPass);
             Statement stmt = conn.createStatement()) {

            stmt.execute("CREATE DATABASE IF NOT EXISTS " + DB_NAME + " CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
            LoggerUtil.info("Verificación de base de datos '" + DB_NAME + "' completada en servidor MySQL.");
            return true;
        } catch (SQLException e) {
            LoggerUtil.warn("No se pudo conectar a nivel de servidor MySQL: " + e.getMessage());
            return false;
        }
    }

    private boolean verifyAndSeedTables() {
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement()) {

            boolean studentsTableExists = false;
            try (ResultSet rs = stmt.executeQuery("SHOW TABLES LIKE 'estudiantes'")) {
                if (rs.next()) {
                    studentsTableExists = true;
                }
            }

            if (!studentsTableExists) {
                LoggerUtil.info("Tablas maestras no detectadas. Procediendo a ejecutar script de creación y poblado...");
                return executeSqlScript(conn);
            }
            return true;
        } catch (SQLException e) {
            LoggerUtil.warn("Error al verificar tablas en " + DB_NAME + ": " + e.getMessage());
            return false;
        }
    }

    private boolean executeSqlScript(Connection conn) {
        List<String> statements = loadSqlStatements();
        if (statements.isEmpty()) {
            LoggerUtil.warn("No se encontraron sentencias SQL válidas en el script de base de datos.");
            return false;
        }

        try (Statement stmt = conn.createStatement()) {
            for (String sql : statements) {
                String trimmed = sql.trim();
                if (!trimmed.isEmpty()) {
                    try {
                        stmt.execute(trimmed);
                    } catch (SQLException ex) {
                        LoggerUtil.warn("Advertencia ejecutando sentencia SQL: " + ex.getMessage());
                    }
                }
            }
            LoggerUtil.info("Script SQL ejecutado con éxito. Total sentencias procesadas: " + statements.size());
            return true;
        } catch (SQLException e) {
            LoggerUtil.error("Fallo general ejecutando script de base de datos", e);
            return false;
        }
    }

    private List<String> loadSqlStatements() {
        List<String> list = new ArrayList<>();
        File scriptFile = findScriptFile();
        if (scriptFile != null && scriptFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(scriptFile, StandardCharsets.UTF_8))) {
                return parseScript(reader);
            } catch (Exception e) {
                LoggerUtil.error("Error leyendo script desde archivo: " + scriptFile.getAbsolutePath(), e);
            }
        }

        try (InputStream is = getClass().getClassLoader().getResourceAsStream("database.sql")) {
            if (is != null) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                    return parseScript(reader);
                }
            }
        } catch (Exception e) {
            LoggerUtil.error("Error leyendo script desde classpath", e);
        }

        return list;
    }

    private File findScriptFile() {
        String[] candidatePaths = {
                "docs/database.sql",
                "database.sql",
                "../docs/database.sql"
        };
        for (String p : candidatePaths) {
            File f = new File(p);
            if (f.exists()) {
                return f;
            }
        }
        return null;
    }

    private List<String> parseScript(BufferedReader reader) throws Exception {
        List<String> statements = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            String trimmed = line.trim();
            if (trimmed.startsWith("--") || trimmed.startsWith("/*") || trimmed.isEmpty()) {
                continue;
            }

            current.append(line).append("\n");
            if (trimmed.endsWith(";")) {
                statements.add(current.toString());
                current.setLength(0);
            }
        }

        if (current.length() > 0 && !current.toString().trim().isEmpty()) {
            statements.add(current.toString());
        }
        return statements;
    }
}
