package ui.auth;

import infrastructure.security.PermissionService;
import modules.auth.AuthService;
import modules.user.AccountStatus;
import modules.user.User;
import modules.user.UserRole;
import modules.user.UserService;
import ui.admin.AdminHomeFrame;
import ui.common.BaseFrame;
import ui.mo.MODashboardFrame;
import ui.ta.TAMainFrame;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.AbstractBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class LoginFrame extends BaseFrame {

    static final int FRAME_WIDTH = 1200;
    static final int FRAME_HEIGHT = 750;
    static final int SHELL_WIDTH = 1120;
    static final int SHELL_HEIGHT = 640;
    static final int BRAND_WIDTH = 560;
    static final int FORM_WIDTH = 560;
    static final int CONTENT_WIDTH = 380;
    static final int FIELD_HEIGHT = 56;

    private JTextField emailPrefixField;
    private JComboBox<String> domainCombo;
    private JPasswordField passwordField;
    private JCheckBox rememberMeBox;
    private JCheckBox showPasswordBox;
    private final AuthService authService;
    private final String prefilledEmail;

    public LoginFrame() {
        this(null);
    }

    public LoginFrame(String prefilledEmail) {
        super("TA Recruitment System - Login", FRAME_WIDTH, FRAME_HEIGHT);
        this.authService = new AuthService();
        this.prefilledEmail = prefilledEmail == null ? "" : prefilledEmail.trim();
        initUI();
    }

    @Override
    protected void initUI() {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        JPanel rootPanel = new JPanel(new BorderLayout());
        rootPanel.setBackground(new Color(242, 245, 250));

        JPanel shellPanel = new JPanel(new BorderLayout());
        shellPanel.setOpaque(false);
        shellPanel.setPreferredSize(new Dimension(SHELL_WIDTH, SHELL_HEIGHT));
        shellPanel.setMinimumSize(new Dimension(SHELL_WIDTH, SHELL_HEIGHT));
        shellPanel.setMaximumSize(new Dimension(SHELL_WIDTH, SHELL_HEIGHT));

        shellPanel.add(createBrandPanel(), BorderLayout.WEST);
        shellPanel.add(createFormSection(), BorderLayout.CENTER);

        rootPanel.add(shellPanel, BorderLayout.CENTER);
        setContentPane(rootPanel);
    }

    private JComponent createFormSection() {
        JPanel formSection = new JPanel(new GridBagLayout());
        formSection.setOpaque(true);
        formSection.setBackground(Color.WHITE);
        formSection.setPreferredSize(new Dimension(FORM_WIDTH, SHELL_HEIGHT));

        JPanel cardPanel = new JPanel();
        cardPanel.setOpaque(false);
        cardPanel.setLayout(new BoxLayout(cardPanel, BoxLayout.Y_AXIS));
        cardPanel.setBorder(new EmptyBorder(24, 52, 20, 52));

        JLabel titleLabel = new JLabel("Welcome Back");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 30));
        titleLabel.setForeground(new Color(17, 24, 39));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Sign in to your account to continue");
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));
        subtitleLabel.setForeground(new Color(107, 114, 128));
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        cardPanel.add(titleLabel);
        cardPanel.add(Box.createVerticalStrut(10));
        cardPanel.add(subtitleLabel);
        cardPanel.add(Box.createVerticalStrut(24));

        cardPanel.add(createFieldLabel("University Email"));
        cardPanel.add(Box.createVerticalStrut(10));

        emailPrefixField = new PromptTextField("Enter email prefix");
        emailPrefixField.setFont(new Font("SansSerif", Font.PLAIN, 16));
        emailPrefixField.setBorder(new EmptyBorder(0, 0, 0, 0));

        domainCombo = new JComboBox<>(new String[]{"@qmul.ac.uk", "@bupt.edu.cn"});
        domainCombo.setFont(new Font("SansSerif", Font.PLAIN, 14));
        domainCombo.setPreferredSize(new Dimension(132, FIELD_HEIGHT - 10));
        domainCombo.setMaximumSize(new Dimension(132, FIELD_HEIGHT - 10));

        JPanel emailWrapper = new JPanel(new BorderLayout(8, 0));
        emailWrapper.setBackground(Color.WHITE);
        emailWrapper.setMaximumSize(new Dimension(CONTENT_WIDTH, FIELD_HEIGHT));
        emailWrapper.setPreferredSize(new Dimension(CONTENT_WIDTH, FIELD_HEIGHT));
        emailWrapper.setMinimumSize(new Dimension(CONTENT_WIDTH, FIELD_HEIGHT));
        emailWrapper.setAlignmentX(Component.CENTER_ALIGNMENT);
        emailWrapper.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(16, new Color(220, 224, 230), 1),
                new EmptyBorder(0, 16, 0, 16)
        ));
        emailWrapper.add(emailPrefixField, BorderLayout.CENTER);
        emailWrapper.add(domainCombo, BorderLayout.EAST);

        cardPanel.add(emailWrapper);
        cardPanel.add(Box.createVerticalStrut(20));

        cardPanel.add(createFieldLabel("Password"));
        cardPanel.add(Box.createVerticalStrut(10));

        passwordField = new JPasswordField();
        cardPanel.add(wrapTextField(passwordField));
        cardPanel.add(Box.createVerticalStrut(10));

        showPasswordBox = new JCheckBox("Show password");
        showPasswordBox.setFont(new Font("SansSerif", Font.PLAIN, 14));
        showPasswordBox.setForeground(new Color(55, 65, 81));
        showPasswordBox.setOpaque(false);
        showPasswordBox.setFocusPainted(false);
        showPasswordBox.setAlignmentX(Component.CENTER_ALIGNMENT);
        char passwordEchoChar = passwordField.getEchoChar();
        showPasswordBox.addActionListener(e ->
                passwordField.setEchoChar(showPasswordBox.isSelected() ? (char) 0 : passwordEchoChar)
        );
        cardPanel.add(showPasswordBox);
        cardPanel.add(Box.createVerticalStrut(16));

        JPanel optionsPanel = new JPanel(new BorderLayout());
        optionsPanel.setOpaque(false);
        optionsPanel.setMaximumSize(new Dimension(CONTENT_WIDTH, 36));
        optionsPanel.setPreferredSize(new Dimension(CONTENT_WIDTH, 36));
        optionsPanel.setMinimumSize(new Dimension(CONTENT_WIDTH, 36));
        optionsPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        rememberMeBox = new JCheckBox("Remember Me");
        rememberMeBox.setFont(new Font("SansSerif", Font.PLAIN, 14));
        rememberMeBox.setForeground(new Color(55, 65, 81));
        rememberMeBox.setOpaque(false);
        rememberMeBox.setFocusPainted(false);
        rememberMeBox.setMargin(new Insets(0, 0, 0, 0));

        JButton forgotPwdButton = new JButton("Forgot Password?");
        forgotPwdButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        forgotPwdButton.setForeground(new Color(37, 99, 235));
        forgotPwdButton.setContentAreaFilled(false);
        forgotPwdButton.setBorderPainted(false);
        forgotPwdButton.setFocusPainted(false);
        forgotPwdButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        forgotPwdButton.addActionListener(e -> handlePasswordRecovery());

        optionsPanel.add(rememberMeBox, BorderLayout.WEST);
        optionsPanel.add(forgotPwdButton, BorderLayout.EAST);
        cardPanel.add(optionsPanel);
        cardPanel.add(Box.createVerticalStrut(18));

        JButton loginButton = new JButton("Sign In");
        loginButton.setFont(new Font("SansSerif", Font.BOLD, 22));
        loginButton.setForeground(Color.WHITE);
        loginButton.setBackground(new Color(37, 99, 235));
        loginButton.setFocusPainted(false);
        loginButton.setBorderPainted(false);
        loginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginButton.addActionListener(new LoginAction());

        RoundedPanel loginButtonPanel = new RoundedPanel(16, new Color(37, 99, 235));
        loginButtonPanel.setLayout(new BorderLayout());
        loginButtonPanel.setMaximumSize(new Dimension(CONTENT_WIDTH, FIELD_HEIGHT));
        loginButtonPanel.setPreferredSize(new Dimension(CONTENT_WIDTH, FIELD_HEIGHT));
        loginButtonPanel.setMinimumSize(new Dimension(CONTENT_WIDTH, FIELD_HEIGHT));
        loginButtonPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginButtonPanel.add(loginButton, BorderLayout.CENTER);

        cardPanel.add(loginButtonPanel);
        cardPanel.add(Box.createVerticalStrut(14));

        JPanel registerPanel = new JPanel();
        registerPanel.setOpaque(false);
        registerPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel noAccountLabel = new JLabel("Don't have an account? ");
        noAccountLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        noAccountLabel.setForeground(new Color(107, 114, 128));

        JButton registerButton = new JButton("Register here");
        registerButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        registerButton.setForeground(new Color(37, 99, 235));
        registerButton.setContentAreaFilled(false);
        registerButton.setBorderPainted(false);
        registerButton.setFocusPainted(false);
        registerButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        registerButton.addActionListener(e -> {
            new RegisterFrame().setVisible(true);
            dispose();
        });

        registerPanel.add(noAccountLabel);
        registerPanel.add(registerButton);
        cardPanel.add(registerPanel);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.NORTH;
        formSection.add(cardPanel, gbc);

        getRootPane().setDefaultButton(loginButton);
        applyPrefilledEmail();
        loadRememberedEmail();
        return formSection;
    }

    static JComponent createBrandPanel() {
        BrandPanel brandPanel = new BrandPanel();
        brandPanel.setPreferredSize(new Dimension(BRAND_WIDTH, SHELL_HEIGHT));
        brandPanel.setMinimumSize(new Dimension(BRAND_WIDTH, SHELL_HEIGHT));
        brandPanel.setMaximumSize(new Dimension(BRAND_WIDTH, SHELL_HEIGHT));
        return brandPanel;
    }

    static JPanel createFormSectionWrapper(JComponent content) {
        JPanel formSection = new JPanel(new GridBagLayout());
        formSection.setOpaque(true);
        formSection.setBackground(Color.WHITE);
        formSection.setPreferredSize(new Dimension(FORM_WIDTH, SHELL_HEIGHT));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.NORTH;
        formSection.add(content, gbc);
        return formSection;
    }

    static JLabel createFieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 16));
        label.setForeground(new Color(17, 24, 39));
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        return label;
    }

    static JPanel wrapTextField(JTextField field) {
        field.setFont(new Font("SansSerif", Font.PLAIN, 16));
        field.setBorder(new EmptyBorder(0, 0, 0, 0));

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(Color.WHITE);
        wrapper.setMaximumSize(new Dimension(CONTENT_WIDTH, FIELD_HEIGHT));
        wrapper.setPreferredSize(new Dimension(CONTENT_WIDTH, FIELD_HEIGHT));
        wrapper.setMinimumSize(new Dimension(CONTENT_WIDTH, FIELD_HEIGHT));
        wrapper.setAlignmentX(Component.CENTER_ALIGNMENT);
        wrapper.setBorder(BorderFactory.createCompoundBorder(
                new RoundedBorder(16, new Color(220, 224, 230), 1),
                new EmptyBorder(0, 16, 0, 16)
        ));
        wrapper.add(field, BorderLayout.CENTER);
        return wrapper;
    }

    private void loadRememberedEmail() {
        // Keep the existing behavior: the checkbox is visual only for now.
    }

    private void applyPrefilledEmail() {
        if (prefilledEmail.isEmpty()) {
            return;
        }

        if (!prefilledEmail.contains("@")) {
            emailPrefixField.setText(prefilledEmail);
            return;
        }

        String[] accountParts = prefilledEmail.split("@", 2);
        if (accountParts.length != 2) {
            emailPrefixField.setText(prefilledEmail);
            return;
        }

        String suffix = "@" + accountParts[1].toLowerCase();
        if ("@qmul.ac.uk".equals(suffix) || "@bupt.edu.cn".equals(suffix)) {
            emailPrefixField.setText(accountParts[0]);
            domainCombo.setSelectedItem(suffix);
            return;
        }

        emailPrefixField.setText(prefilledEmail);
    }

    private String getFullEmail() {
        String prefix = emailPrefixField.getText().trim();
        String domain = (String) domainCombo.getSelectedItem();
        return prefix + domain;
    }

    private class LoginAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String email = getFullEmail();
            String password = new String(passwordField.getPassword());

            if (emailPrefixField.getText().trim().isEmpty() || password.isEmpty()) {
                showWarning("Please enter both email and password");
                return;
            }

            try {
                User user = authService.login(email, password);

                if (user != null) {
                    if (user.getStatus() == AccountStatus.DISABLED) {
                        showError(authService.getAccountStatusMessage(user));
                        return;
                    }

                    if (user.getRole() == UserRole.ADMIN) {
                        UserService userService = UserService.getInstance();
                        if (!userService.isStrictAdmin(user)) {
                            showError("Only active super admin account admin@test.com can access Admin Portal.");
                            return;
                        }
                        if (user.isMustChangePassword()) {
                            String newPassword = promptMandatoryPasswordChange(user.getEmail());
                            if (newPassword == null) {
                                return;
                            }
                            authService.resetPassword(user.getEmail(), newPassword);
                            user = authService.login(user.getEmail(), newPassword);
                            if (user == null) {
                                showError("Password updated, but automatic re-login failed. Please log in again.");
                                return;
                            }
                        }
                    }

                    if (PermissionService.hasAccess(user.getRole(), UserRole.ADMIN)) {
                        new AdminHomeFrame(user).setVisible(true);
                        dispose();
                    } else if (PermissionService.hasAccess(user.getRole(), UserRole.MO)) {
                        new MODashboardFrame(user).setVisible(true);
                        dispose();
                    } else if (PermissionService.hasAccess(user.getRole(), UserRole.TA)) {
                        new TAMainFrame(user).setVisible(true);
                        dispose();
                    } else {
                        showInfo("Routing to " + user.getRole() + " Dashboard...");
                        dispose();
                    }

                } else {
                    showError("Invalid email or password");
                }
            } catch (Exception ex) {
                showError("System Error: " + ex.getMessage());
            }
        }
    }

    private String promptMandatoryPasswordChange(String email) {
        JPasswordField pwd1 = new JPasswordField();
        JPasswordField pwd2 = new JPasswordField();
        Object[] content = {
                "First login security setup for " + email,
                "Please set a new password:",
                pwd1,
                "Confirm new password:",
                pwd2
        };
        int option = JOptionPane.showConfirmDialog(
                this,
                content,
                "First Login - Change Password",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.WARNING_MESSAGE
        );
        if (option != JOptionPane.OK_OPTION) {
            showWarning("You must change the bootstrap password before entering Admin Portal.");
            return null;
        }
        String p1 = new String(pwd1.getPassword());
        String p2 = new String(pwd2.getPassword());
        if (p1.isBlank() || p2.isBlank()) {
            showWarning("Password must not be empty.");
            return null;
        }
        if (!p1.equals(p2)) {
            showWarning("Passwords do not match.");
            return null;
        }
        if (p1.length() < 6) {
            showWarning("Password must be at least 6 characters.");
            return null;
        }
        return p1;
    }

    private void handlePasswordRecovery() {
        String fullEmail = getFullEmail();
        if (emailPrefixField.getText().trim().isEmpty()) {
            fullEmail = showInput("Enter your registered email (full address):", "Password Recovery");
            if (fullEmail == null || fullEmail.trim().isEmpty()) {
                return;
            }
        } else {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Reset password for: " + fullEmail + " ?",
                    "Confirm Email",
                    JOptionPane.YES_NO_OPTION);
            if (confirm != JOptionPane.YES_OPTION) {
                return;
            }
        }

        try {
            if (!authService.checkEmailExists(fullEmail.trim())) {
                showError("Email is not registered.");
                return;
            }
            User targetUser = UserService.getInstance().findByEmail(fullEmail.trim());
            if (targetUser != null && targetUser.getRole() == UserRole.ADMIN) {
                showError("Admin password cannot be reset via self-service recovery.\n"
                        + "Please use secure bootstrap/login flow.");
                return;
            }

            JPasswordField newPasswordField = new JPasswordField();
            int option = JOptionPane.showConfirmDialog(
                    this,
                    new Object[]{"Enter your new password:", newPasswordField},
                    "Reset Password",
                    JOptionPane.OK_CANCEL_OPTION
            );
            if (option != JOptionPane.OK_OPTION) {
                return;
            }

            authService.resetPassword(fullEmail.trim(), new String(newPasswordField.getPassword()));
            showInfo("Password has been reset successfully.");
        } catch (IllegalArgumentException ex) {
            showWarning(ex.getMessage());
        } catch (Exception ex) {
            showError("System Error: " + ex.getMessage());
        }
    }

    static class RoundedPanel extends JPanel {
        private final int radius;
        private Color backgroundColor;

        RoundedPanel(int radius, Color backgroundColor) {
            this.radius = radius;
            this.backgroundColor = backgroundColor;
            setOpaque(false);
        }

        void setBackgroundColor(Color backgroundColor) {
            this.backgroundColor = backgroundColor;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(backgroundColor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    static class RoundedBorder extends AbstractBorder {
        private final int radius;
        private final Color color;
        private final int thickness;

        RoundedBorder(int radius, Color color, int thickness) {
            this.radius = radius;
            this.color = color;
            this.thickness = thickness;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            if (thickness > 0) {
                g2.setStroke(new BasicStroke(thickness));
            }
            g2.drawRoundRect(
                    x + thickness / 2,
                    y + thickness / 2,
                    width - thickness,
                    height - thickness,
                    radius,
                    radius
            );
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(radius / 2, radius / 2, radius / 2, radius / 2);
        }

        @Override
        public Insets getBorderInsets(Component c, Insets insets) {
            insets.left = radius / 2;
            insets.right = radius / 2;
            insets.top = radius / 2;
            insets.bottom = radius / 2;
            return insets;
        }
    }

    static class BrandPanel extends JPanel {
        private static final Color OVERLAY_TOP = new Color(17, 57, 156, 150);
        private static final Color OVERLAY_BOTTOM = new Color(37, 99, 235, 120);
        private final BufferedImage backgroundImage;

        BrandPanel() {
            setOpaque(false);
            backgroundImage = loadBackgroundImage();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            paintCoverImage(g2);

            GradientPaint overlay = new GradientPaint(
                    0, 0, OVERLAY_TOP,
                    0, getHeight(), OVERLAY_BOTTOM
            );
            g2.setPaint(overlay);
            g2.fillRect(0, 0, getWidth(), getHeight());

            paintBrandContent(g2);
            g2.dispose();
        }

        private void paintCoverImage(Graphics2D g2) {
            if (backgroundImage == null) {
                g2.setColor(new Color(30, 64, 175));
                g2.fillRect(0, 0, getWidth(), getHeight());
                return;
            }

            double scale = Math.max(
                    (double) getWidth() / backgroundImage.getWidth(),
                    (double) getHeight() / backgroundImage.getHeight()
            );
            int drawWidth = (int) Math.ceil(backgroundImage.getWidth() * scale);
            int drawHeight = (int) Math.ceil(backgroundImage.getHeight() * scale);
            int x = (getWidth() - drawWidth) / 2;
            int y = (getHeight() - drawHeight) / 2;
            Image scaled = backgroundImage.getScaledInstance(drawWidth, drawHeight, Image.SCALE_SMOOTH);
            g2.drawImage(scaled, x, y, null);
        }

        private void paintBrandContent(Graphics2D g2) {
            int paddingX = 42;

            g2.setColor(Color.WHITE);
            g2.setFont(new Font("SansSerif", Font.BOLD, 24));
            g2.drawString("TA Recruit", paddingX, 58);

            g2.setFont(new Font("SansSerif", Font.BOLD, 38));
            drawWrappedText(
                    g2,
                    "Professional TA Recruitment System",
                    paddingX,
                    getHeight() / 2 - 90,
                    getWidth() - 84,
                    50
            );

            g2.setFont(new Font("SansSerif", Font.PLAIN, 17));
            g2.setColor(new Color(255, 255, 255, 225));
            drawWrappedText(
                    g2,
                    "Streamline the teaching assistant recruitment process. Apply, review, and manage TA positions all in one place.",
                    paddingX,
                    getHeight() / 2 + 20,
                    getWidth() - 96,
                    30
            );

            g2.setFont(new Font("SansSerif", Font.PLAIN, 13));
            g2.drawString("(c) 2026 University TA Recruitment Platform", paddingX, getHeight() - 34);
        }

        private void drawWrappedText(Graphics2D g2, String text, int x, int startY, int maxWidth, int lineHeight) {
            String[] words = text.split(" ");
            StringBuilder line = new StringBuilder();
            int y = startY;
            for (String word : words) {
                String candidate = line.length() == 0 ? word : line + " " + word;
                if (g2.getFontMetrics().stringWidth(candidate) > maxWidth && line.length() > 0) {
                    g2.drawString(line.toString(), x, y);
                    line = new StringBuilder(word);
                    y += lineHeight;
                } else {
                    line = new StringBuilder(candidate);
                }
            }
            if (line.length() > 0) {
                g2.drawString(line.toString(), x, y);
            }
        }

        private BufferedImage loadBackgroundImage() {
            File file = new File("src/images/background.png");
            if (!file.exists()) {
                return null;
            }
            try {
                return ImageIO.read(file);
            } catch (IOException ex) {
                return null;
            }
        }
    }

    static class PromptTextField extends JTextField {
        private final String placeholder;

        PromptTextField(String placeholder) {
            this.placeholder = placeholder;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (!getText().isEmpty()) {
                return;
            }
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setColor(new Color(148, 163, 184));
            g2.setFont(getFont());
            Insets insets = getInsets();
            int y = (getHeight() - g2.getFontMetrics().getHeight()) / 2 + g2.getFontMetrics().getAscent();
            g2.drawString(placeholder, insets.left + 2, y);
            g2.dispose();
        }
    }

    static class SimpleDocumentListener implements DocumentListener {
        private final Runnable callback;

        SimpleDocumentListener(Runnable callback) {
            this.callback = callback;
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            SwingUtilities.invokeLater(callback);
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            SwingUtilities.invokeLater(callback);
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            SwingUtilities.invokeLater(callback);
        }
    }
}
