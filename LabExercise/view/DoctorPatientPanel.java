package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

// Patient Record panel for Doctor role.
// Doctors can only view and update patient records, not add new ones.
public class DoctorPatientPanel extends JFrame {

    public DoctorPatientPanel() {
        setTitle("Patient Records");
        setSize(450, 280);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        JLabel lblTitle = new JLabel("Patient Records", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 20));
        mainPanel.add(lblTitle, BorderLayout.NORTH);

        JPanel menuPanel = new JPanel(new GridLayout(2, 1, 10, 15));

        JButton btnUpdatePatient = new JButton("Update Patient Details");
        btnUpdatePatient.setFont(new Font("Dialog", Font.PLAIN, 18));

        JButton btnViewHistory = new JButton("View Patient History");
        btnViewHistory.setFont(new Font("Dialog", Font.PLAIN, 18));

        // button to (Update Patient Details)
        btnUpdatePatient.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new UpdatePatientFrame().setVisible(true);
            }
        });

        // button to (View Patient History)
        btnViewHistory.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new ViewPatientHistoryFrame().setVisible(true);
            }
        });

        menuPanel.add(btnUpdatePatient);
        menuPanel.add(btnViewHistory);
        mainPanel.add(menuPanel, BorderLayout.CENTER);

        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnClose = new JButton("Close");
        btnClose.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        footerPanel.add(btnClose);
        mainPanel.add(footerPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new DoctorPatientPanel().setVisible(true);
            }
        });
    }
}