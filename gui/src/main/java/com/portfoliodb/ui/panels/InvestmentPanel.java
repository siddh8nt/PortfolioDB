package com.portfoliodb.ui.panels;

import com.portfoliodb.dao.LookupItem;
import com.portfoliodb.dao.PortfolioDAO;
import com.portfoliodb.ui.DatabaseTaskRunner;
import com.portfoliodb.ui.DataRefreshable;
import com.portfoliodb.ui.UIStyle;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.AbstractButton;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.DecimalFormat;
import java.util.Locale;

public class InvestmentPanel extends JPanel implements DataRefreshable {
    private final PortfolioDAO dao;
    private final JComboBox<LookupItem> investorCombo = new JComboBox<>();
    private final JComboBox<LookupItem> assetCombo = new JComboBox<>();
    private final JLabel totalValueLabel = new JLabel("-", JLabel.CENTER);
    private final JLabel selectedAssetValueLabel = new JLabel("-", JLabel.CENTER);
    private final JLabel selectedAssetPercentLabel = new JLabel("-", JLabel.CENTER);
    private final JTable transactionTable = new JTable();
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
    private final DecimalFormat percentFormat = new DecimalFormat("0.00'%'");
    private boolean suppressInvestorSelectionEvents;

    private static final class InvestmentDetailsResult {
        private final BigDecimal total;
        private final BigDecimal selectedAssetInvestment;
        private final BigDecimal percent;
        private final DefaultTableModel tableModel;

        private InvestmentDetailsResult(
                BigDecimal total,
                BigDecimal selectedAssetInvestment,
                BigDecimal percent,
                DefaultTableModel tableModel
        ) {
            this.total = total;
            this.selectedAssetInvestment = selectedAssetInvestment;
            this.percent = percent;
            this.tableModel = tableModel;
        }
    }

    private static final class RefreshResult {
        private final java.util.List<LookupItem> investors;
        private final DefaultTableModel tableModel;

        private RefreshResult(java.util.List<LookupItem> investors, DefaultTableModel tableModel) {
            this.investors = investors;
            this.tableModel = tableModel;
        }
    }

    public InvestmentPanel(PortfolioDAO dao) {
        this.dao = dao;

        setLayout(new BorderLayout(16, 16));
        setBackground(UIStyle.BG_APP);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Investment by Investor + Selected Asset Analysis");
        title.setFont(UIStyle.FONT_TITLE);
        title.setForeground(UIStyle.TEXT_DARK);

        JPanel card = UIStyle.cardPanel();
        card.setLayout(new GridLayout(5, 1, 10, 10));

        JPanel selector = new JPanel(new FlowLayout(FlowLayout.LEFT));
        selector.setOpaque(false);
        selector.add(new JLabel("Investor:"));
        selector.add(investorCombo);
        selector.add(new JLabel("Asset:"));
        selector.add(assetCombo);

        investorCombo.addActionListener(e -> {
            if (!suppressInvestorSelectionEvents) {
                refreshOwnedAssetsForSelectedInvestor(null);
            }
        });

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.setOpaque(false);

        JButton refreshComboBtn = UIStyle.secondaryButton("Reload Investors");
        refreshComboBtn.addActionListener(e -> refreshData(refreshComboBtn));

        JButton computeBtn = UIStyle.primaryButton("Show Investment Details");
        computeBtn.addActionListener(e -> showInvestmentDetails(computeBtn));

        actions.add(refreshComboBtn);
        actions.add(computeBtn);

        totalValueLabel.setFont(UIStyle.FONT_METRIC);
        selectedAssetValueLabel.setFont(UIStyle.FONT_METRIC.deriveFont(22f));
        selectedAssetPercentLabel.setFont(UIStyle.FONT_METRIC.deriveFont(22f));

        JPanel metrics = new JPanel(new GridLayout(2, 1, 6, 6));
        metrics.setOpaque(false);
        metrics.add(new JLabel("Selected Asset Investment", JLabel.CENTER));
        metrics.add(selectedAssetValueLabel);

        JPanel percent = new JPanel(new GridLayout(2, 1, 6, 6));
        percent.setOpaque(false);
        percent.add(new JLabel("Selected Asset % of Investor Portfolio", JLabel.CENTER));
        percent.add(selectedAssetPercentLabel);

        card.add(selector);
        card.add(actions);
        card.add(totalValueLabel);
        card.add(metrics);
        card.add(percent);

        JPanel tableCard = UIStyle.cardPanel();
        tableCard.setLayout(new BorderLayout(8, 8));
        tableCard.add(new JLabel("Transactions for Selected Investor + Asset"), BorderLayout.NORTH);
        tableCard.add(new JScrollPane(transactionTable), BorderLayout.CENTER);

        JPanel top = new JPanel(new BorderLayout(0, 10));
        top.setOpaque(false);
        top.add(title, BorderLayout.NORTH);
        top.add(card, BorderLayout.CENTER);

        add(top, BorderLayout.NORTH);
        add(tableCard, BorderLayout.CENTER);
    }

