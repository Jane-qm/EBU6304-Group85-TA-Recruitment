package ui.auth;

import modules.auth.AuthService;
import modules.user.UserRole;
import ui.common.BaseFrame;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPasswordField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;

public class RegisterFrame extends BaseFrame {

    private final AuthService authService;
    private JTextField prefixField;
    private JComboBox<String> domainCombo;
    private JPasswordField passwordField;
    private JPasswordField confirmField;
    private javax.swing.JCheckBox showPasswordBox;

    public RegisterFrame() {
        super("TA Registration", LoginFrame.FRAME_WIDTH, LoginFrame.FRAME_HEIGHT);
        this.authService = new AuthService();
        initUI();
    }

    @Override
    protected void initUI() {
        JPanel rootPanel = new JPanel(new BorderLayout());
        rootPanel.setBackground(new Color(242, 245, 250));

        JPanel shellPanel = new JPanel(new BorderLayout());
        shellPanel.setOpaque(false);
        shellPanel.setPreferredSize(new Dimension(LoginFrame.SHELL_WIDTH, LoginFrame.SHELL_HEIGHT));
        shellPanel.setMinimumSize(new Dimension(LoginFrame.SHELL_WIDTH, LoginFrame.SHELL_HEIGHT));
        shellPanel.setMaximumSize(new Dimension(LoginFrame.SHELL_WIDTH, LoginFrame.SHELL_HEIGHT));

        shellPanel.add(LoginFrame.createBrandPanel(), BorderLayout.WEST);
        shellPanel.add(createFormSection(), BorderLayout.CENTER);

        rootPanel.add(shellPanel, BorderLayout.CENTER);
        setContentPane(rootPanel);
    }

