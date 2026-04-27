package com.portfoliodb.ui.panels;

import com.portfoliodb.dao.PortfolioDAO;
import com.portfoliodb.ui.DataRefreshable;
import com.portfoliodb.ui.DatabaseTaskRunner;
import com.portfoliodb.ui.UIStyle;

import javax.swing.BorderFactory;
import javax.swing.JButton;
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
import java.awt.GridLayout;

public class InvestorPanel extends JPanel implements DataRefreshable {
    private final PortfolioDAO dao;
    private final JTextField nameField = new JTextField();
    private final JTextField emailField = new JTextField();
    private final JTable table = new JTable();

    public InvestorPanel(PortfolioDAO dao) {
        this.dao = dao;

        setLayout(new BorderLayout(16, 16));
        setBackground(UIStyle.BG_APP);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Add Investor + View Investors");
        title.setFont(UIStyle.FONT_TITLE);
        title.setForeground(UIStyle.TEXT_DARK);

        JPanel formCard = UIStyle.cardPanel();
        formCard.setLayout(new BorderLayout(12, 12));

        JPanel fields = new JPanel(new GridLayout(2, 2, 12, 12));
        fields.setOpaque(false);
        fields.add(new JLabel("Name"));
        fields.add(nameField);
        fields.add(new JLabel("Email"));
        fields.add(emailField);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.setOpaque(false);

        JButton addBtn = UIStyle.primaryButton("Add Investor");
        addBtn.addActionListener(e -> addInvestor(addBtn));

        JButton refreshBtn = UIStyle.secondaryButton("Refresh Table");
        refreshBtn.addActionListener(e -> refreshData(refreshBtn));

        actions.add(refreshBtn);
        actions.add(addBtn);

        formCard.add(fields, BorderLayout.CENTER);
        formCard.add(actions, BorderLayout.SOUTH);

        JPanel top = new JPanel(new BorderLayout(0, 12));
        top.setOpaque(false);
        top.add(title, BorderLayout.NORTH);
        top.add(formCard, BorderLayout.CENTER);

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    private void addInvestor(AbstractButton sourceButton) {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();

        if (name.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name and email are required.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!email.contains("@") || !email.contains(".")) {
            JOptionPane.showMessageDialog(this, "Enter a valid email.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        DatabaseTaskRunner.runDatabaseTask(
                sourceButton,
                () -> {
                    dao.addInvestor(name, email);
                    return dao.getAllInvestorsTable();
                },
                tableModel -> {
                    nameField.setText("");
                    emailField.setText("");
                    table.setModel(tableModel);
                    JOptionPane.showMessageDialog(this, "Investor added successfully.");
                },
                ex -> JOptionPane.showMessageDialog(this, "Could not add investor: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE)
        );
    }

    @Override
    public void refreshData() {
        refreshData(null);
    }

    @Override
    public void refreshData(AbstractButton sourceButton) {
        DatabaseTaskRunner.runDatabaseTask(
                sourceButton,
                dao::getAllInvestorsTable,
                table::setModel,
                ex -> JOptionPane.showMessageDialog(this, "Could not load investors: " + ex.getMessage(), "Database Error", JOptionPane.ERROR_MESSAGE)
        );
    }
}
