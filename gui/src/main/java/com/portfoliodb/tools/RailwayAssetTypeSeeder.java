package com.portfoliodb.tools;

import com.portfoliodb.db.DatabaseManager;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public final class RailwayAssetTypeSeeder {

    private RailwayAssetTypeSeeder() {
    }

    public static void main(String[] args) throws Exception {
        try (Connection connection = DatabaseManager.getConnection()) {
            connection.setAutoCommit(false);
            try {
                dropExistingAssetTypeChecks(connection);
                addExtendedAssetTypeCheck(connection);
                upsertExtendedAssetCatalog(connection);
                printSummary(connection);
                connection.commit();
                System.out.println("Railway asset type migration completed successfully.");
            } catch (Exception ex) {
                connection.rollback();
                throw ex;
            }
        } finally {
            DatabaseManager.closeConnection();
        }
    }

    private static void dropExistingAssetTypeChecks(Connection connection) throws SQLException {
        List<String> constraints = new ArrayList<>();
        String sql = """
                SELECT tc.CONSTRAINT_NAME
                FROM information_schema.TABLE_CONSTRAINTS tc
                WHERE tc.TABLE_SCHEMA = DATABASE()
                  AND tc.TABLE_NAME = 'assets'
                  AND tc.CONSTRAINT_TYPE = 'CHECK'
                """;

        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                constraints.add(rs.getString(1));
            }
        }

        for (String constraint : constraints) {
            try (Statement st = connection.createStatement()) {
                st.execute("ALTER TABLE assets DROP CHECK " + constraint);
                System.out.println("Dropped check: " + constraint);
            }
        }
    }

    private static void addExtendedAssetTypeCheck(Connection connection) throws SQLException {
        String sql = """
                ALTER TABLE assets
                ADD CONSTRAINT chk_assets_type_extended
                CHECK (type IN ('Stock','Crypto','ETF','Bond','Mutual Fund'))
                """;

        try (Statement st = connection.createStatement()) {
            st.execute(sql);
            System.out.println("Added check: chk_assets_type_extended");
        }
    }

    private static void upsertExtendedAssetCatalog(Connection connection) throws SQLException {
        String insertAssetsSql = """
                INSERT INTO assets (name, type)
                SELECT src.name, src.type
                FROM (
                    SELECT 'Vanguard S&P 500 ETF (VOO)' AS name, 'ETF' AS type, 465.20 AS current_price UNION ALL
                    SELECT 'SPDR S&P 500 ETF Trust (SPY)', 'ETF', 521.35 UNION ALL
                    SELECT 'Invesco QQQ Trust (QQQ)', 'ETF', 446.90 UNION ALL
                    SELECT 'Vanguard Total Stock Market ETF (VTI)', 'ETF', 262.40 UNION ALL
                    SELECT 'iShares Core S&P 500 ETF (IVV)', 'ETF', 528.10 UNION ALL
                    SELECT 'Schwab U.S. Dividend Equity ETF (SCHD)', 'ETF', 79.35 UNION ALL
                    SELECT 'ARK Innovation ETF (ARKK)', 'ETF', 49.20 UNION ALL
                    SELECT 'Vanguard FTSE Emerging Markets ETF (VWO)', 'ETF', 43.80 UNION ALL
                    SELECT 'iShares Russell 2000 ETF (IWM)', 'ETF', 201.60 UNION ALL
                    SELECT 'Energy Select Sector SPDR Fund (XLE)', 'ETF', 95.15 UNION ALL

                    SELECT 'U.S. Treasury Bond 10Y', 'Bond', 98.40 UNION ALL
                    SELECT 'U.S. Treasury Bond 20Y', 'Bond', 92.75 UNION ALL
                    SELECT 'U.S. Treasury Bond 30Y', 'Bond', 89.20 UNION ALL
                    SELECT 'Corporate Bond AAA Fund', 'Bond', 101.10 UNION ALL
                    SELECT 'Corporate Bond BBB Fund', 'Bond', 96.55 UNION ALL
                    SELECT 'Municipal Bond Income Fund', 'Bond', 103.25 UNION ALL
                    SELECT 'Short-Term Government Bond Fund', 'Bond', 99.05 UNION ALL
                    SELECT 'Inflation-Protected Bond Fund', 'Bond', 104.45 UNION ALL

                    SELECT 'Vanguard 500 Index Fund Admiral (VFIAX)', 'Mutual Fund', 489.90 UNION ALL
                    SELECT 'Fidelity 500 Index Fund (FXAIX)', 'Mutual Fund', 191.35 UNION ALL
                    SELECT 'Vanguard Total Stock Market Index Fund (VTSAX)', 'Mutual Fund', 129.60 UNION ALL
                    SELECT 'Dodge & Cox Stock Fund (DODGX)', 'Mutual Fund', 254.20 UNION ALL
                    SELECT 'American Funds Growth Fund of America (AGTHX)', 'Mutual Fund', 72.50 UNION ALL
                    SELECT 'T. Rowe Price Blue Chip Growth Fund (TRBCX)', 'Mutual Fund', 167.85 UNION ALL
                    SELECT 'PIMCO Income Fund (PONAX)', 'Mutual Fund', 11.35
                ) src
                LEFT JOIN assets a ON LOWER(a.name) = LOWER(src.name)
                WHERE a.asset_id IS NULL
                """;

        String insertPricesSql = """
                INSERT INTO prices (asset_id, current_price)
                SELECT a.asset_id, src.current_price
                FROM (
                    SELECT 'Vanguard S&P 500 ETF (VOO)' AS name, 465.20 AS current_price UNION ALL
                    SELECT 'SPDR S&P 500 ETF Trust (SPY)', 521.35 UNION ALL
                    SELECT 'Invesco QQQ Trust (QQQ)', 446.90 UNION ALL
                    SELECT 'Vanguard Total Stock Market ETF (VTI)', 262.40 UNION ALL
                    SELECT 'iShares Core S&P 500 ETF (IVV)', 528.10 UNION ALL
                    SELECT 'Schwab U.S. Dividend Equity ETF (SCHD)', 79.35 UNION ALL
                    SELECT 'ARK Innovation ETF (ARKK)', 49.20 UNION ALL
                    SELECT 'Vanguard FTSE Emerging Markets ETF (VWO)', 43.80 UNION ALL
                    SELECT 'iShares Russell 2000 ETF (IWM)', 201.60 UNION ALL
                    SELECT 'Energy Select Sector SPDR Fund (XLE)', 95.15 UNION ALL

                    SELECT 'U.S. Treasury Bond 10Y', 98.40 UNION ALL
                    SELECT 'U.S. Treasury Bond 20Y', 92.75 UNION ALL
                    SELECT 'U.S. Treasury Bond 30Y', 89.20 UNION ALL
                    SELECT 'Corporate Bond AAA Fund', 101.10 UNION ALL
                    SELECT 'Corporate Bond BBB Fund', 96.55 UNION ALL
                    SELECT 'Municipal Bond Income Fund', 103.25 UNION ALL
                    SELECT 'Short-Term Government Bond Fund', 99.05 UNION ALL
                    SELECT 'Inflation-Protected Bond Fund', 104.45 UNION ALL

                    SELECT 'Vanguard 500 Index Fund Admiral (VFIAX)', 489.90 UNION ALL
                    SELECT 'Fidelity 500 Index Fund (FXAIX)', 191.35 UNION ALL
                    SELECT 'Vanguard Total Stock Market Index Fund (VTSAX)', 129.60 UNION ALL
                    SELECT 'Dodge & Cox Stock Fund (DODGX)', 254.20 UNION ALL
                    SELECT 'American Funds Growth Fund of America (AGTHX)', 72.50 UNION ALL
                    SELECT 'T. Rowe Price Blue Chip Growth Fund (TRBCX)', 167.85 UNION ALL
                    SELECT 'PIMCO Income Fund (PONAX)', 11.35
                ) src
                JOIN assets a ON LOWER(a.name) = LOWER(src.name)
                LEFT JOIN prices p ON p.asset_id = a.asset_id
                WHERE p.asset_id IS NULL
                """;

        try (Statement st = connection.createStatement()) {
            int insertedAssets = st.executeUpdate(insertAssetsSql);
            int insertedPrices = st.executeUpdate(insertPricesSql);
            System.out.println("Inserted assets: " + insertedAssets);
            System.out.println("Inserted prices: " + insertedPrices);
        }
    }

    private static void printSummary(Connection connection) throws SQLException {
        String sql = """
                SELECT type, COUNT(*)
                FROM assets
                WHERE type IN ('ETF', 'Bond', 'Mutual Fund')
                GROUP BY type
                ORDER BY type
                """;

        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                String type = rs.getString(1);
                int count = rs.getInt(2);
                System.out.println(type + " count: " + count);
            }
        }
    }
}
