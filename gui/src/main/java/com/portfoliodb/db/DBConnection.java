package com.portfoliodb.db;

import java.sql.Connection;
import java.sql.SQLException;

public final class DBConnection {

    private DBConnection() {
    }

    public static Connection getConnection() throws SQLException {
        return DatabaseManager.getConnection();
    }
}
