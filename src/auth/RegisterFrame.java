package auth;

import common.entity.User;
import common.entity.UserRole;

import javax.swing.JButton;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.net.URL;

public class RegisterFrame extends JFrame {
    private final AuthService authService;
    private JTextField emailField;
    private JPasswordField passwordField;
    private JPasswordField confirmField;
    private JComboBox<UserRole> roleCombo;

    public RegisterFrame() {
        this.authService = new AuthService();
        initUI();
    }

    private void initUI() {
        setTitle("TA Recruitment System - Register");
        setSize(520, 360);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        Image bgImage = loadBackgroundImage();
        JPanel root = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (bgImage != null) {
                    g.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this);
                }
            }
        };
        root.add(buildFormPanel(), BorderLayout.CENTER);
        root.add(buildActionPanel(), BorderLayout.SOUTH);
        add(root);
    }

    private JPanel buildFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(javax.swing.BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel title = new JLabel("Create an account", SwingConstants.CENTER);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(title, gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 1;
        gbc.gridx = 0;
        panel.add(new JLabel("Email:"), gbc);
        emailField = new JTextField(20);
        gbc.gridx = 1;
        panel.add(emailField, gbc);

        gbc.gridy = 2;
        gbc.gridx = 0;
        panel.add(new JLabel("Password:"), gbc);
        passwordField = new JPasswordField(20);
        gbc.gridx = 1;
        panel.add(passwordField, gbc);

        gbc.gridy = 3;
        gbc.gridx = 0;
        panel.add(new JLabel("Confirm Password:"), gbc);
        confirmField = new JPasswordField(20);
        gbc.gridx = 1;
        panel.add(confirmField, gbc);

        gbc.gridy = 4;
        gbc.gridx = 0;
        panel.add(new JLabel("Role:"), gbc);
        roleCombo = new JComboBox<>(UserRole.values());
        roleCombo.setSelectedItem(UserRole.TA);
        gbc.gridx = 1;
        panel.add(roleCombo, gbc);

        return panel;
    }

    private JPanel buildActionPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 18, 12));
        panel.setOpaque(false);
        JButton registerButton = new JButton("Register");
        JButton backButton = new JButton("Back to Login");

        registerButton.addActionListener(e -> handleRegister());
        backButton.addActionListener(e -> {
            new LoginFrame().setVisible(true);
            dispose();
        });

        panel.add(registerButton);
        panel.add(backButton);
        getRootPane().setDefaultButton(registerButton);
        return panel;
    }

    private void handleRegister() {
        String email = emailField.getText().trim();
        String password = new String(passwordField.getPassword());
        String confirm = new String(confirmField.getPassword());
        UserRole role = (UserRole) roleCombo.getSelectedItem();

        if (email.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!password.equals(confirm)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            User user = authService.register(email, password, role);
            JOptionPane.showMessageDialog(
                this,
                "Register success.\nEmail: " + user.getEmail() + "\nRole: " + user.getRole() + "\nStatus: " + user.getStatus(),
                "Success",
                JOptionPane.INFORMATION_MESSAGE
            );
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Register Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private Image loadBackgroundImage() {
        URL resource = getClass().getResource("/images/Background_1.jpg");
        if (resource != null) {
            return new ImageIcon(resource).getImage();
        }

        ImageIcon fileIcon = new ImageIcon("src/images/Background_1.jpg");
        if (fileIcon.getIconWidth() > 0) {
            return fileIcon.getImage();
        }
        return null;
    }
}
