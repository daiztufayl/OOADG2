package SystemController;

import java.sql.*;
import java.util.ArrayList;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;

public class Register extends JPanel {
    private Connection conn;

    // temp storage for input data
    private static String role;
    private static String username;
    private static String password;
    private static String confPass;
    private static String name;

    public Register() {
        this.conn = SystemController.getDBConnection(); // get database connection
        setLayout(new BorderLayout());

        setBorder(BorderFactory.createEmptyBorder(25, 50, 50, 50));
        // adds padding by setting an invisible border

        JLabel topLabel = new JLabel("Register");
        // make font bigger, create border so it's not hugging the top and have it in
        // the middle instead of to the left
        topLabel.setFont(new Font("Dialog", Font.PLAIN, 72));
        topLabel.setBorder(BorderFactory.createEmptyBorder(25, 0, 25, 0));
        topLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(topLabel, BorderLayout.NORTH);

        JPanel registerForm = registerFormInit();
        add(registerForm, BorderLayout.CENTER);
    }

    private JPanel registerFormInit() {
        JPanel registerForm = new JPanel();
        registerForm.setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();

        // initialise constraints
        constraints.weightx = constraints.weighty = 1; // make components stretchable
        constraints.fill = GridBagConstraints.BOTH; // stretch components to fit
        constraints.insets = new Insets(10, 20, 10, 20); // add padding

        JPanel roleChoice = roleChoiceInit(); // pick role combo box

        constraints = setGridBag(constraints, 0, 0);
        registerForm.add(roleChoice);

        RegistrationInfoBox usernameBox = new RegistrationInfoBox("Enter Username:");
        RegistrationInfoBox passwordBox = new RegistrationInfoBox("Enter Password:");
        RegistrationInfoBox confPassBox = new RegistrationInfoBox("Confirm Password:");
        RegistrationInfoBox nameBox = new RegistrationInfoBox("Enter Name:");

        constraints = setGridBag(constraints, 1, 0);
        registerForm.add(usernameBox, constraints);
        constraints = setGridBag(constraints, 0, 1);
        registerForm.add(passwordBox, constraints);
        constraints = setGridBag(constraints, 1, 1);
        registerForm.add(confPassBox, constraints);
        constraints = setGridBag(constraints, 0, 2, 2);
        registerForm.add(nameBox, constraints);

        class RegisterListener implements ActionListener {
            public void actionPerformed(ActionEvent e) {
                username = usernameBox.getText();
                password = passwordBox.getText();
                confPass = confPassBox.getText();
                name = nameBox.getText();

                if (role == null) { // check if combobox has been interacted with

                    JOptionPane.showMessageDialog(null, "Invalid Registration: Please select a Role.",
                            "Registration Attempt Failed", JOptionPane.INFORMATION_MESSAGE);

                    clearStoredUserFields();

                } else if (username.length() == 0 || password.length() == 0 || confPass.length() == 0
                        || name.length() == 0) { // check if there's an empty field

                    JOptionPane.showMessageDialog(null, "Invalid Registration: Please fill in all fields.",
                            "Registration Attempt Failed", JOptionPane.INFORMATION_MESSAGE);

                    clearStoredUserFields();

                } else if (!SystemController.alphaNumValidate(username) || !SystemController.alphaNumValidate(password)
                        || !SystemController.alphaNumValidate(confPass) || !validateName(name)) { // validate fields

                    JOptionPane.showMessageDialog(null, "Invalid Registration: Only alphanumeric characters allowed.",
                            "Registration Attempt Failed", JOptionPane.INFORMATION_MESSAGE);

                    clearStoredUserFields();

                } else if (!password.matches(confPass)) { // check if passwords match

                    JOptionPane.showMessageDialog(null,
                            "Registration Failed: Passwords do not match.",
                            "Registration Attempt Failed", JOptionPane.INFORMATION_MESSAGE);

                    clearStoredUserFields();

                } else if (usernameExists(username)) { // check if a user with the same username already exists

                    JOptionPane.showMessageDialog(null,
                            "Registration Failed: Username already exists.",
                            "Registration Attempt Failed", JOptionPane.INFORMATION_MESSAGE);

                    clearStoredUserFields();

                } else {
                    completeRegistration(); // if all checks pass register new user info to database
                }
            }
        }

        JButton registerButton = new JButton("Confirm"); // button to confirm registration (and go back to landing if
                                                         // successful)
        registerButton.addActionListener(new RegisterListener());
        registerButton.setFont(new Font("Dialog", Font.PLAIN, 32)); // make font size bigger
        constraints = setGridBag(constraints, 0, 3);
        registerForm.add(registerButton, constraints);

        JButton cancelButton = new JButton("Cancel"); // button to cancel registration (go back to landing)
        cancelButton.addActionListener(new CancelListener());
        cancelButton.setFont(new Font("Dialog", Font.PLAIN, 32)); // make font size bigger
        constraints = setGridBag(constraints, 1, 3);
        registerForm.add(cancelButton, constraints);

        return registerForm;
    }

