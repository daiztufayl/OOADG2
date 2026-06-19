
//import java.sql.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Login extends JFrame {
    // private Connection conn;

    static String role;

    public Login() {
        /*
         * try {
         * String databaseURL = "jdbc:postgresql://localhost:5432/hmstest";
         * String username = "postgres";
         * String password = "Zxcv123";
         * this.conn = DriverManager.getConnection(databaseURL, username, password);
         * } catch (SQLException e) {
         * e.printStackTrace();
         * }
         */
        super("Login to Hospital Management System");
        setLayout(new BorderLayout());
        JLabel pickRole = new JLabel("Pick Role to Login");
        pickRole.setFont(new Font("Dialog", Font.PLAIN, 72));
        pickRole.setBorder(BorderFactory.createEmptyBorder(50, 0, 50, 0));
        add(pickRole, BorderLayout.NORTH);
        pickRole.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel loginGUI = LoginGuiInit();
        add(loginGUI, BorderLayout.CENTER);
        setSize(900, 900);

        setVisible(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    private JPanel LoginGuiInit() {
        JPanel loginGUI = new JPanel(new BorderLayout());
        loginGUI.setBorder(BorderFactory.createEmptyBorder(50, 200, 50, 200));

        JPanel rolePanel = new JPanel(new GridLayout(3, 1, 0, 100));
        JButton[] roleButtons = new JButton[3];
        loginGUI.add(rolePanel, BorderLayout.CENTER);

        class RoleListener implements ActionListener {
            public void actionPerformed(ActionEvent e) {
                int roleInt = Integer.valueOf(e.getActionCommand());
                switch (roleInt) {
                    case 0:
                        role = "AD";
                        break;
                    case 1:
                        role = "RC";
                        break;
                    case 2:
                        role = "DR";
                        break;
                }
            }
        }

        for (int i = 0; i < 3; i++) {
            switch (i) {
                case 0:
                    roleButtons[0] = new JButton("Admin");
                    break;
                case 1:
                    roleButtons[1] = new JButton("Receptionist");
                    break;
                case 2:
                    roleButtons[2] = new JButton("Doctor");
                    break;
            }
            roleButtons[i].setActionCommand(String.valueOf(i));
            roleButtons[i].addActionListener(new RoleListener());
            roleButtons[i].setFont(new Font("Dialog", Font.PLAIN, 48));
            rolePanel.add(roleButtons[i]);
        }
        return loginGUI;
    }

    public static void main(String[] args) {
        new Login();
    }
}