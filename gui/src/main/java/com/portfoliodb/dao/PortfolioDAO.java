package com.portfoliodb.dao;

import com.portfoliodb.db.DatabaseManager;

import javax.swing.table.DefaultTableModel;
import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PortfolioDAO {

    public void addInvestor(String name, String email) throws SQLException {
        String sql = "INSERT INTO investors (name, email) VALUES (?, ?)";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, email);
            ps.executeUpdate();
        }
    }

    public void addAsset(String name, String type) throws SQLException {
        String sql = "INSERT INTO assets (name, type) VALUES (?, ?)";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, type);
            ps.executeUpdate();
        }
    }

    public void addTransaction(int investorId, int assetId, String transactionType, BigDecimal quantity, BigDecimal price, LocalDate date) throws SQLException {
        String normalizedType = transactionType == null ? "BUY" : transactionType.trim().toUpperCase();
        if (!"BUY".equals(normalizedType) && !"SELL".equals(normalizedType)) {
            throw new SQLException("Transaction type must be BUY or SELL.");
        }

        if ("SELL".equals(normalizedType)) {
            BigDecimal ownedQty = getNetQuantityByInvestorAsset(investorId, assetId);
            if (ownedQty.compareTo(quantity) < 0) {
                throw new SQLException("Cannot sell more units than currently owned. Owned: " + ownedQty);
            }
        }

        String sql = "INSERT INTO transactions (investor_id, asset_id, transaction_type, quantity, price, transaction_date) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, investorId);
            ps.setInt(2, assetId);
            ps.setString(3, normalizedType);
            ps.setBigDecimal(4, quantity);
            ps.setBigDecimal(5, price);
            ps.setDate(6, Date.valueOf(date));
            ps.executeUpdate();
        }
    }

    public List<LookupItem> getInvestors() throws SQLException {
        String sql = "SELECT investor_id, name FROM investors ORDER BY name";
        List<LookupItem> result = new ArrayList<>();

        try (Statement st = DatabaseManager.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                result.add(new LookupItem(rs.getInt("investor_id"), rs.getString("name")));
            }
        }

        return result;
    }

    public List<LookupItem> getAssets() throws SQLException {
        String sql = "SELECT asset_id, name FROM assets ORDER BY name";
        List<LookupItem> result = new ArrayList<>();

        try (Statement st = DatabaseManager.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                result.add(new LookupItem(rs.getInt("asset_id"), rs.getString("name")));
            }
        }

        return result;
    }

    public List<LookupItem> getAssetsOwnedByInvestor(int investorId) throws SQLException {
        String sql = """
                SELECT a.asset_id, a.name
                FROM assets a
                JOIN transactions t ON t.asset_id = a.asset_id
                WHERE t.investor_id = ?
                GROUP BY a.asset_id, a.name
                HAVING SUM(CASE WHEN t.transaction_type = 'SELL' THEN -t.quantity ELSE t.quantity END) > 0
                ORDER BY a.name
                """;
        List<LookupItem> result = new ArrayList<>();

        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, investorId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(new LookupItem(rs.getInt("asset_id"), rs.getString("name")));
                }
            }
        }

        return result;
    }

    public List<AssetLookupItem> getAssetsWithTypes() throws SQLException {
        String sql = "SELECT asset_id, name, type FROM assets ORDER BY type, name";
        List<AssetLookupItem> result = new ArrayList<>();

        try (Statement st = DatabaseManager.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                result.add(new AssetLookupItem(
                        rs.getInt("asset_id"),
                        rs.getString("name"),
                        rs.getString("type")
                ));
            }
        }

        return result;
    }

    public DashboardStats getDashboardStats() throws SQLException {
        int totalInvestors = getCount("SELECT COUNT(*) FROM investors");
        int totalTransactions = getCount("SELECT COUNT(*) FROM transactions");

        BigDecimal assetsUnderManagement = getDecimal("""
                SELECT COALESCE(SUM(x.net_qty * COALESCE(p.current_price, 0)), 0)
                FROM (
                    SELECT investor_id,
                           asset_id,
                           SUM(CASE WHEN transaction_type = 'SELL' THEN -quantity ELSE quantity END) AS net_qty
                    FROM transactions
                    GROUP BY investor_id, asset_id
                    HAVING net_qty > 0
                ) x
                LEFT JOIN prices p ON p.asset_id = x.asset_id
                """);

        BigDecimal averageDailyTradingVolume = getDecimal("""
                SELECT COALESCE(AVG(day_value), 0)
                FROM (
                    SELECT transaction_date,
                           SUM(quantity * price) AS day_value
                    FROM transactions
                    GROUP BY transaction_date
                ) d
                """);

        BigDecimal overallProfitLoss = getDecimal("""
                SELECT COALESCE(SUM(s.sell_proceeds - (s.sold_qty * COALESCE(b.avg_buy_price, 0))), 0)
                FROM (
                    SELECT investor_id,
                           asset_id,
                           SUM(quantity) AS sold_qty,
                           SUM(quantity * price) AS sell_proceeds
                    FROM transactions
                    WHERE transaction_type = 'SELL'
                    GROUP BY investor_id, asset_id
                ) s
                LEFT JOIN (
                    SELECT investor_id,
                           asset_id,
                           SUM(quantity * price) / NULLIF(SUM(quantity), 0) AS avg_buy_price
                    FROM transactions
                    WHERE transaction_type = 'BUY'
                    GROUP BY investor_id, asset_id
                ) b ON b.investor_id = s.investor_id AND b.asset_id = s.asset_id
                """);

        String assetTypes = "Stock, Crypto, ETF, Mutual Fund, Bond";

        return new DashboardStats(
            totalInvestors,
            assetsUnderManagement,
            totalTransactions,
            averageDailyTradingVolume,
            assetTypes,
            overallProfitLoss
        );
    }

    public BigDecimal getInvestorPortfolioCurrentValue(int investorId) throws SQLException {
        String sql = """
                SELECT COALESCE(SUM(x.net_qty * COALESCE(p.current_price, 0)), 0)
                FROM (
                    SELECT t.asset_id,
                           SUM(CASE WHEN t.transaction_type = 'SELL' THEN -t.quantity ELSE t.quantity END) AS net_qty
                    FROM transactions t
                    WHERE t.investor_id = ?
                    GROUP BY t.asset_id
                    HAVING net_qty > 0
                ) x
                LEFT JOIN prices p ON p.asset_id = x.asset_id
                """;
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, investorId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    BigDecimal value = rs.getBigDecimal(1);
                    return value == null ? BigDecimal.ZERO : value;
                }
            }
        }
        return BigDecimal.ZERO;
    }

    public BigDecimal getInvestorRealizedProfitLoss(int investorId) throws SQLException {
        String sql = """
                SELECT COALESCE(SUM(s.sell_proceeds - (s.sold_qty * COALESCE(b.avg_buy_price, 0))), 0)
                FROM (
                    SELECT asset_id,
                           SUM(quantity) AS sold_qty,
                           SUM(quantity * price) AS sell_proceeds
                    FROM transactions
                    WHERE investor_id = ? AND transaction_type = 'SELL'
                    GROUP BY asset_id
                ) s
                LEFT JOIN (
                    SELECT asset_id,
                           SUM(quantity * price) / NULLIF(SUM(quantity), 0) AS avg_buy_price
                    FROM transactions
                    WHERE investor_id = ? AND transaction_type = 'BUY'
                    GROUP BY asset_id
                ) b ON b.asset_id = s.asset_id
                """;
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, investorId);
            ps.setInt(2, investorId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    BigDecimal value = rs.getBigDecimal(1);
                    return value == null ? BigDecimal.ZERO : value;
                }
            }
        }
        return BigDecimal.ZERO;
    }

    public DefaultTableModel getInvestorPortfolioData(int investorId) throws SQLException {
        String sql = """
                SELECT a.name AS asset_name,
                       x.net_qty AS units_owned,
                       COALESCE(p.current_price, 0) AS current_price,
                       (x.net_qty * COALESCE(p.current_price, 0)) AS current_value
                FROM (
                    SELECT t.asset_id,
                           SUM(CASE WHEN t.transaction_type = 'SELL' THEN -t.quantity ELSE t.quantity END) AS net_qty
                    FROM transactions t
                    WHERE t.investor_id = ?
                    GROUP BY t.asset_id
                    HAVING net_qty > 0
                ) x
                JOIN assets a ON a.asset_id = x.asset_id
                LEFT JOIN prices p ON p.asset_id = x.asset_id
                ORDER BY a.name
                """;
        return executePreparedQueryToTable(sql, investorId);
    }

    public DefaultTableModel getPortfolioData(boolean sortByProfitDesc) throws SQLException {
        DefaultTableModel model = new DefaultTableModel();

        try (CallableStatement cs = DatabaseManager.getConnection().prepareCall("{CALL get_portfolio()}");
             ResultSet rs = cs.executeQuery()) {

            ResultSetMetaData meta = rs.getMetaData();
            int colCount = meta.getColumnCount();

            for (int i = 1; i <= colCount; i++) {
                model.addColumn(meta.getColumnLabel(i));
            }

            while (rs.next()) {
                Object[] row = new Object[colCount];
                for (int i = 0; i < colCount; i++) {
                    row[i] = rs.getObject(i + 1);
                }
                model.addRow(row);
            }
        }

        if (sortByProfitDesc) {
            model = sortTableByNumericColumn(model, model.findColumn("profit_loss"));
        }

        return model;
    }

    public DefaultTableModel getAllInvestorsTable() throws SQLException {
        return executeQueryToTable("SELECT investor_id, name, email FROM investors ORDER BY investor_id");
    }

    public DefaultTableModel getAllTransactionsTable() throws SQLException {
        return executeQueryToTable("""
              SELECT t.transaction_id,
                  i.name AS investor_name,
                  a.name AS asset_name,
                  t.transaction_type,
                  t.quantity,
                  t.price,
                  t.transaction_date
              FROM transactions t
              JOIN investors i ON t.investor_id = i.investor_id
              JOIN assets a ON t.asset_id = a.asset_id
              ORDER BY t.transaction_id DESC
                """);
    }

    public DefaultTableModel getTransactionsByInvestor(int investorId) throws SQLException {
        String sql = """
                SELECT t.transaction_id,
                       i.name AS investor_name,
                       a.name AS asset_name,
                  t.transaction_type,
                       t.quantity,
                       t.price,
                       t.transaction_date
                FROM transactions t
                JOIN investors i ON t.investor_id = i.investor_id
                JOIN assets a ON t.asset_id = a.asset_id
                WHERE t.investor_id = ?
                ORDER BY t.transaction_id DESC
                """;
        return executePreparedQueryToTable(sql, investorId);
    }

    public DefaultTableModel getTransactionsByAsset(int assetId) throws SQLException {
        String sql = """
                SELECT t.transaction_id,
                       i.name AS investor_name,
                       a.name AS asset_name,
                  t.transaction_type,
                       t.quantity,
                       t.price,
                       t.transaction_date
                FROM transactions t
                JOIN investors i ON t.investor_id = i.investor_id
                JOIN assets a ON t.asset_id = a.asset_id
                WHERE t.asset_id = ?
                ORDER BY t.transaction_id DESC
                """;
        return executePreparedQueryToTable(sql, assetId);
    }

    public BigDecimal getTotalInvestmentByInvestor(int investorId) throws SQLException {
        String sql = "SELECT COALESCE(total_investment(?), 0)";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, investorId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal(1);
                }
            }
        }
        return BigDecimal.ZERO;
    }

    public BigDecimal getInvestmentByInvestorAndAsset(int investorId, int assetId) throws SQLException {
        String sql = """
                SELECT COALESCE(SUM(quantity * price), 0)
                FROM transactions
                WHERE investor_id = ? AND asset_id = ?
                """;
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, investorId);
            ps.setInt(2, assetId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    BigDecimal value = rs.getBigDecimal(1);
                    return value == null ? BigDecimal.ZERO : value;
                }
            }
        }
        return BigDecimal.ZERO;
    }

    public DefaultTableModel getTransactionsByInvestorAndAsset(int investorId, int assetId) throws SQLException {
        String sql = """
            SELECT t.transaction_id,
                   i.name AS investor_name,
                   a.name AS asset_name,
                   t.transaction_type,
                   t.quantity,
                   t.price,
                   t.transaction_date
            FROM transactions t
            JOIN investors i ON t.investor_id = i.investor_id
            JOIN assets a ON t.asset_id = a.asset_id
            WHERE t.investor_id = ? AND t.asset_id = ?
            ORDER BY t.transaction_id DESC
                """;
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, investorId);
            ps.setInt(2, assetId);
            try (ResultSet rs = ps.executeQuery()) {
                return toTableModel(rs);
            }
        }
    }

    public void deleteTransaction(int transactionId) throws SQLException {
        String sql = "DELETE FROM transactions WHERE transaction_id = ?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, transactionId);
            ps.executeUpdate();
        }
    }

    public void updateTransactionQuantity(int transactionId, BigDecimal quantity) throws SQLException {
        String sql = "UPDATE transactions SET quantity = ? WHERE transaction_id = ?";
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setBigDecimal(1, quantity);
            ps.setInt(2, transactionId);
            ps.executeUpdate();
        }
    }

    private int getCount(String sql) throws SQLException {
        try (Statement st = DatabaseManager.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            rs.next();
            return rs.getInt(1);
        }
    }

    private BigDecimal getDecimal(String sql) throws SQLException {
        try (Statement st = DatabaseManager.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            rs.next();
            BigDecimal value = rs.getBigDecimal(1);
            return value == null ? BigDecimal.ZERO : value;
        }
    }

    private DefaultTableModel executeQueryToTable(String sql) throws SQLException {
        try (Statement st = DatabaseManager.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            return toTableModel(rs);
        }
    }

    private DefaultTableModel executePreparedQueryToTable(String sql, int value) throws SQLException {
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, value);
            try (ResultSet rs = ps.executeQuery()) {
                return toTableModel(rs);
            }
        }
    }

    private DefaultTableModel toTableModel(ResultSet rs) throws SQLException {
        DefaultTableModel model = new DefaultTableModel();
        ResultSetMetaData meta = rs.getMetaData();
        int colCount = meta.getColumnCount();

        for (int i = 1; i <= colCount; i++) {
            model.addColumn(meta.getColumnLabel(i));
        }

        while (rs.next()) {
            Object[] row = new Object[colCount];
            for (int i = 0; i < colCount; i++) {
                row[i] = rs.getObject(i + 1);
            }
            model.addRow(row);
        }

        return model;
    }

    private DefaultTableModel sortTableByNumericColumn(DefaultTableModel model, int colIndex) {
        if (colIndex < 0) {
            return model;
        }

        List<Object[]> rows = new ArrayList<>();
        int rowCount = model.getRowCount();
        int colCount = model.getColumnCount();

        for (int r = 0; r < rowCount; r++) {
            Object[] row = new Object[colCount];
            for (int c = 0; c < colCount; c++) {
                row[c] = model.getValueAt(r, c);
            }
            rows.add(row);
        }

        rows.sort((a, b) -> {
            BigDecimal left = parseDecimal(a[colIndex]);
            BigDecimal right = parseDecimal(b[colIndex]);
            return right.compareTo(left);
        });

        DefaultTableModel sorted = new DefaultTableModel();
        for (int c = 0; c < colCount; c++) {
            sorted.addColumn(model.getColumnName(c));
        }
        for (Object[] row : rows) {
            sorted.addRow(row);
        }
        return sorted;
    }

    private BigDecimal parseDecimal(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        if (value instanceof BigDecimal d) {
            return d;
        }
        try {
            return new BigDecimal(value.toString());
        } catch (NumberFormatException ex) {
            return BigDecimal.ZERO;
        }
    }

    private BigDecimal getNetQuantityByInvestorAsset(int investorId, int assetId) throws SQLException {
        String sql = """
                SELECT COALESCE(SUM(CASE WHEN transaction_type = 'SELL' THEN -quantity ELSE quantity END), 0)
                FROM transactions
                WHERE investor_id = ? AND asset_id = ?
                """;
        try (PreparedStatement ps = DatabaseManager.getConnection().prepareStatement(sql)) {
            ps.setInt(1, investorId);
            ps.setInt(2, assetId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    BigDecimal qty = rs.getBigDecimal(1);
                    return qty == null ? BigDecimal.ZERO : qty;
                }
            }
        }
        return BigDecimal.ZERO;
    }
}
