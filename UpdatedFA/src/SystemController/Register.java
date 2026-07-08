package SystemController;

import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.ArrayList;
import javax.swing.*;

public class Register extends JPanel {
    private final Connection conn;

    // Temporary storage for input data
    private static String role;
    private static String username;
    private static String password;
    private static String confPass;
    private static String name;

    public Register() {
        conn = SystemController.getDBConnection();

        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(25, 50, 50, 50));

        JLabel topLabel = new JLabel("Register");
        topLabel.setFont(new Font("Dialog", Font.PLAIN, 72));
        topLabel.setBorder(
                BorderFactory.createEmptyBorder(25, 0, 25, 0)
        );
        topLabel.setHorizontalAlignment(SwingConstants.CENTER);

        add(topLabel, BorderLayout.NORTH);
        add(registerFormInit(), BorderLayout.CENTER);
    }

    private JPanel registerFormInit() {
        JPanel registerForm = new JPanel(new GridBagLayout());

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.weightx = 1;
        constraints.weighty = 1;
        constraints.fill = GridBagConstraints.BOTH;
        constraints.insets = new Insets(10, 20, 10, 20);

        JPanel roleChoice = roleChoiceInit();

        RegistrationInfoBox usernameBox =
                new RegistrationInfoBox("Enter Username:");

        RegistrationInfoBox passwordBox =
                new RegistrationInfoBox("Enter Password:");

        RegistrationInfoBox confPassBox =
                new RegistrationInfoBox("Confirm Password:");

        RegistrationInfoBox nameBox =
                new RegistrationInfoBox("Enter Name:");

        // First row: Role and Username
        constraints = setGridBag(constraints, 0, 0);
        registerForm.add(roleChoice, constraints);

        constraints = setGridBag(constraints, 1, 0);
        registerForm.add(usernameBox, constraints);

        // Second row: Password and Confirm Password
        constraints = setGridBag(constraints, 0, 1);
        registerForm.add(passwordBox, constraints);

        constraints = setGridBag(constraints, 1, 1);
        registerForm.add(confPassBox, constraints);

        // Third row: Name uses the whole row
        constraints = setGridBag(constraints, 0, 2, 2);
        registerForm.add(nameBox, constraints);

        JButton registerButton = new JButton("Confirm");
        registerButton.setFont(new Font("Dialog", Font.PLAIN, 32));

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setFont(new Font("Dialog", Font.PLAIN, 32));

        registerButton.addActionListener(e -> {
            username = usernameBox.getText().trim();
            password = passwordBox.getText();
            confPass = confPassBox.getText();
            name = nameBox.getText().trim();

            if (role == null) {
                JOptionPane.showMessageDialog(
                        this,
                        "Invalid Registration: Please select a role.",
                        "Registration Attempt Failed",
                        JOptionPane.INFORMATION_MESSAGE
                );

            } else if (username.isEmpty()
                    || password.isEmpty()
                    || confPass.isEmpty()
                    || name.isEmpty()) {

                JOptionPane.showMessageDialog(
                        this,
                        "Invalid Registration: Please fill in all fields.",
                        "Registration Attempt Failed",
                        JOptionPane.INFORMATION_MESSAGE
                );

            } else if (!SystemController.alphaNumValidate(username)
                    || !SystemController.alphaNumValidate(password)
                    || !SystemController.alphaNumValidate(confPass)
                    || !validateName(name)) {

                JOptionPane.showMessageDialog(
                        this,
                        "Invalid Registration: Username and password may only "
                                + "contain letters and numbers. "
                                + "Name may only contain letters and spaces.",
                        "Registration Attempt Failed",
                        JOptionPane.INFORMATION_MESSAGE
                );

            } else if (!password.equals(confPass)) {
                // Use equals(), not matches()
                JOptionPane.showMessageDialog(
                        this,
                        "Registration Failed: Passwords do not match.",
                        "Registration Attempt Failed",
                        JOptionPane.INFORMATION_MESSAGE
                );

            } else if (usernameExists(username)) {
                JOptionPane.showMessageDialog(
                        this,
                        "Registration Failed: Username already exists.",
                        "Registration Attempt Failed",
                        JOptionPane.INFORMATION_MESSAGE
                );

            } else {
                completeRegistration();
            }
        });

        cancelButton.addActionListener(new CancelListener());

        constraints = setGridBag(constraints, 0, 3);
        registerForm.add(registerButton, constraints);

        constraints = setGridBag(constraints, 1, 3);
        registerForm.add(cancelButton, constraints);

        return registerForm;
    }

    private GridBagConstraints setGridBag(
            GridBagConstraints constraints,
            int x,
            int y
    ) {
        constraints.gridx = x;
        constraints.gridy = y;
        constraints.gridwidth = 1;

        return constraints;
    }

    private GridBagConstraints setGridBag(
            GridBagConstraints constraints,
            int x,
            int y,
            int width
    ) {
        constraints.gridx = x;
        constraints.gridy = y;
        constraints.gridwidth = width;

        return constraints;
    }

    private JPanel roleChoiceInit() {
        ArrayList<String> roleList = RoleLookup.getRoleList();

        String[] roleArray;

        // Ignore Admin at index 0
        if (roleList.size() > 1) {
            roleArray = roleList
                    .subList(1, roleList.size())
                    .toArray(new String[0]);
        } else {
            roleArray = new String[0];
        }

        JPanel roleChoice = new JPanel(new BorderLayout());

        JLabel roleChoiceLabel = new JLabel("Select Role:");
        roleChoiceLabel.setFont(new Font("Dialog", Font.PLAIN, 32));

        JComboBox<String> roleChoiceBox = new JComboBox<>(roleArray);
        roleChoiceBox.setFont(new Font("Dialog", Font.PLAIN, 32));

        // Store the default selected role immediately
        if (roleArray.length > 0) {
            roleChoiceBox.setSelectedIndex(0);
            role = roleArray[0];
        } else {
            role = null;
        }

        roleChoiceBox.addActionListener(e -> {
            Object selectedRole = roleChoiceBox.getSelectedItem();

            if (selectedRole != null) {
                role = selectedRole.toString();
            }
        });

        roleChoice.add(roleChoiceLabel, BorderLayout.NORTH);
        roleChoice.add(roleChoiceBox, BorderLayout.CENTER);

        return roleChoice;
    }

    private boolean validateName(String inputName) {
        return inputName.matches("^[A-Za-z]+( [A-Za-z]+)*$");
    }

    private boolean usernameExists(String inputUsername) {
        String sql =
                "SELECT COUNT(*) AS matchCount " +
                        "FROM sysUser " +
                        "WHERE user_username = ?";

        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, inputUsername);

            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return result.getInt("matchCount") > 0;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();

            JOptionPane.showMessageDialog(
                    this,
                    "Unable to check username: " + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
            );

            // Stop registration if the database check fails
            return true;
        }

        return false;
    }

    private void completeRegistration() {
        int roleId = RoleLookup.roleIDLookup(role);

        if (roleId == -1) {
            JOptionPane.showMessageDialog(
                    this,
                    "Registration failed: Invalid role selected.",
                    "Registration Failed",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        String sql =
                "INSERT INTO sysUser " +
                        "(user_role, user_username, user_password, user_name) " +
                        "VALUES (?, ?, ?, ?)";

        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setInt(1, roleId);
            statement.setString(2, username);
            statement.setString(3, password);
            statement.setString(4, name);

            statement.executeUpdate();

            // Successful account creation popup
            JOptionPane.showMessageDialog(
                    this,
                    "Account created successfully. Please login.",
                    "Registration Successful",
                    JOptionPane.INFORMATION_MESSAGE
            );

            clearStoredUserFields();
            Landing.redirectLanding();

        } catch (SQLException e) {
            e.printStackTrace();

            JOptionPane.showMessageDialog(
                    this,
                    "Failed to create account: " + e.getMessage(),
                    "Registration Failed",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void clearStoredUserFields() {
        username = null;
        password = null;
        confPass = null;
        name = null;

        // Do not set role to null because a role remains selected
        // in the combo box.
    }

    private class RegistrationInfoBox extends JPanel {
        private final JTextField enterRegInfo;

        RegistrationInfoBox(String labelName) {
            setLayout(new BorderLayout());

            JLabel regInfoLabel = new JLabel(labelName);
            regInfoLabel.setFont(new Font("Dialog", Font.PLAIN, 32));

            enterRegInfo = new JTextField();
            enterRegInfo.setFont(new Font("Dialog", Font.PLAIN, 32));

            add(regInfoLabel, BorderLayout.NORTH);
            add(enterRegInfo, BorderLayout.CENTER);
        }

        String getText() {
            return enterRegInfo.getText();
        }
    }

    private class CancelListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            clearStoredUserFields();
            Landing.redirectLanding();
        }
    }
}
