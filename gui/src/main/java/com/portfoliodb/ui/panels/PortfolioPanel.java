package com.portfoliodb.ui.panels;

import com.portfoliodb.dao.LookupItem;
import com.portfoliodb.dao.PortfolioDAO;
import com.portfoliodb.ui.DataRefreshable;
import com.portfoliodb.ui.DatabaseTaskRunner;
import com.portfoliodb.ui.UIStyle;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.AbstractButton;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

public class PortfolioPanel extends JPanel implements DataRefreshable {
    private final PortfolioDAO dao;
    private final JTable table = new JTable();
    private final JComboBox<LookupItem> investorCombo = new JComboBox<>();
    private final JLabel totalPortfolioValueLabel = new JLabel("Total Portfolio Value: -");
    private final JLabel realizedProfitLossLabel = new JLabel("Realized Profit/Loss (Sold Units): -");
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
    private boolean suppressInvestorSelectionEvents;

    private static final class RefreshResult {
        private final java.util.List<LookupItem> investors;
        private final Integer selectedInvestorId;
        private final DefaultTableModel portfolioModel;
        private final BigDecimal totalPortfolioValue;
        private final BigDecimal realizedProfitLoss;

        private RefreshResult(
                java.util.List<LookupItem> investors,
                Integer selectedInvestorId,
                DefaultTableModel portfolioModel,
                BigDecimal totalPortfolioValue,
                BigDecimal realizedProfitLoss
        ) {
            this.investors = investors;
            this.selectedInvestorId = selectedInvestorId;
            this.portfolioModel = portfolioModel;
            this.totalPortfolioValue = totalPortfolioValue;
            this.realizedProfitLoss = realizedProfitLoss;
        }
    }

    public PortfolioPanel(PortfolioDAO dao) {
        this.dao = dao;

        setLayout(new BorderLayout(16, 16));
        setBackground(UIStyle.BG_APP);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Portfolio by Investor (Owned Assets + Value + Realized P/L)");
        title.setFont(UIStyle.FONT_TITLE);
        title.setForeground(UIStyle.TEXT_DARK);

        JButton refreshBtn = UIStyle.primaryButton("Load Portfolio");
        refreshBtn.addActionListener(e -> refreshData(refreshBtn));

        investorCombo.addActionListener(e -> {
            if (!suppressInvestorSelectionEvents) {
                refreshData(null);
            }
        });

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(title, BorderLayout.NORTH);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        controls.setOpaque(false);
        controls.add(new JLabel("Investor"));
        controls.add(investorCombo);
        controls.add(refreshBtn);
        top.add(controls, BorderLayout.SOUTH);

        JPanel summary = UIStyle.cardPanel();
        summary.setLayout(new FlowLayout(FlowLayout.LEFT, 24, 10));
        summary.add(totalPortfolioValueLabel);
        summary.add(realizedProfitLossLabel);

        JPanel body = new JPanel(new BorderLayout(10, 10));
        body.setOpaque(false);
        body.add(summary, BorderLayout.NORTH);
        body.add(new JScrollPane(table), BorderLayout.CENTER);

        add(top, BorderLayout.NORTH);
        add(body, BorderLayout.CENTER);
    }

    @Override
    public void refreshData() {
        refreshData(null);
    }

    @Override
    public void refreshData(AbstractButton sourceButton) {
        LookupItem currentSelection = (LookupItem) investorCombo.getSelectedItem();
        Integer requestedInvestorId = currentSelection == null ? null : currentSelection.getId();

        DatabaseTaskRunner.runDatabaseTask(
                sourceButton,
                () -> {
                    java.util.List<LookupItem> investors = dao.getInvestors();
                    Integer selectedInvestorId = requestedInvestorId;

                    if (selectedInvestorId == null || !containsInvestorId(investors, selectedInvestorId)) {
                        selectedInvestorId = investors.isEmpty() ? null : investors.get(0).getId();
                    }

                    if (selectedInvestorId == null) {
                        return new RefreshResult(
                                investors,
                                null,
                                emptyPortfolioModel(),
                                BigDecimal.ZERO,
                                BigDecimal.ZERO
                        );
                    }

                    return new RefreshResult(
                            investors,
                            selectedInvestorId,
                            dao.getInvestorPortfolioData(selectedInvestorId),
                            dao.getInvestorPortfolioCurrentValue(selectedInvestorId),
                            dao.getInvestorRealizedProfitLoss(selectedInvestorId)
                    );
                },
                result -> {
                    suppressInvestorSelectionEvents = true;
                    investorCombo.removeAllItems();
                    for (LookupItem investor : result.investors) {
                        investorCombo.addItem(investor);
                    }
                    selectInvestorById(result.selectedInvestorId);
                    suppressInvestorSelectionEvents = false;

                    table.setModel(result.portfolioModel);
                    totalPortfolioValueLabel.setText("Total Portfolio Value: " + currencyFormat.format(result.totalPortfolioValue));
                    realizedProfitLossLabel.setText("Realized Profit/Loss (Sold Units): " + currencyFormat.format(result.realizedProfitLoss));
                },
                ex -> JOptionPane.showMessageDialog(this, "Could not load portfolio: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE)
        );
    }

    private void selectInvestorById(Integer investorId) {
        if (investorId == null) {
            return;
        }
        for (int i = 0; i < investorCombo.getItemCount(); i++) {
            LookupItem item = investorCombo.getItemAt(i);
            if (item != null && item.getId() == investorId) {
                investorCombo.setSelectedIndex(i);
                return;
            }
        }
    }

    private DefaultTableModel emptyPortfolioModel() {
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("asset_name");
        model.addColumn("units_owned");
        model.addColumn("current_price");
        model.addColumn("current_value");
        return model;
    }

    private boolean containsInvestorId(java.util.List<LookupItem> investors, Integer investorId) {
        if (investorId == null) {
            return false;
        }
        for (LookupItem investor : investors) {
            if (investor != null && investor.getId() == investorId) {
                return true;
            }
        }
        return false;
    }
}
