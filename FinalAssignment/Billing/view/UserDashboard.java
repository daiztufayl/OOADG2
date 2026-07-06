package view;

import SystemController.Landing;
import SystemController.Session;
import SystemController.SystemController;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class UserDashboard extends JFrame {

    private CardLayout cardLayout;
    private JPanel mainPanel;

    public UserDashboard() {
        super("User Dashboard");

        setSize(1000, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        setLayout(new BorderLayout());

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // Pages
        mainPanel.add(welcomePanel(), "Welcome");
        mainPanel.add(new RentalManagement(), "RentalManagement");

        // Placeholder for Adam's module
        JPanel pricingPanel = new JPanel(new BorderLayout());
        pricingPanel.add(
                new JLabel("Display Special Pricing", SwingConstants.CENTER),
                BorderLayout.CENTER);

        mainPanel.add(pricingPanel, "SpecialPricing");

        add(sidebar(), BorderLayout.WEST);
        add(mainPanel, BorderLayout.CENTER);

        cardLayout.show(mainPanel, "Welcome");

        setVisible(true);
    }

    private JPanel sidebar() {

        JPanel panel = new JPanel(new BorderLayout());
        panel.setPreferredSize(new Dimension(260, 750));

        panel.add(headerPanel(), BorderLayout.NORTH);

        JPanel navPanel = new JPanel(new GridLayout(5, 1, 0, 15));
        navPanel.setBorder(BorderFactory.createEmptyBorder(30, 20, 30, 20));

        JButton homeButton = new JButton("Home");
        JButton rentalButton = new JButton("Rental & Return");
        JButton pricingButton = new JButton("Display Special Pricing");
        JButton logoutButton = new JButton("Logout");

        homeButton.addActionListener(e ->
                cardLayout.show(mainPanel, "Welcome"));

        rentalButton.addActionListener(e ->
                cardLayout.show(mainPanel, "RentalManagement"));

        pricingButton.addActionListener(e ->
                cardLayout.show(mainPanel, "SpecialPricing"));

        logoutButton.addActionListener(new LogoutListener());

        navPanel.add(homeButton);
        navPanel.add(rentalButton);
        navPanel.add(pricingButton);
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

        JLabel titleLabel = new JLabel("USER DASHBOARD");
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

        Session current = SystemController.getCurrentUser();

        String name = (current != null) ? current.getName() : "User";
        String role = (current != null) ? current.getRole() : "";

        JLabel label = new JLabel(
                "<html><center>Welcome, " + name +
                "<br><br>Role: " + role +
                "</center></html>");

        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setFont(new Font("Dialog", Font.BOLD, 36));

        panel.add(label, BorderLayout.CENTER);

        return panel;
    }

    private class LogoutListener implements ActionListener {

        @Override
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