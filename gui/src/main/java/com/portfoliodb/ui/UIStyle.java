package com.portfoliodb.ui;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public final class UIStyle {
    public static final Color BG_APP = new Color(246, 248, 252);
    public static final Color BG_PANEL = new Color(238, 243, 250);
    public static final Color BG_NAV = new Color(9, 16, 35);
    public static final Color BG_CARD = Color.WHITE;
    public static final Color ACCENT = new Color(12, 113, 195);
    public static final Color ACCENT_DARK = new Color(8, 85, 153);
    public static final Color NAV_ACTIVE = new Color(30, 64, 175);
    public static final Color NAV_HOVER = new Color(20, 36, 70);
    public static final Color TEXT_LIGHT = Color.WHITE;
    public static final Color TEXT_DARK = new Color(15, 23, 42);
    public static final Color TEXT_MUTED = new Color(71, 85, 105);
    public static final Color SUCCESS = new Color(16, 185, 129);
    public static final Color DANGER = new Color(220, 38, 38);

    public static final Font FONT_TITLE = new Font("Segoe UI Semibold", Font.BOLD, 38);
    public static final Font FONT_SECTION = new Font("Segoe UI Semibold", Font.BOLD, 20);
    public static final Font FONT_TEXT = new Font("Segoe UI", Font.PLAIN, 15);
    public static final Font FONT_METRIC = new Font("Segoe UI Semibold", Font.BOLD, 40);

    private UIStyle() {
    }

    public static void setupGlobalTheme() {
        UIManager.put("Label.font", FONT_TEXT);
        UIManager.put("Button.font", FONT_TEXT);
        UIManager.put("TextField.font", FONT_TEXT);
        UIManager.put("ComboBox.font", FONT_TEXT);
        UIManager.put("Table.font", FONT_TEXT);
        UIManager.put("TableHeader.font", FONT_TEXT.deriveFont(Font.BOLD, 14f));
        UIManager.put("Table.rowHeight", 30);
        UIManager.put("OptionPane.messageFont", FONT_TEXT);
        UIManager.put("OptionPane.buttonFont", FONT_TEXT);
    }

    public static JButton navButton(String text) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setFont(FONT_TEXT.deriveFont(Font.BOLD, 14f));
        button.setBackground(BG_NAV);
        button.setForeground(TEXT_LIGHT);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(11, 14, 11, 14));
        button.setHorizontalAlignment(JButton.LEFT);
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!NAV_ACTIVE.equals(button.getBackground())) {
                    button.setBackground(NAV_HOVER);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (!NAV_ACTIVE.equals(button.getBackground())) {
                    button.setBackground(BG_NAV);
                }
            }
        });
        return button;
    }

    public static JButton primaryButton(String text) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setFont(FONT_TEXT);
        button.setBackground(ACCENT);
        button.setForeground(TEXT_LIGHT);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(9, 14, 9, 14));
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(ACCENT_DARK);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(ACCENT);
            }
        });
        return button;
    }

    public static JButton secondaryButton(String text) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setFont(FONT_TEXT);
        button.setForeground(TEXT_DARK);
        button.setBackground(new Color(226, 232, 240));
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(new EmptyBorder(9, 14, 9, 14));
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(new Color(203, 213, 225));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(new Color(226, 232, 240));
            }
        });
        return button;
    }

    public static JPanel cardPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(BG_CARD);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(217, 226, 237)),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        return panel;
    }

    public static JLabel heading(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FONT_SECTION);
        label.setForeground(TEXT_DARK);
        return label;
    }

    public static void pad(JComponent component, int top, int left, int bottom, int right) {
        component.setBorder(BorderFactory.createEmptyBorder(top, left, bottom, right));
    }
}
