package com.littlehorizons.erp.config;

import com.littlehorizons.erp.util.LoggerUtil;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public final class DatabaseConfig {

    private static HikariDataSource dataSource;
    private static String configuredUrl;
    private static String configuredUser;

    static {
        initializeDataSource();
    }

    private DatabaseConfig() {
        
    }

    private static synchronized void initializeDataSource() {
        Properties props = new Properties();
        try (InputStream is = DatabaseConfig.class.getClassLoader().getResourceAsStream("database.properties")) {
            if (is != null) {
                props.load(is);
            } else {
                LoggerUtil.warn("database.properties not found in classpath. Using default properties.");
            }
        } catch (IOException e) {
            LoggerUtil.error("Failed to load database.properties", e);
        }

        String envUrl = System.getenv("DB_URL");
        String envUser = System.getenv("DB_USER");
        String envPassword = System.getenv("DB_PASSWORD");

        configuredUrl = envUrl != null ? envUrl : props.getProperty("db.url", "jdbc:mysql://localhost:3306/erp_little_horizons?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Bogota&characterEncoding=UTF-8");
        configuredUser = envUser != null ? envUser : props.getProperty("db.user", "root");
        String pass = envPassword != null ? envPassword : props.getProperty("db.password", "root");

        try {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(configuredUrl);
            config.setUsername(configuredUser);
            config.setPassword(pass);
            config.setMaximumPoolSize(Integer.parseInt(props.getProperty("db.pool.maximumPoolSize", "10")));
            config.setMinimumIdle(Integer.parseInt(props.getProperty("db.pool.minimumIdle", "2")));
            config.setIdleTimeout(Long.parseLong(props.getProperty("db.pool.idleTimeout", "30000")));
            config.setConnectionTimeout(Long.parseLong(props.getProperty("db.pool.connectionTimeout", "10000")));
            config.setPoolName("LittleHorizonsHikariPool");

            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

            dataSource = new HikariDataSource(config);
            LoggerUtil.info("Database connection pool initialized successfully for " + configuredUrl);
        } catch (Exception e) {
            LoggerUtil.error("Failed to initialize HikariCP connection pool: " + e.getMessage(), e);
        }
    }

    public static Connection getConnection() throws SQLException {
        if (dataSource == null || dataSource.isClosed()) {
            initializeDataSource();
        }
        if (dataSource == null) {
            throw new SQLException("Database pool is not initialized. Check database service and credentials.");
        }
        return dataSource.getConnection();
    }

    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            LoggerUtil.error("Database connectivity test failed: " + e.getMessage(), e);
            return false;
        }
    }

    public static synchronized boolean reconfigure(String url, String user, String password) {
        close();
        try {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(url);
            config.setUsername(user);
            config.setPassword(password);
            config.setMaximumPoolSize(10);
            config.setConnectionTimeout(5000);
            config.setPoolName("LittleHorizonsCustomPool");
            dataSource = new HikariDataSource(config);
            configuredUrl = url;
            configuredUser = user;
            return testConnection();
        } catch (Exception e) {
            LoggerUtil.error("Failed to reconfigure database connection: " + e.getMessage(), e);
            return false;
        }
    }

    public static String getConfiguredUrl() {
        return configuredUrl;
    }

    public static String getConfiguredUser() {
        return configuredUser;
    }

    public static synchronized void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            LoggerUtil.info("Database connection pool closed.");
        }
    }
}
