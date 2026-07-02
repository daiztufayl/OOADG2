package SystemController;

import java.sql.*;
import javax.swing.*;

import java.awt.*;
import java.awt.event.*;

public class Login extends JPanel {
    private Connection conn;

    // identifiers for each 'page'
    final static String card1 = "Pick Role to Login";
    final static String card2 = "Login as Admin";
    final static String card3 = "Login as Student";
    final static String card4 = "Login as Lecturer";
    final static String card5 = "Return to Landing Page";
    final static String cardOuter = "Welcome to the SERBS";
    static String role;
    Session currUser;
    CardLayout c1;

    public Login() {
        this.conn = SystemController.getDBConnection(); // titles the Login JFrame
        setLayout(new CardLayout()); // sets the JFrame's Content Pane to use CardLayout for swapping pages

        JPanel pickRolePage = pickRolePageInit(); // initialises the 'pick role to login as' page and adds it to frame
                                                  // with the identifier card1
        add(pickRolePage, card1);

        // initialises login as {role} pages and adds it to cardlayout with identifiers
        JPanel AdminLogin = roleLoginInit(card2);
        add(AdminLogin, card2);
        JPanel StudentLogin = roleLoginInit(card3);
        add(StudentLogin, card3);
        JPanel LecturerLogin = roleLoginInit(card4);
        add(LecturerLogin, card4);

        c1 = (CardLayout) (this.getLayout()); // gets cardlayout of this frame
        c1.show(this, card1); // shows page identified by 'card1': 'pick a role to login as'
    }

    // initialiser for 'pick role to login as' page
    private JPanel pickRolePageInit() {
        JPanel pickRolePage = new JPanel(new BorderLayout()); // creates panel for page to hold stuff
        JLabel pickRole = topLabel(card1); // sets a label at the top saying 'Pick Role to Login'

        pickRolePage.add(pickRole, BorderLayout.NORTH); // adds to top of panel

        JPanel loginGUI = loginGuiInit(); // creates additional grid panel for the buttons
        pickRolePage.add(loginGUI, BorderLayout.CENTER); // adds to center of panel

        return pickRolePage;
    }

    // initialiser for 'pick role to login as' page's login buttons
    private JPanel loginGuiInit() {
        JPanel loginGUI = new JPanel(new BorderLayout());
        // creates outer panel with borderlayout (just to add a little padding so the
        // buttons dont stretch across the whole thing)
        loginGUI.setBorder(BorderFactory.createEmptyBorder(50, 200, 50, 200));
        // adds the padding by setting an invisible border

        // actual panel for the buttons (vgap 100 means there's space between the
        // buttons)
        JPanel rolePanel = new JPanel(new GridLayout(4, 1, 0, 50));
        JButton[] roleButtons = new JButton[4]; // init buttons

        // listener for the 'pick role' buttons
        class RoleListener implements ActionListener {
            public void actionPerformed(ActionEvent e) {
                int buttonInt = Integer.valueOf(e.getActionCommand());
                // gets cardlayout of the outer frame

                switch (buttonInt) {
                    case 0:
                        role = "AD"; // sets role for authenticating credentials with database

                        c1.show(Login.this, card2); // switches active page to 'Login as Admin'

                        break;
                    case 1:
                        role = "SD"; // sets role for authenticating credentials with database

                        c1.show(Login.this, card3); // switches active page to 'Login as Student'

                        break;
                    case 2:
                        role = "LC"; // sets role for authenticating credentials with database

                        c1.show(Login.this, card4); // switches active page to 'Login as Lecturer'

                        break;
                    case 3:
                        role = "";
                        Landing.redirectLanding();
                }
            }
        }

        // initialises the three buttons
        for (

                int i = 0; i < 4; i++) {
            switch (i) {
                case 0:
                    roleButtons[0] = new JButton("Admin");
                    break;
                case 1:
                    roleButtons[1] = new JButton("Student");
                    break;
                case 2:
                    roleButtons[2] = new JButton("Lecturer");
                    break;
                case 3:
                    roleButtons[3] = new JButton("Return to Landing");
            }
            roleButtons[i].setActionCommand(String.valueOf(i)); // set identifier of buttons for switch/case
            roleButtons[i].addActionListener(new RoleListener()); // set listener detailed above
            roleButtons[i].setFont(new Font("Dialog", Font.PLAIN, 48)); // set font size to be bigger cuz default is
                                                                        // tiny
            rolePanel.add(roleButtons[i]); // add button to role panel
        }

        // stretch rolepanel to fit most of the page
        // (to the extent the invisible border allows)
        loginGUI.add(rolePanel, BorderLayout.CENTER);

        return loginGUI;
    }

