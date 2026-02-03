package com.accounting.api.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Test Configuration Reader
 * Reads configuration from properties files
 */
public class TestConfig {

    private static final Properties properties = new Properties();
    private static final String CONFIG_FILE = "config/test-config.properties";

    static {
        loadProperties();
    }

    private static void loadProperties() {
        try (InputStream input = TestConfig.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (input != null) {
                properties.load(input);
            } else {
                // Load from environment or use defaults
                loadDefaults();
            }
        } catch (IOException e) {
            loadDefaults();
        }

        // Override with system properties
        properties.putAll(System.getProperties());
    }

    private static void loadDefaults() {
        properties.setProperty("base.url", "http://localhost:8080");
        properties.setProperty("api.base.path", "/api");
        properties.setProperty("username", "admin");
        properties.setProperty("password", "admin123");
        properties.setProperty("connection.timeout", "30000");
        properties.setProperty("read.timeout", "30000");
    }

    public static String getBaseUrl() {
        return getProperty("base.url", "http://localhost:8080");
    }

    public static String getApiBasePath() {
        return getProperty("api.base.path", "/api");
    }

    public static String getUsername() {
        return getProperty("username", "admin");
    }

    public static String getPassword() {
        return getProperty("password", "admin123");
    }

    public static int getConnectionTimeout() {
        return Integer.parseInt(getProperty("connection.timeout", "30000"));
    }

    public static int getReadTimeout() {
        return Integer.parseInt(getProperty("read.timeout", "30000"));
    }

    public static String getProperty(String key) {
        return properties.getProperty(key);
    }

    public static String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    public static String getFullApiUrl() {
        return getBaseUrl() + getApiBasePath();
    }
}