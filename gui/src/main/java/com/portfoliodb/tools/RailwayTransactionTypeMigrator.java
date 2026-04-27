package com.portfoliodb.tools;

import com.portfoliodb.db.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public final class RailwayTransactionTypeMigrator {

    private RailwayTransactionTypeMigrator() {
    }

    public static void main(String[] args) throws Exception {
        try (Connection connection = DatabaseManager.getConnection()) {
            connection.setAutoCommit(false);
            try {
                ensureTransactionTypeColumn(connection);
                connection.commit();
                System.out.println("Transaction type migration completed successfully.");
            } catch (Exception ex) {
                connection.rollback();
                throw ex;
            }
        } finally {
            DatabaseManager.closeConnection();
        }
    }

    private static void ensureTransactionTypeColumn(Connection connection) throws SQLException {
        if (!columnExists(connection, "transactions", "transaction_type")) {
            try (Statement st = connection.createStatement()) {
                st.execute("""
                        ALTER TABLE transactions
                        ADD COLUMN transaction_type VARCHAR(4) NOT NULL DEFAULT 'BUY'
                        """);
            }
            System.out.println("Added transactions.transaction_type column.");
        } else {
            try (Statement st = connection.createStatement()) {
                st.execute("UPDATE transactions SET transaction_type = 'BUY' WHERE transaction_type IS NULL OR transaction_type = ''");
            }
            System.out.println("transactions.transaction_type already exists.");
        }

        for (String checkName : findTransactionChecks(connection)) {
            try (Statement st = connection.createStatement()) {
                st.execute("ALTER TABLE transactions DROP CHECK " + checkName);
                System.out.println("Dropped check: " + checkName);
            }
        }

        try (Statement st = connection.createStatement()) {
            st.execute("""
                    ALTER TABLE transactions
                    ADD CONSTRAINT chk_transactions_type
                    CHECK (transaction_type IN ('BUY','SELL'))
                    """);
        }

        System.out.println("Applied chk_transactions_type constraint.");
    }

    private static boolean columnExists(Connection connection, String tableName, String columnName) throws SQLException {
        String sql = """
                SELECT COUNT(*)
                FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE()
                  AND TABLE_NAME = ?
                  AND COLUMN_NAME = ?
                """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, tableName);
            ps.setString(2, columnName);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    private static List<String> findTransactionChecks(Connection connection) throws SQLException {
        List<String> checks = new ArrayList<>();
        String sql = """
                SELECT CONSTRAINT_NAME
                FROM information_schema.TABLE_CONSTRAINTS
                WHERE TABLE_SCHEMA = DATABASE()
                  AND TABLE_NAME = 'transactions'
                  AND CONSTRAINT_TYPE = 'CHECK'
                """;

        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                checks.add(rs.getString(1));
            }
        }

        return checks;
    }
}