    // initialiser for 'Login as x pages'
    private JPanel roleLoginInit(String pageName) {
        JPanel loginPage = new JPanel(new BorderLayout()); // initialises login as {role} page
        JLabel loginLabel = topLabel(pageName); // label at the top saying 'login as {role}'

        loginPage.add(loginLabel, BorderLayout.NORTH); // places label at the top

        JPanel enterCreds = enterCreds(); // initialises panel for entering credentials + submit/cancel buttons
        loginPage.add(enterCreds, BorderLayout.CENTER); // makes it take up the rest of the screen

        return loginPage;
    }

    // format the labels at the top automatically
    private JLabel topLabel(String labelName) {
        JLabel topLabel = new JLabel(labelName);

        // make font bigger, create border so it's not hugging the top and have it in
        // the middle instead of to the left
        topLabel.setFont(new Font("Dialog", Font.PLAIN, 72));
        topLabel.setBorder(BorderFactory.createEmptyBorder(50, 0, 50, 0));
        topLabel.setHorizontalAlignment(SwingConstants.CENTER);

        return topLabel;
    }

    // initialises the enter credentials bit since it'll be the same across a few
    // pages anyway
    private JPanel enterCreds() {
        JPanel enterCreds = new JPanel(new GridLayout(4, 1, 0, 50)); // smaller vgap since 4 things instead of 3
        enterCreds.setBorder(BorderFactory.createEmptyBorder(50, 200, 50, 200)); // more invisible border stuffing

        // enter username box
        JPanel usernameBox = new JPanel(new BorderLayout()); // panel to hold label and box for entering info
        JLabel usernameBoxLabel = new JLabel("Enter Username:"); // separate label cuz otherwise you gotta empty the box
        usernameBoxLabel.setFont(new Font("Dialog", Font.PLAIN, 32)); // make font size bigger
        JTextField enterUser = new JTextField(); // box to enter username
        enterUser.setFont(new Font("Dialog", Font.PLAIN, 32)); // make font size bigger
        usernameBox.add(usernameBoxLabel, BorderLayout.NORTH); // places label at top
        usernameBox.add(enterUser, BorderLayout.CENTER); // box to enter pass takes rest of space

        // enter password box
        JPanel passwordBox = new JPanel(new BorderLayout()); // panel to hold label and box for entering info
        JLabel passwordBoxLabel = new JLabel("Enter Password:");// separate label cuz otherwise you gotta empty the box
        JTextField enterPass = new JTextField(); // box to enter password
        passwordBoxLabel.setFont(new Font("Dialog", Font.PLAIN, 32)); // make font size bigger
        enterPass.setFont(new Font("Dialog", Font.PLAIN, 32)); // make font size bigger
        passwordBox.add(passwordBoxLabel, BorderLayout.NORTH); // places label at top
        passwordBox.add(enterPass, BorderLayout.CENTER); // box to enter pass takes rest of space

        JButton confLogin = new JButton("Login"); // button to submit for authentication
        JButton cancLogin = new JButton("Return to Role Select"); // button to return to pick role screen

        class loginListener implements ActionListener { // listener for login button
            public void actionPerformed(ActionEvent e) {
                // gets text from the two textboxes of user input
                String usernameInput = enterUser.getText();
                String passwordInput = enterPass.getText();

                // check if box is empty
                if (usernameInput.length() == 0 || passwordInput.length() == 0) {
                    JOptionPane.showMessageDialog(null, "Invalid Input: username or password field is empty.",
                            "Login Attempt Failed", JOptionPane.INFORMATION_MESSAGE);
                }
                // check if input is alphanumeric
                else if (!SystemController.alphaNumValidate(usernameInput)
                        || !SystemController.alphaNumValidate(passwordInput)) {
                    JOptionPane.showMessageDialog(null, "Invalid Input: Only alphanumeric characters allowed.",
                            "Login Attempt Failed", JOptionPane.INFORMATION_MESSAGE);
                }
                // check if username and password given match a username-password pair in
                // database belonging to the current chosen userRole
                else if (!authenticate(usernameInput, passwordInput)) {
                    JOptionPane.showMessageDialog(null,
                            "Login Failed: Either username/password is incorrect or you have selected to Login under the wrong role.",
                            "Login Attempt Failed", JOptionPane.INFORMATION_MESSAGE);
                }
                // passed all checks, proceed to login
                else {
                    // stub
                    loginToDash(usernameInput);
                    JOptionPane.showMessageDialog(null,
                            "Successfuly Logged In.",
                            "Login Success", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        }

        class returnListener implements ActionListener { // listener for return to role select button
            public void actionPerformed(ActionEvent e) {
                c1.show(Login.this, card1); // switches to pickrolepage
            }
        }

        confLogin.addActionListener(new loginListener()); // attach listener to button
        confLogin.setFont(new Font("Dialog", Font.PLAIN, 32)); // make button font size bigger
        cancLogin.addActionListener(new returnListener()); // attach listener to button
        cancLogin.setFont(new Font("Dialog", Font.PLAIN, 32)); // make button font size bigger

        // adds the components to entercreds panel
        enterCreds.add(usernameBox);
        enterCreds.add(passwordBox);
        enterCreds.add(confLogin);
        enterCreds.add(cancLogin);

        return enterCreds;
    }

    // function to authenticate user
    private boolean authenticate(String username, String password) {
        String authString;
        if (role == "AD") {
            authString = "SELECT COUNT(*) AS matchCount FROM administrator WHERE admin_username = (?) AND admin_password = (?)";
        } else {
            authString = "SELECT COUNT(*) AS matchCount FROM sysUser WHERE user_role = (?) AND user_username = (?) AND user_password = (?)";
        }
        try {
            PreparedStatement authStatement = conn.prepareStatement(authString);
            if (role == "AD") {
                authStatement.setString(1, username);
                authStatement.setString(2, password);
            } else {
                authStatement.setInt(1, RoleLookup.roleIDLookup(role));
                authStatement.setString(2, username);
                authStatement.setString(3, password);
            }
            ResultSet matchCount = authStatement.executeQuery();

            matchCount.next();
            int matchResult = matchCount.getInt("matchCount");

            authStatement.close();
            return (matchResult == 1);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // function to set up current user and redirect to dashboard
    private void loginToDash(String username) {
        String loginString = "SELECT user_id, user_name FROM sysUser WHERE user_role = (?) AND user_username = (?)";
        try {
            int roleInt = RoleLookup.roleIDLookup(role);
            PreparedStatement logStatement = conn.prepareStatement(loginString);
            logStatement.setInt(1, roleInt);
            logStatement.setString(2, username);
            ResultSet result = logStatement.executeQuery();

            if (result.next()) {
                int userId = result.getInt("user_id");
                String userName = result.getString("user_name");
                currUser = new Session(role, userId, roleInt, username, userName);
            }

            // pass login info to system controller
            SystemController.setCurrentUser(currUser);

            // stub to send to dashboard
            if (role == "AD") {

            } else {

            }

            logStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}