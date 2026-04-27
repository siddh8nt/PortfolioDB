package com.portfoliodb.ui.panels;

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

public class AssetPanel extends JPanel implements DataRefreshable {
    private final PortfolioDAO dao;
    private final JTextField nameField = new JTextField();
    private final JComboBox<String> typeCombo = new JComboBox<>(new String[]{"Stock", "Crypto", "ETF", "Bond", "Mutual Fund"});

    public AssetPanel(PortfolioDAO dao) {
        this.dao = dao;

        setLayout(new BorderLayout(16, 16));
        setBackground(UIStyle.BG_APP);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Add Asset");
        title.setFont(UIStyle.FONT_TITLE);
        title.setForeground(UIStyle.TEXT_DARK);

        JPanel formCard = UIStyle.cardPanel();
        formCard.setLayout(new BorderLayout(12, 12));

        JPanel fields = new JPanel(new GridLayout(2, 2, 12, 12));
        fields.setOpaque(false);
        fields.add(new JLabel("Asset Name"));
        fields.add(nameField);
        fields.add(new JLabel("Asset Type"));
        fields.add(typeCombo);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.setOpaque(false);
        JButton addBtn = UIStyle.primaryButton("Add Asset");
        addBtn.addActionListener(e -> addAsset(addBtn));
        actions.add(addBtn);

        formCard.add(fields, BorderLayout.CENTER);
        formCard.add(actions, BorderLayout.SOUTH);

        add(title, BorderLayout.NORTH);
        add(formCard, BorderLayout.CENTER);
    }

    private void addAsset(AbstractButton sourceButton) {
        String name = nameField.getText().trim();
        String type = (String) typeCombo.getSelectedItem();

        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Asset name is required.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        DatabaseTaskRunner.runDatabaseTask(
                sourceButton,
                () -> {
                    dao.addAsset(name, type);
                    return null;
                },
                ignored -> {
                    nameField.setText("");
                    JOptionPane.showMessageDialog(this, "Asset added successfully.");
                },
                ex -> JOptionPane.showMessageDialog(this, "Could not add asset: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE)
        );
    }

    @Override
    public void refreshData() {
        // No dynamic data needed yet for this panel.
    }
}
