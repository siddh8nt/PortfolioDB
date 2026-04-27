package com.portfoliodb.ui.panels;

import com.portfoliodb.dao.AssetLookupItem;
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
import javax.swing.JTextField;
import javax.swing.AbstractButton;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class TransactionEntryPanel extends JPanel implements DataRefreshable {
    private static final String[] SUPPORTED_ASSET_TYPES = {"Stock", "Crypto", "ETF", "Bond", "Mutual Fund"};

    private final PortfolioDAO dao;

    private final JComboBox<LookupItem> investorCombo = new JComboBox<>();
    private final JComboBox<String> transactionTypeCombo = new JComboBox<>(new String[]{"BUY", "SELL"});
    private final JComboBox<String> assetTypeCombo = new JComboBox<>();
    private final JComboBox<LookupItem> assetCombo = new JComboBox<>();
    private final JTextField quantityField = new JTextField();
    private final JTextField priceField = new JTextField();
    private final JTextField dateField = new JTextField(LocalDate.now().toString());
    private List<AssetLookupItem> allAssets = new ArrayList<>();

    private static final class RefreshResult {
        private final java.util.List<LookupItem> investors;
        private final java.util.List<AssetLookupItem> assets;

        private RefreshResult(java.util.List<LookupItem> investors, java.util.List<AssetLookupItem> assets) {
            this.investors = investors;
            this.assets = assets;
        }
    }

    public TransactionEntryPanel(PortfolioDAO dao) {
        this.dao = dao;

        setLayout(new BorderLayout(16, 16));
        setBackground(UIStyle.BG_APP);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Add Transaction");
        title.setFont(UIStyle.FONT_TITLE);
        title.setForeground(UIStyle.TEXT_DARK);

        JPanel formCard = UIStyle.cardPanel();
        formCard.setLayout(new BorderLayout(12, 12));

        JPanel fields = new JPanel(new GridLayout(7, 2, 12, 12));
        fields.setOpaque(false);

        fields.add(new JLabel("Investor"));
        fields.add(investorCombo);
        fields.add(new JLabel("Transaction Type"));
        fields.add(transactionTypeCombo);
        fields.add(new JLabel("Asset Type"));
        fields.add(assetTypeCombo);
        fields.add(new JLabel("Asset"));
        fields.add(assetCombo);
        fields.add(new JLabel("Quantity"));
        fields.add(quantityField);
        fields.add(new JLabel("Price"));
        fields.add(priceField);
        fields.add(new JLabel("Date (YYYY-MM-DD)"));
        fields.add(dateField);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.setOpaque(false);

        JButton reloadBtn = UIStyle.secondaryButton("Reload Dropdowns");
        reloadBtn.addActionListener(e -> refreshData(reloadBtn));

        JButton addBtn = UIStyle.primaryButton("Add Transaction");
        addBtn.addActionListener(e -> addTransaction(addBtn));

        assetTypeCombo.addActionListener(e -> populateAssetsForSelectedType());

        actions.add(reloadBtn);
        actions.add(addBtn);

        formCard.add(fields, BorderLayout.CENTER);
        formCard.add(actions, BorderLayout.SOUTH);

        add(title, BorderLayout.NORTH);
        add(formCard, BorderLayout.CENTER);
    }

    @Override
    public void refreshData() {
        refreshData(null);
    }

    @Override
    public void refreshData(AbstractButton sourceButton) {
        DatabaseTaskRunner.runDatabaseTask(
                sourceButton,
                () -> new RefreshResult(dao.getInvestors(), dao.getAssetsWithTypes()),
                result -> {
                    investorCombo.removeAllItems();
                    assetTypeCombo.removeAllItems();
                    assetCombo.removeAllItems();
                    allAssets = result.assets;

                    for (LookupItem item : result.investors) {
                        investorCombo.addItem(item);
                    }

                    Set<String> types = new LinkedHashSet<>();
                    for (String supportedType : SUPPORTED_ASSET_TYPES) {
                        types.add(supportedType);
                    }
                    for (AssetLookupItem item : result.assets) {
                        if (item.getType() != null && !item.getType().isBlank()) {
                            types.add(item.getType());
                        }
                    }
                    for (String type : types) {
                        assetTypeCombo.addItem(type);
                    }

                    populateAssetsForSelectedType();
                },
                ex -> JOptionPane.showMessageDialog(this, "Could not load dropdowns: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE)
        );
    }

    private void populateAssetsForSelectedType() {
        assetCombo.removeAllItems();

        String selectedType = (String) assetTypeCombo.getSelectedItem();
        if (selectedType == null || selectedType.isBlank()) {
            return;
        }

        for (AssetLookupItem asset : allAssets) {
            if (selectedType.equals(asset.getType())) {
                assetCombo.addItem(asset);
            }
        }
    }

    private void addTransaction(AbstractButton sourceButton) {
        LookupItem investor = (LookupItem) investorCombo.getSelectedItem();
        LookupItem asset = (LookupItem) assetCombo.getSelectedItem();
        String transactionType = (String) transactionTypeCombo.getSelectedItem();

        if (investor == null || asset == null) {
            JOptionPane.showMessageDialog(this, "Please add investors and assets first.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (transactionType == null || transactionType.isBlank()) {
            JOptionPane.showMessageDialog(this, "Please select transaction type.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        final BigDecimal quantity;
        final BigDecimal price;
        final LocalDate date;
        try {
            quantity = new BigDecimal(quantityField.getText().trim());
            price = new BigDecimal(priceField.getText().trim());
            date = LocalDate.parse(dateField.getText().trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Enter valid numeric quantity and price.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (quantity.compareTo(BigDecimal.ZERO) <= 0 || price.compareTo(BigDecimal.ZERO) <= 0) {
            JOptionPane.showMessageDialog(this, "Quantity and price must be positive.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        DatabaseTaskRunner.runDatabaseTask(
                sourceButton,
                () -> {
                    dao.addTransaction(investor.getId(), asset.getId(), transactionType, quantity, price, date);
                    return null;
                },
                ignored -> {
                    quantityField.setText("");
                    priceField.setText("");
                    dateField.setText(LocalDate.now().toString());
                    JOptionPane.showMessageDialog(this, "Transaction added successfully.");
                },
                ex -> JOptionPane.showMessageDialog(this, "Could not add transaction: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE)
        );
    }
}
