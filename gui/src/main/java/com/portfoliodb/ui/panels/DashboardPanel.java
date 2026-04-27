package com.portfoliodb.ui.panels;

import com.portfoliodb.dao.DashboardStats;
import com.portfoliodb.dao.PortfolioDAO;
import com.portfoliodb.config.DBConfig;
import com.portfoliodb.ui.DatabaseTaskRunner;
import com.portfoliodb.ui.DataRefreshable;
import com.portfoliodb.ui.UIStyle;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.AbstractButton;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class DashboardPanel extends JPanel implements DataRefreshable {
    private final PortfolioDAO dao;
    private final Map<String, JLabel> valueLabels = new LinkedHashMap<>();
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));

    public DashboardPanel(PortfolioDAO dao) {
        this.dao = dao;

        setLayout(new BorderLayout(16, 16));
        setBackground(UIStyle.BG_APP);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JLabel title = new JLabel("Dashboard", SwingConstants.LEFT);
        title.setFont(UIStyle.FONT_TITLE);
        title.setForeground(UIStyle.TEXT_DARK);

        JButton refreshBtn = UIStyle.primaryButton("Refresh");
        refreshBtn.addActionListener(e -> refreshData(refreshBtn));

        top.add(title, BorderLayout.WEST);
        top.add(refreshBtn, BorderLayout.EAST);

        JPanel grid = new JPanel(new GridLayout(3, 2, 14, 14));
        grid.setOpaque(false);

        addCard(grid, "Total Investors");
        addCard(grid, "Assets Under Management");
        addCard(grid, "Total Transactions");
        addCard(grid, "Average Daily Trading Volume");
        addCard(grid, "Asset Types");
        addCard(grid, "Overall Profit/Loss");

        add(top, BorderLayout.NORTH);
        add(grid, BorderLayout.CENTER);
    }

    private void addCard(JPanel grid, String title) {
        JPanel card = UIStyle.cardPanel();
        card.setLayout(new BorderLayout(6, 6));

        JLabel heading = new JLabel(title);
        heading.setFont(UIStyle.FONT_TEXT.deriveFont(18f));
        heading.setForeground(UIStyle.TEXT_MUTED);

        JLabel value = new JLabel("-");
        value.setFont(UIStyle.FONT_METRIC);
        value.setForeground(UIStyle.TEXT_DARK);

        if ("Asset Types".equals(title)) {
            value.setFont(UIStyle.FONT_TEXT.deriveFont(18f));
            value.setVerticalAlignment(SwingConstants.TOP);
        }

        card.add(heading, BorderLayout.NORTH);
        card.add(value, BorderLayout.CENTER);

        valueLabels.put(title, value);
        grid.add(card);
    }

    @Override
    public void refreshData() {
        refreshData(null);
    }

    @Override
    public void refreshData(AbstractButton sourceButton) {
        DatabaseTaskRunner.runDatabaseTask(
                sourceButton,
                dao::getDashboardStats,
                stats -> {
                    valueLabels.get("Total Investors").setText(String.valueOf(stats.totalInvestors()));
                    valueLabels.get("Assets Under Management").setText(formatCurrency(stats.assetsUnderManagement()));
                    valueLabels.get("Total Transactions").setText(String.valueOf(stats.totalTransactions()));
                    valueLabels.get("Average Daily Trading Volume").setText(formatCurrency(stats.averageDailyTradingVolume()));
                    valueLabels.get("Asset Types").setText("<html>Stock, Crypto, ETF,<br>Mutual Fund, Bond</html>");

                    JLabel plLabel = valueLabels.get("Overall Profit/Loss");
                    BigDecimal profit = stats.overallProfitLoss();
                    plLabel.setText(formatCurrency(profit));
                    plLabel.setForeground(profit.compareTo(BigDecimal.ZERO) >= 0 ? UIStyle.SUCCESS : UIStyle.DANGER);
                },
                ex -> {
                    String message = "Failed to load dashboard: " + ex.getMessage()
                            + "\n\nActive DB settings:"
                            + "\nDB_URL=" + DBConfig.getUrl()
                            + "\nDB_USER=" + DBConfig.getUser()
                            + "\n\nTip: set DB_URL/DB_USER/DB_PASSWORD in the same terminal session,"
                            + "\nOR keep db_cloud_cred/cred.txt populated, then rerun mvn exec:java.";
                    JOptionPane.showMessageDialog(this, message, "Database Error", JOptionPane.ERROR_MESSAGE);
                }
        );
    }

    private String formatCurrency(BigDecimal value) {
        return currencyFormat.format(value == null ? BigDecimal.ZERO : value);
    }
}