    private GridBagConstraints setGridBag(GridBagConstraints c, int x, int y) { // set position for next component added
        c.gridx = x;
        c.gridy = y;
        c.gridwidth = 1;
        return c;
    }

    private GridBagConstraints setGridBag(GridBagConstraints c, int x, int y, int w) { // set position for next
                                                                                       // component added + num of grids
                                                                                       // wide
        c.gridx = x;
        c.gridy = y;
        c.gridwidth = w;
        return c;
    }

    private JPanel roleChoiceInit() {
        ArrayList<String> roleList = RoleLookup.getRoleList(); // get list of roles
        String[] roleArray = roleList.subList(1, roleList.size()).toArray(new String[0]); // convert to array for use in
                                                                                          // combobox
        // take sublist from 1 to ignore Admin since you cant register as admin

        JPanel roleChoice = new JPanel(new BorderLayout()); // panel to hold label and combo box for selecting role
        JLabel roleChoiceLabel = new JLabel("Select Role:");
        JComboBox<String> roleChoiceBox = new JComboBox<>(roleArray);

        roleChoiceBox.addActionListener(new RoleChoiceListener());

        roleChoiceLabel.setFont(new Font("Dialog", Font.PLAIN, 32));
        roleChoiceBox.setFont(new Font("Dialog", Font.PLAIN, 32));

        roleChoice.add(roleChoiceLabel, BorderLayout.NORTH);
        roleChoice.add(roleChoiceBox, BorderLayout.CENTER);
        return roleChoice;
    }

    private class RegistrationInfoBox extends JPanel {
        private JTextField enterRegInfo;

        public RegistrationInfoBox(String labelName) {
            setLayout(new BorderLayout()); // initialise BorderLayout for stretching

            JPanel regInfoBox = new JPanel(new BorderLayout()); // panel to hold label and box for entering info
            JLabel regInfoLabel = new JLabel(labelName); // separate label cuz otherwise you gotta empty the box
            regInfoLabel.setFont(new Font("Dialog", Font.PLAIN, 32)); // make font size bigger

            enterRegInfo = new JTextField(); // box to enter info
            enterRegInfo.setFont(new Font("Dialog", Font.PLAIN, 32)); // make font size bigger

            regInfoBox.add(regInfoLabel, BorderLayout.NORTH); // places label at top
            regInfoBox.add(enterRegInfo, BorderLayout.CENTER); // box to enter info takes rest of space
            add(regInfoBox);
        }

        public String getText() { // getter to be able to get the textfield data
            return enterRegInfo.getText();
        }
    }

    // function to validate name
    private boolean validateName(String name) { // allows only letters + spaces between words
        return name.matches("^[A-Za-z]+( [A-Za-z]+)*$");
    }

    // function to check if username already exists in database
    private boolean usernameExists(String username) {
        String chkString = "SELECT COUNT(*) AS matchCount FROM sysUser WHERE user_username = (?)";
        try {
            PreparedStatement chkStatement = conn.prepareStatement(chkString);
            chkStatement.setString(1, username);

            ResultSet matchCount = chkStatement.executeQuery();

            matchCount.next();
            int matchResult = matchCount.getInt("matchCount");

            chkStatement.close();
            return (matchResult == 1);
        } catch (SQLException e) {
            e.printStackTrace();
            return true;
        }
    }

    // function to write new user info to database
    private void completeRegistration() {
        String registerString = "INSERT INTO sysUser (user_role,user_username,user_password,user_name) VALUES ((?),(?),(?),(?))";
        try {
            PreparedStatement registerStatement = conn.prepareStatement(registerString);
            registerStatement.setInt(1, RoleLookup.roleIDLookup(role));
            registerStatement.setString(2, username);
            registerStatement.setString(3, password);
            registerStatement.setString(4, name);

            registerStatement.execute();

            clearStoredUserFields();
            Landing.redirectLanding();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // clear stored data: invoked after failed registration or returning to landing
    // page
    private void clearStoredUserFields() {
        role = username = password = confPass = name = null;
    }

    // role choice and return to landing page listeners respectively
    class RoleChoiceListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            JComboBox<?> retrieveRoleChoice = (JComboBox<?>) e.getSource();
            role = (String) retrieveRoleChoice.getSelectedItem();
        }
    }

    class CancelListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            clearStoredUserFields();
            Landing.redirectLanding();
        }
    }
}