    @Override
    public void refreshData() {
        refreshData(null);
    }

    @Override
    public void refreshData(AbstractButton sourceButton) {
        DatabaseTaskRunner.runDatabaseTask(
                sourceButton,
                () -> new RefreshResult(dao.getInvestors(), dao.getAllTransactionsTable()),
                result -> {
                    suppressInvestorSelectionEvents = true;
                    investorCombo.removeAllItems();
                    for (LookupItem investor : result.investors) {
                        investorCombo.addItem(investor);
                    }
                    suppressInvestorSelectionEvents = false;

                    // Avoid passing the same nav/source button into a nested task,
                    // otherwise the nested task may restore "Loading..." as button text.
                    refreshOwnedAssetsForSelectedInvestor(null);

                    totalValueLabel.setText("-");
                    selectedAssetValueLabel.setText("-");
                    selectedAssetPercentLabel.setText("-");
                    transactionTable.setModel(result.tableModel);
                },
                ex -> JOptionPane.showMessageDialog(this, "Could not load investors/assets: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE)
        );
    }

    private void refreshOwnedAssetsForSelectedInvestor(AbstractButton sourceButton) {
        LookupItem investor = (LookupItem) investorCombo.getSelectedItem();
        if (investor == null) {
            assetCombo.removeAllItems();
            return;
        }

        DatabaseTaskRunner.runDatabaseTask(
                sourceButton,
                () -> dao.getAssetsOwnedByInvestor(investor.getId()),
                assets -> {
                    assetCombo.removeAllItems();
                    for (LookupItem asset : assets) {
                        assetCombo.addItem(asset);
                    }
                },
                ex -> JOptionPane.showMessageDialog(this, "Could not load investor assets: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE)
        );
    }

    private void showInvestmentDetails(AbstractButton sourceButton) {
        LookupItem investor = (LookupItem) investorCombo.getSelectedItem();
        LookupItem asset = (LookupItem) assetCombo.getSelectedItem();
        if (investor == null) {
            JOptionPane.showMessageDialog(this, "Please select an investor.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (asset == null) {
            JOptionPane.showMessageDialog(this, "No assets found for this investor. Add a transaction first.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        DatabaseTaskRunner.runDatabaseTask(
                sourceButton,
                () -> {
                    BigDecimal total = dao.getTotalInvestmentByInvestor(investor.getId());
                    BigDecimal selectedAssetInvestment = dao.getInvestmentByInvestorAndAsset(investor.getId(), asset.getId());
                    BigDecimal percent = BigDecimal.ZERO;
                    if (total.compareTo(BigDecimal.ZERO) > 0) {
                        percent = selectedAssetInvestment
                                .multiply(BigDecimal.valueOf(100))
                                .divide(total, 4, java.math.RoundingMode.HALF_UP);
                    }
                    return new InvestmentDetailsResult(
                            total,
                            selectedAssetInvestment,
                            percent,
                            dao.getTransactionsByInvestorAndAsset(investor.getId(), asset.getId())
                    );
                },
                result -> {
                    totalValueLabel.setText(currencyFormat.format(result.total));
                    selectedAssetValueLabel.setText(currencyFormat.format(result.selectedAssetInvestment));
                    selectedAssetPercentLabel.setText(percentFormat.format(result.percent));
                    transactionTable.setModel(result.tableModel);
                },
                ex -> JOptionPane.showMessageDialog(this, "Could not compute investment: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE)
        );
    }
}