    private JComponent createFormSection() {
        JPanel cardPanel = new JPanel();
        cardPanel.setOpaque(false);
        cardPanel.setLayout(new BoxLayout(cardPanel, BoxLayout.Y_AXIS));
        cardPanel.setBorder(new EmptyBorder(24, 52, 20, 52));

        JLabel roleLabel = new JLabel("TA");
        roleLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        roleLabel.setForeground(new Color(37, 99, 235));
        roleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        cardPanel.add(roleLabel);
        cardPanel.add(Box.createVerticalStrut(8));

        JLabel titleLabel = new JLabel("Create an Account");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 30));
        titleLabel.setForeground(new Color(17, 24, 39));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        cardPanel.add(titleLabel);
        cardPanel.add(Box.createVerticalStrut(10));

        JLabel subtitleLabel = new JLabel("Register as a teaching assistant");
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));
        subtitleLabel.setForeground(new Color(107, 114, 128));
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        cardPanel.add(subtitleLabel);
        cardPanel.add(Box.createVerticalStrut(24));

        prefixField = new LoginFrame.PromptTextField("Enter email prefix");
        prefixField.setFont(new Font("SansSerif", Font.PLAIN, 16));
        prefixField.setBorder(new EmptyBorder(0, 0, 0, 0));

        domainCombo = new JComboBox<>(new String[]{"@qmul.ac.uk", "@bupt.edu.cn"});
        domainCombo.setFont(new Font("SansSerif", Font.PLAIN, 14));
        domainCombo.setPreferredSize(new Dimension(132, LoginFrame.FIELD_HEIGHT - 10));
        domainCombo.setMaximumSize(new Dimension(132, LoginFrame.FIELD_HEIGHT - 10));

        cardPanel.add(LoginFrame.createFieldLabel("University Email"));
        cardPanel.add(Box.createVerticalStrut(10));
        cardPanel.add(wrapEmailField(prefixField, domainCombo));
        cardPanel.add(Box.createVerticalStrut(20));

        passwordField = new JPasswordField();
        cardPanel.add(LoginFrame.createFieldLabel("Password"));
        cardPanel.add(Box.createVerticalStrut(10));
        cardPanel.add(LoginFrame.wrapTextField(passwordField));
        cardPanel.add(Box.createVerticalStrut(16));

        confirmField = new JPasswordField();
        cardPanel.add(LoginFrame.createFieldLabel("Confirm Password"));
        cardPanel.add(Box.createVerticalStrut(10));
        cardPanel.add(LoginFrame.wrapTextField(confirmField));
        cardPanel.add(Box.createVerticalStrut(10));

        showPasswordBox = new javax.swing.JCheckBox("Show password");
        showPasswordBox.setFont(new Font("SansSerif", Font.PLAIN, 14));
        showPasswordBox.setForeground(new Color(55, 65, 81));
        showPasswordBox.setOpaque(false);
        showPasswordBox.setFocusPainted(false);
        showPasswordBox.setAlignmentX(Component.CENTER_ALIGNMENT);
        char passwordEchoChar = passwordField.getEchoChar();
        char confirmEchoChar = confirmField.getEchoChar();
        showPasswordBox.addActionListener(e -> {
            boolean show = showPasswordBox.isSelected();
            passwordField.setEchoChar(show ? (char) 0 : passwordEchoChar);
            confirmField.setEchoChar(show ? (char) 0 : confirmEchoChar);
        });
        cardPanel.add(showPasswordBox);
        cardPanel.add(Box.createVerticalStrut(22));

        JButton registerBtn = new JButton("Register");
        registerBtn.setFont(new Font("SansSerif", Font.BOLD, 22));
        registerBtn.setForeground(Color.WHITE);
        registerBtn.setBackground(new Color(37, 99, 235));
        registerBtn.setFocusPainted(false);
        registerBtn.setBorderPainted(false);
        registerBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        registerBtn.addActionListener(e -> handleRegister());

        LoginFrame.RoundedPanel btnPanel = new LoginFrame.RoundedPanel(16, new Color(37, 99, 235));
        btnPanel.setLayout(new BorderLayout());
        btnPanel.setMaximumSize(new Dimension(LoginFrame.CONTENT_WIDTH, LoginFrame.FIELD_HEIGHT));
        btnPanel.setPreferredSize(new Dimension(LoginFrame.CONTENT_WIDTH, LoginFrame.FIELD_HEIGHT));
        btnPanel.setMinimumSize(new Dimension(LoginFrame.CONTENT_WIDTH, LoginFrame.FIELD_HEIGHT));
        btnPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnPanel.add(registerBtn, BorderLayout.CENTER);
        cardPanel.add(btnPanel);
        cardPanel.add(Box.createVerticalStrut(16));

        JPanel backPanel = new JPanel();
        backPanel.setOpaque(false);
        JButton backBtn = new JButton("Back to Login");
        backBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        backBtn.setForeground(new Color(107, 114, 128));
        backBtn.setContentAreaFilled(false);
        backBtn.setBorderPainted(false);
        backBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backBtn.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            dispose();
        });
        backPanel.add(backBtn);
        cardPanel.add(backPanel);

        getRootPane().setDefaultButton(registerBtn);
        return LoginFrame.createFormSectionWrapper(cardPanel);
    }

    private JPanel wrapEmailField(JTextField emailField, JComboBox<String> comboBox) {
        JPanel emailWrapper = new JPanel(new BorderLayout(8, 0));
        emailWrapper.setBackground(Color.WHITE);
        emailWrapper.setMaximumSize(new Dimension(LoginFrame.CONTENT_WIDTH, LoginFrame.FIELD_HEIGHT));
        emailWrapper.setPreferredSize(new Dimension(LoginFrame.CONTENT_WIDTH, LoginFrame.FIELD_HEIGHT));
        emailWrapper.setMinimumSize(new Dimension(LoginFrame.CONTENT_WIDTH, LoginFrame.FIELD_HEIGHT));
        emailWrapper.setAlignmentX(Component.CENTER_ALIGNMENT);
        emailWrapper.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                new LoginFrame.RoundedBorder(16, new Color(220, 224, 230), 1),
                new EmptyBorder(0, 16, 0, 16)
        ));
        emailWrapper.add(emailField, BorderLayout.CENTER);
        emailWrapper.add(comboBox, BorderLayout.EAST);
        return emailWrapper;
    }

    private void handleRegister() {
        String prefix = prefixField.getText().trim();
        String domain = (String) domainCombo.getSelectedItem();
        String email = prefix + domain;
        String pwd = new String(passwordField.getPassword());
        String confirm = new String(confirmField.getPassword());

        if (prefix.isEmpty() || pwd.isEmpty() || confirm.isEmpty()) {
            showWarning("All fields are required.");
            return;
        }
        if (!pwd.equals(confirm)) {
            showWarning("Passwords do not match.");
            return;
        }

        try {
            authService.register(email, pwd, UserRole.TA);
            showInfo("Registration successful!");

            new LoginFrame().setVisible(true);
            dispose();
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    @Override
    protected void onWindowMaximized() {
        // no-op
    }

    @Override
    protected void onWindowRestored() {
        // no-op
    }
}
