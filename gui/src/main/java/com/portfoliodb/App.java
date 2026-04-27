package com.portfoliodb;

import com.portfoliodb.db.DatabaseManager;
import com.portfoliodb.ui.MainFrame;
import com.portfoliodb.ui.UIStyle;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class App {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
                // Fall back to default look and feel if system LAF fails.
            }

            UIStyle.setupGlobalTheme();

            Runtime.getRuntime().addShutdownHook(new Thread(DatabaseManager::closeConnection));

            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}
