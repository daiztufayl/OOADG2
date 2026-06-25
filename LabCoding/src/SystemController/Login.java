package SystemController;

// remove SystemController imports - same package
import model.Admin;
import model.Doctor;
import model.Receptionist;
import model.User;
import view.AdminDashboard;    // ← needed for Admin.openDashboard()
import view.DoctorDashboard;   // ← needed for Doctor.openDashboard()
import view.ReceptionistDashboard; // ← needed for Receptionist.openDashboard()

import java.sql.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Login extends JFrame {
    private Connection conn;

    // identifiers for each 'page'
    final static String card1 = "Pick Role to Login";
    final static String card2 = "Login as Admin";
    final static String card3 = "Login as Doctor";
    final static String card4 = "Login as Receptionist";
    // role currently unused really but important for later database integration
    static String role;

    public Login() {

        this.conn = DBConnection.getDBConnection();

        super("Login to Hospital Management System"); // titles the Login JFrame
        setLayout(new CardLayout()); // sets the JFrame's Content Pane to use CardLayout for swapping pages

        JPanel pickRolePage = pickRolePageInit(); // initialises the 'pick role to login as' page and adds it to frame
        // with the identifier card1
        add(pickRolePage, card1);

        // initialises login as {role} pages and adds it to cardlayout with identifiers
        JPanel AdminLogin = roleLoginInit(card2);
        add(AdminLogin, card2);
        JPanel DoctorLogin = roleLoginInit(card3);
        add(DoctorLogin, card3);
        JPanel ReceptionistLogin = roleLoginInit(card4);
        add(ReceptionistLogin, card4);

        CardLayout c1 = (CardLayout) (this.getContentPane().getLayout()); // gets cardlayout of this frame
        c1.show(this.getContentPane(), card1); // shows page identified by 'card1': 'pick a role to login as'

        setSize(900, 900); // set size of frame and other standard formatting of jframe
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);
        setLocationRelativeTo(null);
    }

    // initialiser for 'pick role to login as' page
    private JPanel pickRolePageInit() {
        JPanel pickRolePage = new JPanel(new BorderLayout()); // creates panel for page to hold stuff
        JLabel pickRole = TopLabel(card1); // sets a label at the top saying 'Pick Role to Login'

        pickRolePage.add(pickRole, BorderLayout.NORTH); // adds to top of panel

        JPanel loginGUI = LoginGui(); // creates additional grid panel for the buttons
        pickRolePage.add(loginGUI, BorderLayout.CENTER); // adds to center of panel

        return pickRolePage;
    }

    // initialiser for 'pick role to login as' page's login buttons
    private JPanel LoginGui() {
        JPanel loginGUI = new JPanel(new BorderLayout());
        // creates outer panel with borderlayout (just to add a little padding so the
        // buttons dont stretch across the whole thing)
        loginGUI.setBorder(BorderFactory.createEmptyBorder(50, 200, 50, 200));
        // adds the padding by setting an invisible border

        // actual panel for the buttons (vgap 100 means there's space between the
        // buttons)
        JPanel rolePanel = new JPanel(new GridLayout(3, 1, 0, 100));
        JButton[] roleButtons = new JButton[3]; // init buttons

        // listener for the 'pick role' buttons
        class RoleListener implements ActionListener {
            public void actionPerformed(ActionEvent e) {
                int roleInt = Integer.valueOf(e.getActionCommand());
                // gets cardlayout of the outer frame
                CardLayout c1 = (CardLayout) (Login.this.getContentPane().getLayout());

                switch (roleInt) {
                    case 0:
                        role = "AD"; // sets role for authenticating credentials with database

                        c1.show(Login.this.getContentPane(), card2); // switches active page to 'Login as Admin'

                        break;
                    case 1:
                        role = "DR"; // sets role for authenticating credentials with database

                        c1.show(Login.this.getContentPane(), card3); // switches active page to 'Login as Doctor'

                        break;
                    case 2:
                        role = "RC"; // sets role for authenticating credentials with database

                        c1.show(Login.this.getContentPane(), card4); // switches active page to 'Login as Receptionist'

                        break;
                }
            }
        }

        // initialises the three buttons
        for (int i = 0; i < 3; i++) {
            switch (i) {
                case 0:
                    roleButtons[0] = new JButton("Admin");
                    break;
                case 1:
                    roleButtons[1] = new JButton("Doctor");
                    break;
                case 2:
                    roleButtons[2] = new JButton("Receptionist");
                    break;
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
        JPanel LoginPage = new JPanel(new BorderLayout()); // initialises login as {role} page
        JLabel Login = TopLabel(pageName); // label at the top saying 'login as {role}'

        LoginPage.add(Login, BorderLayout.NORTH); // places label at the top

        JPanel enterCreds = enterCreds(); // initialises panel for entering credentials + submit/cancel buttons
        LoginPage.add(enterCreds, BorderLayout.CENTER); // makes it take up the rest of the screen

        return LoginPage;
    }

    // format the labels at the top automatically
    private JLabel TopLabel(String labelName) {
        JLabel TopLabel = new JLabel(labelName);

        // make font bigger, create border so it's not hugging the top and have it in
        // the middle instead of to the left
        TopLabel.setFont(new Font("Dialog", Font.PLAIN, 72));
        TopLabel.setBorder(BorderFactory.createEmptyBorder(50, 0, 50, 0));
        TopLabel.setHorizontalAlignment(SwingConstants.CENTER);

        return TopLabel;
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
                else if (!alphaNumValidate(usernameInput) || !alphaNumValidate(passwordInput)) {
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
                    loginToDash(usernameInput);
                }
            }
        }

        class returnListener implements ActionListener { // listener for return to role select button
            public void actionPerformed(ActionEvent e) {
                CardLayout c1 = (CardLayout) (Login.this.getContentPane().getLayout());
                // gets cardlayout of outer frame
                c1.show(Login.this.getContentPane(), card1); // switches to pickrolepage
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

    // validates that an input contains only alphanumerical characters
    private boolean alphaNumValidate(String input) {
        return input.matches("^[A-Za-z0-9]+$");
    }

    //
    private boolean authenticate(String username, String password) {
        String authString = "SELECT COUNT(*) AS matchCount FROM authInfo WHERE user_role = (?) AND username = (?) AND password = (?)";
        try {
            PreparedStatement authStatement = conn.prepareStatement(authString);
            authStatement.setString(1, role);
            authStatement.setString(2, username);
            authStatement.setString(3, password);
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

    private void loginToDash(String username) {
        String loginString = "SELECT user_id, user_name FROM hmsuser WHERE user_role = (?) AND user_username = (?)";
        try {
            PreparedStatement logStatement = conn.prepareStatement(loginString);
            logStatement.setString(1, role);
            logStatement.setString(2, username);
            ResultSet result = logStatement.executeQuery();

            if (result.next()) {
                int userId = result.getInt("user_id");
                String userName = result.getString("user_name");
                Session.startUserSession(role, userId, username, userName);

                // polymorphism - each subclass opens its own dashboard
                User user;
                switch (role) {
                    case "AD": user = new Admin(userId, username, userName); break;
                    case "DR": user = new Doctor(userId, username, userName, ""); break;
                    default:   user = new Receptionist(userId, username, userName); break;
                }

                dispose();
                user.openDashboard(); // polymorphic call
            } else {
                JOptionPane.showMessageDialog(null, "User not found.",
                        "Login Error", JOptionPane.ERROR_MESSAGE);
            }
            logStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new Login();
            }
        });
    }
}