package view;

import SystemController.Landing;
import SystemController.Session;
import SystemController.SystemController;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class AdminDashboard extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainPanel;

    public AdminDashboard() {
        super("Admin Dashboard");

        setSize(1000, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        setLayout(new BorderLayout());

        add(sidebar(), BorderLayout.WEST);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        mainPanel.add(welcomePanel(), "Welcome");

        // Tufayl's part: Equipment Management page
        mainPanel.add(new EquipmentManagement(), "EquipmentManagement");

        // Other members can add their pages here later
        // example: mainPanel.add(new RentalManagement(), "RentalManagement");

        add(mainPanel, BorderLayout.CENTER);

        cardLayout.show(mainPanel, "Welcome");
        setVisible(true);
    }

    private JPanel sidebar() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(260, 750));

        // header banner: "ADMIN DASHBOARD" + subtitle
        panel.add(headerPanel(), BorderLayout.NORTH);

        // nav buttons
        JPanel navPanel = new JPanel(new GridLayout(5, 1, 0, 15));
        navPanel.setBorder(BorderFactory.createEmptyBorder(30, 20, 30, 20));

        JButton homeButton = new JButton("Home");
        JButton equipmentButton = new JButton("Equipment Management");
        JButton logoutButton = new JButton("Logout");

        // Other members can add more sidebar buttons here

        homeButton.addActionListener(e -> cardLayout.show(mainPanel, "Welcome"));
        equipmentButton.addActionListener(e -> cardLayout.show(mainPanel, "EquipmentManagement"));
        logoutButton.addActionListener(new LogoutListener());

        navPanel.add(homeButton);
        navPanel.add(equipmentButton);
        navPanel.add(new JLabel(""));
        navPanel.add(new JLabel(""));
        navPanel.add(logoutButton);

        setFontForPanel(navPanel, 20);

        panel.add(navPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel headerPanel() {
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBorder(BorderFactory.createEmptyBorder(30, 20, 20, 20));

        JLabel titleLabel = new JLabel("ADMIN DASHBOARD");
        titleLabel.setFont(new Font("Dialog", Font.BOLD, 22));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Smart Equipment Rental");
        subtitleLabel.setFont(new Font("Dialog", Font.PLAIN, 14));
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitleLabel.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));

        header.add(titleLabel);
        header.add(subtitleLabel);

        return header;
    }

    private JPanel welcomePanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JLabel label = new JLabel("Welcome, Admin");
        label.setFont(new Font("Dialog", Font.BOLD, 44));
        label.setHorizontalAlignment(SwingConstants.CENTER);

        panel.add(label, BorderLayout.CENTER);

        return panel;
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

    private void setFontForPanel(Container container, int size) {
        for (Component component : container.getComponents()) {
            component.setFont(new Font("Dialog", Font.PLAIN, size));

            if (component instanceof Container) {
                setFontForPanel((Container) component, size);
            }
        }
    }
}