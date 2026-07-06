package view;

import SystemController.Landing;
import SystemController.Session;
import SystemController.SystemController;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class AdminDashboard extends JFrame {
    private CardLayout rootCardLayout;
    private JPanel rootPanel;

    private CardLayout moduleCardLayout;
    private JPanel modulePanel;

    public AdminDashboard() {
        super("Admin Dashboard");

        setSize(1000, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        rootCardLayout = new CardLayout();
        rootPanel = new JPanel(rootCardLayout);

        // first page before opening any module
        rootPanel.add(dashboardMenuPanel(), "DashboardMenu");

        // page with sidebar after admin clicks a module
        rootPanel.add(adminSidebarPanel(), "AdminSidebar");

        add(rootPanel);

        rootCardLayout.show(rootPanel, "DashboardMenu");

        setVisible(true);
    }

    private JPanel dashboardMenuPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(40, 80, 40, 80));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;

        JLabel titleLabel = new JLabel("ADMIN DASHBOARD");
        titleLabel.setFont(new Font("Dialog", Font.BOLD, 36));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel subtitleLabel = new JLabel("Smart Equipment Rental");
        subtitleLabel.setFont(new Font("Dialog", Font.PLAIN, 16));
        subtitleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel welcomeLabel = new JLabel("Welcome, Admin");
        welcomeLabel.setFont(new Font("Dialog", Font.BOLD, 26));
        welcomeLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel chooseLabel = new JLabel("Choose a management function");
        chooseLabel.setFont(new Font("Dialog", Font.PLAIN, 18));
        chooseLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JButton equipmentButton = dashboardButton("Equipment Management");
        JButton pricingButton = dashboardButton("Manage Special Pricing");
        JButton logoutButton = dashboardButton("Logout");

        equipmentButton.addActionListener(e -> {
            moduleCardLayout.show(modulePanel, "EquipmentManagement");
            rootCardLayout.show(rootPanel, "AdminSidebar");
        });

        pricingButton.addActionListener(e -> {
            moduleCardLayout.show(modulePanel, "SpecialPricingManagement");
            rootCardLayout.show(rootPanel, "AdminSidebar");
        });

        logoutButton.addActionListener(new LogoutListener());

        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 5, 0);
        panel.add(titleLabel, gbc);

        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 50, 0);
        panel.add(subtitleLabel, gbc);

        gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, 10, 0);
        panel.add(welcomeLabel, gbc);

        gbc.gridy = 3;
        gbc.insets = new Insets(0, 0, 50, 0);
        panel.add(chooseLabel, gbc);

        gbc.gridy = 4;
        gbc.insets = new Insets(0, 0, 18, 0);
        panel.add(equipmentButton, gbc);

        gbc.gridy = 5;
        gbc.insets = new Insets(0, 0, 18, 0);
        panel.add(pricingButton, gbc);

        gbc.gridy = 6;
        gbc.insets = new Insets(20, 0, 0, 0);
        panel.add(logoutButton, gbc);

        return panel;
    }

    private JButton dashboardButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Dialog", Font.PLAIN, 22));
        button.setPreferredSize(new Dimension(360, 65));
        button.setFocusPainted(false);
        return button;
    }

    private JPanel adminSidebarPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // =========================
        // SIDEBAR CODING START
        // This sidebar appears after admin opens a module.
        // It lets admin move between dashboard, equipment, pricing, and logout.
        // =========================
        panel.add(sidebar(), BorderLayout.WEST);
        // =========================
        // SIDEBAR CODING END
        // =========================

        moduleCardLayout = new CardLayout();
        modulePanel = new JPanel(moduleCardLayout);

        modulePanel.add(new EquipmentManagement(), "EquipmentManagement");
        modulePanel.add(new SpecialPricingManagement(), "SpecialPricingManagement");

        panel.add(modulePanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel sidebar() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(280, 750));

        panel.add(sidebarHeader(), BorderLayout.NORTH);

        JPanel navPanel = new JPanel();
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));
        navPanel.setBorder(BorderFactory.createEmptyBorder(30, 25, 30, 25));

        JButton homeButton = sidebarButton("Dashboard");
        JButton equipmentButton = sidebarButton("Equipment Management");
        JButton pricingButton = sidebarButton("Manage Special Pricing");
        JButton logoutButton = sidebarButton("Logout");

        homeButton.addActionListener(e ->
                rootCardLayout.show(rootPanel, "DashboardMenu"));

        equipmentButton.addActionListener(e ->
                moduleCardLayout.show(modulePanel, "EquipmentManagement"));

        pricingButton.addActionListener(e ->
                moduleCardLayout.show(modulePanel, "SpecialPricingManagement"));

        logoutButton.addActionListener(new LogoutListener());

        navPanel.add(homeButton);
        navPanel.add(Box.createVerticalStrut(15));
        navPanel.add(equipmentButton);
        navPanel.add(Box.createVerticalStrut(15));
        navPanel.add(pricingButton);
        navPanel.add(Box.createVerticalGlue());
        navPanel.add(logoutButton);

        panel.add(navPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel sidebarHeader() {
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBorder(BorderFactory.createEmptyBorder(35, 20, 25, 20));

        JLabel titleLabel = new JLabel("ADMIN DASHBOARD");
        titleLabel.setFont(new Font("Dialog", Font.BOLD, 22));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Smart Equipment Rental");
        subtitleLabel.setFont(new Font("Dialog", Font.PLAIN, 14));
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitleLabel.setBorder(BorderFactory.createEmptyBorder(6, 0, 0, 0));

        header.add(titleLabel);
        header.add(subtitleLabel);

        return header;
    }

    private JButton sidebarButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Dialog", Font.PLAIN, 16));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(230, 50));
        button.setPreferredSize(new Dimension(230, 50));
        button.setFocusPainted(false);
        return button;
    }

    private class LogoutListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            Session current = SystemController.getCurrentUser();

            if (current != null) {
                current.closeUserSession();
            }

            dispose();
            new Landing();
        }
    }
}