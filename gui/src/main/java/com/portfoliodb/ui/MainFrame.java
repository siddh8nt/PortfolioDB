package com.portfoliodb.ui;

import com.portfoliodb.dao.PortfolioDAO;
import com.portfoliodb.ui.panels.AssetPanel;
import com.portfoliodb.ui.panels.DashboardPanel;
import com.portfoliodb.ui.panels.InvestmentPanel;
import com.portfoliodb.ui.panels.InvestorPanel;
import com.portfoliodb.ui.panels.PortfolioPanel;
import com.portfoliodb.ui.panels.TransactionEntryPanel;
import com.portfoliodb.ui.panels.TransactionsPanel;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.util.LinkedHashMap;
import java.util.Map;

public class MainFrame extends JFrame {
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel contentPanel = new JPanel(cardLayout);
    private final Map<String, DataRefreshable> refreshablePanels = new LinkedHashMap<>();
    private final Map<String, JButton> navButtons = new LinkedHashMap<>();

    public MainFrame() {
        setTitle("PortfolioDB - Java Swing + JDBC");
        setSize(1280, 780);
        setMinimumSize(new Dimension(1024, 680));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        PortfolioDAO dao = new PortfolioDAO();

        DashboardPanel dashboardPanel = new DashboardPanel(dao);
        InvestorPanel investorPanel = new InvestorPanel(dao);
        AssetPanel assetPanel = new AssetPanel(dao);
        TransactionEntryPanel transactionEntryPanel = new TransactionEntryPanel(dao);
        PortfolioPanel portfolioPanel = new PortfolioPanel(dao);
        InvestmentPanel investmentPanel = new InvestmentPanel(dao);
        TransactionsPanel transactionsPanel = new TransactionsPanel(dao);

        registerPanel("Dashboard", dashboardPanel);
        registerPanel("Add Investor", investorPanel);
        registerPanel("Add Asset", assetPanel);
        registerPanel("Add Transaction", transactionEntryPanel);
        registerPanel("View Portfolio", portfolioPanel);
        registerPanel("View Investment", investmentPanel);
        registerPanel("Transactions", transactionsPanel);

        JPanel navPanel = buildNavPanel();

        getContentPane().setLayout(new BorderLayout());
        getContentPane().setBackground(UIStyle.BG_APP);
        contentPanel.setBackground(UIStyle.BG_PANEL);
        getContentPane().add(navPanel, BorderLayout.WEST);
        getContentPane().add(contentPanel, BorderLayout.CENTER);

        showPanel("Dashboard");
    }

    private void registerPanel(String key, DataRefreshable panel) {
        refreshablePanels.put(key, panel);
        contentPanel.add((JPanel) panel, key);
    }

    private JPanel buildNavPanel() {
        JPanel nav = new JPanel();
        nav.setBackground(UIStyle.BG_NAV);
        nav.setPreferredSize(new Dimension(240, 0));
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setBorder(BorderFactory.createEmptyBorder(20, 12, 20, 12));

        JLabel title = new JLabel("PortfolioDB");
        title.setForeground(UIStyle.TEXT_LIGHT);
        title.setFont(UIStyle.FONT_SECTION.deriveFont(31f));
        title.setHorizontalAlignment(SwingConstants.LEFT);
        title.setBorder(BorderFactory.createEmptyBorder(4, 10, 22, 10));
        nav.add(title);

        for (String key : refreshablePanels.keySet()) {
            JButton btn = UIStyle.navButton(key);
            btn.addActionListener(e -> showPanel(key, btn));
            navButtons.put(key, btn);
            nav.add(btn);
            nav.add(new JLabel(" "));
        }

        return nav;
    }

    private void showPanel(String key) {
        showPanel(key, null);
    }

    private void showPanel(String key, JButton sourceButton) {
        updateNavSelection(key);
        cardLayout.show(contentPanel, key);

        DataRefreshable panel = refreshablePanels.get(key);
        if (panel != null) {
            panel.refreshData(sourceButton);
        }
    }

    private void updateNavSelection(String key) {
        for (Map.Entry<String, JButton> entry : navButtons.entrySet()) {
            JButton button = entry.getValue();
            if (entry.getKey().equals(key)) {
                button.setBackground(UIStyle.NAV_ACTIVE);
            } else {
                button.setBackground(UIStyle.BG_NAV);
            }
        }
    }
}
