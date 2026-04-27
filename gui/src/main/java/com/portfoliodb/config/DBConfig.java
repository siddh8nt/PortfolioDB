package com.portfoliodb.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class DBConfig {
    private static final String DEFAULT_DB_NAME = "portfoliodb";
    private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/portfoliodb?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASSWORD = "root";
    private static final Map<String, String> FILE_CONFIG = loadFileConfig();

    private DBConfig() {
    }

    public static String getUrl() {
        String envUrl = readSystemSetting("DB_URL");
        if (envUrl != null) {
            return envUrl;
        }

        String fileUrl = FILE_CONFIG.get("DB_URL");
        if (fileUrl != null && !fileUrl.isBlank()) {
            return fileUrl;
        }

        return DEFAULT_URL;
    }

    public static String getUser() {
        return getOrDefault("DB_USER", DEFAULT_USER);
    }

    public static String getPassword() {
        return getOrDefault("DB_PASSWORD", DEFAULT_PASSWORD);
    }

    private static String getOrDefault(String key, String fallback) {
        String value = readSystemSetting(key);
        if (value != null) {
            return value;
        }

        String fileValue = FILE_CONFIG.get(key);
        if (fileValue != null && !fileValue.isBlank()) {
            return fileValue;
        }

        return fallback;
    }

    private static String readSystemSetting(String key) {
        String systemProperty = System.getProperty(key);
        if (systemProperty != null && !systemProperty.isBlank()) {
            return systemProperty;
        }

        String envValue = System.getenv(key);
        if (envValue != null && !envValue.isBlank()) {
            return envValue;
        }

        return null;
    }

    private static Map<String, String> loadFileConfig() {
        Map<String, String> config = new HashMap<>();

        for (Path path : credentialFileCandidates()) {
            if (!Files.exists(path)) {
                continue;
            }

            try {
                parseCredentialFile(path, config);
                if (config.containsKey("DB_URL") && config.containsKey("DB_USER") && config.containsKey("DB_PASSWORD")) {
                    break;
                }
            } catch (IOException ignored) {
                // Keep defaults if the optional credentials file cannot be read.
            }
        }

        return config;
    }

    private static List<Path> credentialFileCandidates() {
        return List.of(
                Path.of("db_cloud_cred", "cred.txt"),
                Path.of("..", "db_cloud_cred", "cred.txt"),
                Path.of("..", "..", "db_cloud_cred", "cred.txt")
        );
    }

    private static void parseCredentialFile(Path file, Map<String, String> config) throws IOException {
        String host = null;
        String port = null;
        String dbName = DEFAULT_DB_NAME;

        for (String rawLine : Files.readAllLines(file)) {
            String line = rawLine.trim();
            if (line.isBlank() || line.startsWith("#") || !line.contains(":")) {
                continue;
            }

            String[] parts = line.split(":", 2);
            String key = parts[0].trim().toLowerCase();
            String value = parts[1].trim();
            if (value.isBlank()) {
                continue;
            }

            switch (key) {
                case "url":
                    config.put("DB_URL", value);
                    break;
                case "name":
                case "host":
                    host = value;
                    break;
                case "port":
                    port = value;
                    break;
                case "database":
                case "dbname":
                    dbName = value;
                    break;
                case "username":
                case "user":
                    config.put("DB_USER", value);
                    break;
                case "password":
                    config.put("DB_PASSWORD", value);
                    break;
                default:
                    break;
            }
        }

        if (!config.containsKey("DB_URL") && host != null && port != null) {
            config.put("DB_URL", "jdbc:mysql://" + host + ":" + port + "/" + dbName
                    + "?useSSL=true&requireSSL=true&verifyServerCertificate=false&allowPublicKeyRetrieval=true&serverTimezone=UTC");
        }
    }
}
