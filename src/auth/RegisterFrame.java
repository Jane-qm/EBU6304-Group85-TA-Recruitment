package auth;

import common.entity.User;
import common.entity.UserRole;
import common.ui.BaseFrame;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;

public class RegisterFrame extends BaseFrame {
    private final AuthService authService;

    private LoginFrame.PromptTextField accountField;
    private JComboBox<String> domainCombo;
    private JPasswordField passwordField;
    private JPasswordField confirmField;
    private JLabel helperLabel;
    private JPanel taChip;
    private JPanel moChip;
    private JLabel taChipLabel;
    private JLabel moChipLabel;
    private JButton registerBtn;
    private javax.swing.JCheckBox showPasswordBox;

    public RegisterFrame() {
        super("TA Recruitment System - Register", LoginFrame.FRAME_WIDTH, LoginFrame.FRAME_HEIGHT);
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

        JLabel titleLabel = new JLabel("Create an Account");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 30));
        titleLabel.setForeground(new Color(17, 24, 39));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        cardPanel.add(titleLabel);
        cardPanel.add(Box.createVerticalStrut(14));

        cardPanel.add(createFieldLabel("Email"));
        cardPanel.add(Box.createVerticalStrut(10));

        accountField = new LoginFrame.PromptTextField("Enter email prefix or full email");
        accountField.setFont(new Font("SansSerif", Font.PLAIN, 16));
        accountField.setBorder(new EmptyBorder(0, 0, 0, 0));

        domainCombo = new JComboBox<>(new String[]{"@qmul.ac.uk", "@bupt.edu.cn"});
        domainCombo.setFont(new Font("SansSerif", Font.PLAIN, 14));
        domainCombo.setPreferredSize(new Dimension(132, LoginFrame.FIELD_HEIGHT - 10));
        domainCombo.setMaximumSize(new Dimension(132, LoginFrame.FIELD_HEIGHT - 10));
        domainCombo.addActionListener(e -> updateDetectedRole());

        JPanel accountWrapper = new JPanel(new BorderLayout(8, 0));
        accountWrapper.setBackground(Color.WHITE);
        accountWrapper.setMaximumSize(new Dimension(LoginFrame.CONTENT_WIDTH, LoginFrame.FIELD_HEIGHT));
        accountWrapper.setPreferredSize(new Dimension(LoginFrame.CONTENT_WIDTH, LoginFrame.FIELD_HEIGHT));
        accountWrapper.setMinimumSize(new Dimension(LoginFrame.CONTENT_WIDTH, LoginFrame.FIELD_HEIGHT));
        accountWrapper.setAlignmentX(Component.CENTER_ALIGNMENT);
        accountWrapper.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                new LoginFrame.RoundedBorder(16, new Color(220, 224, 230), 1),
                new EmptyBorder(0, 16, 0, 16)
        ));
        accountWrapper.add(accountField, BorderLayout.CENTER);
        accountWrapper.add(domainCombo, BorderLayout.EAST);

        cardPanel.add(accountWrapper);
        cardPanel.add(Box.createVerticalStrut(12));

        JPanel detectedRolePanel = new JPanel();
        detectedRolePanel.setOpaque(false);
        detectedRolePanel.setLayout(new BoxLayout(detectedRolePanel, BoxLayout.X_AXIS));
        detectedRolePanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        detectedRolePanel.setMaximumSize(new Dimension(LoginFrame.CONTENT_WIDTH, 34));

        JLabel detectedLabel = new JLabel("Detected role:");
        detectedLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        detectedLabel.setForeground(new Color(71, 85, 105));

        taChip = createRoleChip("TA");
        taChipLabel = (JLabel) taChip.getComponent(0);
        moChip = createRoleChip("MO");
        moChipLabel = (JLabel) moChip.getComponent(0);

        detectedRolePanel.add(detectedLabel);
        detectedRolePanel.add(Box.createHorizontalStrut(10));
        detectedRolePanel.add(taChip);
        detectedRolePanel.add(Box.createHorizontalStrut(8));
        detectedRolePanel.add(moChip);

        helperLabel = new JLabel("Enter your email prefix or full email to detect your role automatically.");
        helperLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));
        helperLabel.setForeground(new Color(100, 116, 139));
        helperLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        helperLabel.setMaximumSize(new Dimension(LoginFrame.CONTENT_WIDTH, 38));

        cardPanel.add(detectedRolePanel);
        cardPanel.add(Box.createVerticalStrut(6));
        cardPanel.add(helperLabel);
        cardPanel.add(Box.createVerticalStrut(16));

        passwordField = new JPasswordField();
        cardPanel.add(createFieldLabel("Password"));
        cardPanel.add(Box.createVerticalStrut(10));
        cardPanel.add(LoginFrame.wrapTextField(passwordField));
        cardPanel.add(Box.createVerticalStrut(12));

        confirmField = new JPasswordField();
        cardPanel.add(createFieldLabel("Confirm Password"));
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

        registerBtn = new JButton("Register");
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

        accountField.getDocument().addDocumentListener(new LoginFrame.SimpleDocumentListener(this::updateDetectedRole));
        getRootPane().setDefaultButton(registerBtn);
        updateDetectedRole();
        return LoginFrame.createFormSectionWrapper(cardPanel);
    }

    private JPanel createRoleChip(String text) {
        JPanel chip = new LoginFrame.RoundedPanel(16, new Color(241, 245, 249));
        chip.setLayout(new BorderLayout());
        chip.setBorder(new EmptyBorder(6, 14, 6, 14));
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 12));
        label.setForeground(new Color(100, 116, 139));
        chip.add(label, BorderLayout.CENTER);
        return chip;
    }

    private JLabel createFieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 16));
        label.setForeground(new Color(17, 24, 39));
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        return label;
    }

    private void updateDetectedRole() {
        String account = AccountRules.buildAccount(accountField.getText(), (String) domainCombo.getSelectedItem());
        AccountRules.DetectedRole detectedRole = AccountRules.detectRole(account);

        setChipState(taChip, taChipLabel, detectedRole == AccountRules.DetectedRole.TA);
        setChipState(moChip, moChipLabel, detectedRole == AccountRules.DetectedRole.MO);

        switch (detectedRole) {
            case UNKNOWN -> setHelperMessage(
                    "Enter your email prefix or full email to detect your role automatically.",
                    new Color(100, 116, 139)
            );
            case INVALID -> setHelperMessage(
                    "Please enter a valid email address.",
                    new Color(220, 38, 38)
            );
            case ADMIN -> setHelperMessage(
                    "Admin accounts cannot be registered here.",
                    new Color(220, 38, 38)
            );
            case TA -> {
                setHelperMessage("Detected role: TA", new Color(37, 99, 235));
            }
            case MO -> {
                setHelperMessage(
                        "Detected role: MO. Staff accounts require administrator approval before login.",
                        new Color(37, 99, 235)
                );
            }
        }
    }

    private void setChipState(JPanel chip, JLabel label, boolean active) {
        Color bg = active ? new Color(37, 99, 235) : new Color(241, 245, 249);
        Color fg = active ? Color.WHITE : new Color(100, 116, 139);
        if (chip instanceof LoginFrame.RoundedPanel roundedPanel) {
            roundedPanel.setBackgroundColor(bg);
        }
        label.setForeground(fg);
        chip.repaint();
    }

    private void setHelperMessage(String text, Color color) {
        helperLabel.setText(text);
        helperLabel.setForeground(color);
    }

    private void handleRegister() {
        String account = AccountRules.buildAccount(accountField.getText(), (String) domainCombo.getSelectedItem());
        String pwd = new String(passwordField.getPassword());
        String confirm = new String(confirmField.getPassword());
        AccountRules.DetectedRole detectedRole = AccountRules.detectRole(account);

        if (detectedRole == AccountRules.DetectedRole.UNKNOWN || detectedRole == AccountRules.DetectedRole.INVALID) {
            showWarning("Please enter a valid email address.");
            return;
        }
        if (detectedRole == AccountRules.DetectedRole.ADMIN) {
            showWarning("Admin accounts cannot be registered here.");
            return;
        }
        if (pwd.isEmpty() || confirm.isEmpty()) {
            showWarning("All fields are required.");
            return;
        }
        if (!pwd.equals(confirm)) {
            showWarning("Passwords do not match.");
            return;
        }

        UserRole role = AccountRules.toUserRole(detectedRole);
        if (role == null) {
            showWarning("Please enter a valid email address.");
            return;
        }

        try {
            User user = authService.register(account, pwd, role);
            String message = "Registration Successful!\nStatus: " + user.getStatus();
            if (user.getRole() == UserRole.MO) {
                message += "\nYour staff account must be approved by an administrator before login.";
            }
            showInfo(message);
            new LoginFrame(account).setVisible(true);
            dispose();
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    @Override
    protected void onWindowMaximized() {
        System.out.println("Register window maximized");
    }

    @Override
    protected void onWindowRestored() {
        System.out.println("Register window restored");
    }
}
