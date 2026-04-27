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
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
import javax.swing.AbstractButton;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.math.BigDecimal;

public class TransactionsPanel extends JPanel implements DataRefreshable {
    private final PortfolioDAO dao;
    private final JTable table = new JTable();

    private final JComboBox<Object> investorFilter = new JComboBox<>();
    private final JComboBox<Object> assetFilter = new JComboBox<>();

    private final JTextField txIdField = new JTextField(8);
    private final JTextField newQtyField = new JTextField(8);

    private static final class RefreshResult {
        private final java.util.List<LookupItem> investors;
        private final java.util.List<LookupItem> assets;
        private final DefaultTableModel tableModel;

        private RefreshResult(java.util.List<LookupItem> investors, java.util.List<LookupItem> assets, DefaultTableModel tableModel) {
            this.investors = investors;
            this.assets = assets;
            this.tableModel = tableModel;
        }
    }

    public TransactionsPanel(PortfolioDAO dao) {
        this.dao = dao;

        setLayout(new BorderLayout(16, 16));
        setBackground(UIStyle.BG_APP);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Transactions (View / Filter / Update / Delete)");
        title.setFont(UIStyle.FONT_TITLE);
        title.setForeground(UIStyle.TEXT_DARK);

        JPanel controlsCard = UIStyle.cardPanel();
        controlsCard.setLayout(new BorderLayout(8, 8));

        JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterRow.setOpaque(false);
        filterRow.add(new JLabel("Investor Filter"));
        filterRow.add(investorFilter);
        filterRow.add(new JLabel("Asset Filter"));
        filterRow.add(assetFilter);

        JButton filterBtn = UIStyle.primaryButton("Apply Filter");
        filterBtn.addActionListener(e -> applyFilter(filterBtn));

        JButton clearBtn = UIStyle.secondaryButton("Show All");
        clearBtn.addActionListener(e -> loadAllTransactions(clearBtn));

        JButton refreshListsBtn = UIStyle.secondaryButton("Reload Filters");
        refreshListsBtn.addActionListener(e -> refreshData(refreshListsBtn));

        filterRow.add(filterBtn);
        filterRow.add(clearBtn);
        filterRow.add(refreshListsBtn);

        JPanel modifyRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        modifyRow.setOpaque(false);
        modifyRow.add(new JLabel("Transaction ID"));
        modifyRow.add(txIdField);
        modifyRow.add(new JLabel("New Quantity"));
        modifyRow.add(newQtyField);

        JButton updateBtn = UIStyle.primaryButton("Update Quantity");
        updateBtn.addActionListener(e -> updateTransaction(updateBtn));

        JButton deleteBtn = UIStyle.secondaryButton("Delete Transaction");
        deleteBtn.addActionListener(e -> deleteTransaction(deleteBtn));

        modifyRow.add(updateBtn);
        modifyRow.add(deleteBtn);

        controlsCard.add(filterRow, BorderLayout.NORTH);
        controlsCard.add(modifyRow, BorderLayout.SOUTH);

        JPanel top = new JPanel(new BorderLayout(0, 10));
        top.setOpaque(false);
        top.add(title, BorderLayout.NORTH);
        top.add(controlsCard, BorderLayout.CENTER);

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    @Override
    public void refreshData() {
        refreshData(null);
    }

    @Override
    public void refreshData(AbstractButton sourceButton) {
        DatabaseTaskRunner.runDatabaseTask(
                sourceButton,
                () -> new RefreshResult(dao.getInvestors(), dao.getAssets(), dao.getAllTransactionsTable()),
                result -> {
                    investorFilter.removeAllItems();
                    assetFilter.removeAllItems();

                    investorFilter.addItem("All Investors");
                    for (LookupItem investor : result.investors) {
                        investorFilter.addItem(investor);
                    }

                    assetFilter.addItem("All Assets");
                    for (LookupItem asset : result.assets) {
                        assetFilter.addItem(asset);
                    }

                    table.setModel(result.tableModel);
                },
                ex -> JOptionPane.showMessageDialog(this, "Could not load transactions view: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE)
        );
    }

    private void loadAllTransactions(AbstractButton sourceButton) {
        DatabaseTaskRunner.runDatabaseTask(
                sourceButton,
                dao::getAllTransactionsTable,
                table::setModel,
                ex -> JOptionPane.showMessageDialog(this, "Could not load transactions: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE)
        );
    }

    private void applyFilter(AbstractButton sourceButton) {
        Object investorObj = investorFilter.getSelectedItem();
        Object assetObj = assetFilter.getSelectedItem();

        DatabaseTaskRunner.runDatabaseTask(
                sourceButton,
                () -> {
                    if (investorObj instanceof LookupItem investor) {
                        return dao.getTransactionsByInvestor(investor.getId());
                    }
                    if (assetObj instanceof LookupItem asset) {
                        return dao.getTransactionsByAsset(asset.getId());
                    }
                    return dao.getAllTransactionsTable();
                },
                table::setModel,
                ex -> JOptionPane.showMessageDialog(this, "Could not filter transactions: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE)
        );
    }

    private void updateTransaction(AbstractButton sourceButton) {
        String txText = txIdField.getText().trim();
        String qtyText = newQtyField.getText().trim();

        if (txText.isEmpty() || qtyText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter transaction id and new quantity.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        final int txId;
        final BigDecimal qty;
        try {
            txId = Integer.parseInt(txText);
            qty = new BigDecimal(qtyText);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Enter valid numeric values.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (qty.compareTo(BigDecimal.ZERO) <= 0) {
            JOptionPane.showMessageDialog(this, "Quantity must be positive.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        DatabaseTaskRunner.runDatabaseTask(
                sourceButton,
                () -> {
                    dao.updateTransactionQuantity(txId, qty);
                    return dao.getAllTransactionsTable();
                },
                model -> {
                    table.setModel(model);
                    JOptionPane.showMessageDialog(this, "Transaction updated.");
                },
                ex -> JOptionPane.showMessageDialog(this, "Could not update transaction: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE)
        );
    }

    private void deleteTransaction(AbstractButton sourceButton) {
        String txText = txIdField.getText().trim();
        if (txText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter transaction id to delete.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        final int txId;
        try {
            txId = Integer.parseInt(txText);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Transaction id must be numeric.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        DatabaseTaskRunner.runDatabaseTask(
                sourceButton,
                () -> {
                    dao.deleteTransaction(txId);
                    return dao.getAllTransactionsTable();
                },
                model -> {
                    table.setModel(model);
                    JOptionPane.showMessageDialog(this, "Transaction deleted.");
                },
                ex -> JOptionPane.showMessageDialog(this, "Could not delete transaction: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE)
        );
    }
}
