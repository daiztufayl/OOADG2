package SystemController;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;

public class Landing extends JFrame {

    final static String card1 = "Welcome to the SERBS";
    final static String card2 = "RegisterCard";
    final static String card3 = "LoginCard";
    static CardLayout c1;
    static Container contentPane;

    public Landing() {
        super("Welcome to the Smart Equipment Rental & Billing System");
        setLayout(new CardLayout());
        JPanel landingPage = landingPageInit();
        add(landingPage, card1);

        JPanel registerPage = new Register();
        add(registerPage, card2);
        JPanel loginPage = new Login();
        add(loginPage, card3);

        c1 = (CardLayout) this.getContentPane().getLayout();
        contentPane = getContentPane();
        c1.show(contentPane, card1);

        setSize(900, 900); // set size of frame and other standard formatting of jframe
        setVisible(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    private JPanel landingPageInit() {
        JPanel landingPage = new JPanel(new BorderLayout());
        JLabel landingPageLabel = new JLabel(card1);

        // make font bigger, create border so it's not hugging the top and have it in
        // the middle instead of to the left
        landingPageLabel.setFont(new Font("Dialog", Font.PLAIN, 72));
        landingPageLabel.setBorder(BorderFactory.createEmptyBorder(50, 0, 50, 0));
        landingPageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        landingPage.add(landingPageLabel, BorderLayout.NORTH);

        JPanel authPanel = authPanel();
        landingPage.add(authPanel, BorderLayout.CENTER);

        return landingPage;
    }

    private JPanel authPanel() {
        JPanel authPanel = new JPanel(new BorderLayout());
        // creates outer panel with borderlayout (just to add a little padding so the
        // buttons dont stretch across the whole thing)
        authPanel.setBorder(BorderFactory.createEmptyBorder(50, 200, 50, 200));
        // adds the padding by setting an invisible border

        // actual panel for the buttons (vgap 100 means there's space between the
        // buttons)
        JPanel buttonPanel = new JPanel(new GridLayout(2, 1, 0, 100));
        JButton[] authButtons = new JButton[2]; // init buttons

        // listener for the 'pick role' buttons
        class AuthListener implements ActionListener {
            public void actionPerformed(ActionEvent e) {
                int buttonInt = Integer.valueOf(e.getActionCommand());
                // gets cardlayout of the outer frame
                switch (buttonInt) {
                    case 0:
                        c1.show(contentPane, card2); // switches active page to 'Register'

                        break;
                    case 1:
                        c1.show(contentPane, card3); // switches active page to 'Login'

                        break;
                }
            }
        }

        // initialises the three buttons
        for (int i = 0; i < 2; i++) {
            switch (i) {
                case 0:
                    authButtons[0] = new JButton("Register");
                    break;
                case 1:
                    authButtons[1] = new JButton("Login");
                    break;
            }
            authButtons[i].setActionCommand(String.valueOf(i)); // set identifier of buttons for switch/case
            authButtons[i].addActionListener(new AuthListener()); // set listener detailed above
            authButtons[i].setFont(new Font("Dialog", Font.PLAIN, 48)); // set font size to be bigger cuz default is
                                                                        // tiny
            buttonPanel.add(authButtons[i]); // add button to role panel
        }

        authPanel.add(buttonPanel, BorderLayout.CENTER);
        return authPanel;
    }

    protected static void redirectLanding() {
        c1.show(contentPane, card1);
    }

}