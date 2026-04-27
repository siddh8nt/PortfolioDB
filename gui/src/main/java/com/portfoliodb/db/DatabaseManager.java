package com.portfoliodb.db;

import com.portfoliodb.config.DBConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class DatabaseManager {
    private static final Pattern MYSQL_HOST_PATTERN = Pattern.compile("(jdbc:mysql://)([^/:?]+)(.*)");
    private static final String RAILWAY_PROXY_HOST = "interchange.proxy.rlwy.net";
    private static final Object LOCK = new Object();

    private static Connection connection;

    private DatabaseManager() {
    }

    public static Connection getConnection() throws SQLException {
        synchronized (LOCK) {
            if (connection == null || connection.isClosed() || !isConnectionAlive(connection)) {
                connection = createConnectionWithRetry();
            }
            return connection;
        }
    }

    public static void closeConnection() {
        synchronized (LOCK) {
            if (connection == null) {
                return;
            }
            try {
                connection.close();
            } catch (SQLException ignored) {
                // Best-effort cleanup.
            } finally {
                connection = null;
            }
        }
    }

    private static boolean isConnectionAlive(Connection candidate) {
        try {
            return candidate.isValid(2);
        } catch (SQLException ex) {
            return false;
        }
    }

    private static Connection createConnectionWithRetry() throws SQLException {
        String url = DBConfig.getUrl();
        String user = DBConfig.getUser();
        String password = DBConfig.getPassword();

        try {
            return DriverManager.getConnection(url, user, password);
        } catch (SQLException firstError) {
            String retryUrl = buildRailwayProxyUrl(url);
            if (retryUrl == null || retryUrl.equals(url)) {
                throw firstError;
            }

            try {
                return DriverManager.getConnection(retryUrl, user, password);
            } catch (SQLException secondError) {
                secondError.addSuppressed(firstError);
                throw secondError;
            }
        }
    }

    private static String buildRailwayProxyUrl(String url) {
        if (url == null || url.isBlank() || !url.startsWith("jdbc:mysql://")) {
            return null;
        }

        Matcher matcher = MYSQL_HOST_PATTERN.matcher(url);
        if (!matcher.matches()) {
            return null;
        }

        String currentHost = matcher.group(2);
        if (RAILWAY_PROXY_HOST.equalsIgnoreCase(currentHost)) {
            return null;
        }

        return matcher.group(1) + RAILWAY_PROXY_HOST + matcher.group(3);
    }
}