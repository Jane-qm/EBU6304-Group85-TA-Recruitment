package ui.admin;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import modules.cv.CVInfo;
import modules.cv.CVService;
import modules.profile.TAProfile;
import modules.profile.TAProfileService;
import modules.user.AccountStatus;
import modules.user.User;
import modules.user.UserRole;
import modules.user.UserService;

/**
 * TA Management Panel for Admin
 * All buttons: white background, black text, black border
 * TA can only be registered via registration page, not added/imported here
 */
public class TAManagementPanel extends JPanel {
    private final UserService userService = new UserService();
    private final TAProfileService profileService = new TAProfileService();
    private final CVService cvService = new CVService();
    private JTable table;
    private DefaultTableModel tableModel;
    private Runnable refreshCallback;
    private List<User> taUsers;

    public TAManagementPanel(Runnable refreshCallback) {
        this.refreshCallback = refreshCallback;
        setLayout(new BorderLayout());
        setBackground(new Color(248, 250, 252));
        setBorder(new EmptyBorder(20, 20, 20, 20));
        initUI();
        loadData();
    }

    private void initUI() {
        JLabel titleLabel = new JLabel("TA Management");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        add(titleLabel, BorderLayout.NORTH);

        // Table
        String[] columns = {"Name", "Email", "Status", "View Profile", "View CV", "Status Action", "Reset Password"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 3 || column == 4 || column == 5 || column == 6;
            }
        };

        table = new JTable(tableModel);
        table.setRowHeight(45);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));

        // Set custom renderer and editor for button columns
        for (int col = 3; col <= 6; col++) {
            table.getColumnModel().getColumn(col).setCellRenderer(new ButtonRenderer());
            table.getColumnModel().getColumn(col).setCellEditor(new ButtonEditor());
        }

        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    private JButton createButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.PLAIN, 12));
        button.setBackground(Color.WHITE);
        button.setForeground(Color.BLACK);
        button.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private void loadData() {
        tableModel.setRowCount(0);
        taUsers = userService.listAllUsers().stream()
                .filter(u -> u.getRole() == UserRole.TA)
                .toList();

        for (User user : taUsers) {
            TAProfile profile = profileService.getProfileByTaId(user.getUserId());
            String name = (profile != null && profile.getFullName() != null && !profile.getFullName().isEmpty())
                    ? profile.getFullName() : "Not updated";
            String email = user.getEmail();
            String status = getStatusText(user.getStatus());

            tableModel.addRow(new Object[]{
                    name, email, status,
                    "View Profile", "View CV", "Status", "Reset Pwd"
            });
        }
    }

    // Button Renderer
    private class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
            setFocusPainted(false);
            setFont(new Font("SansSerif", Font.PLAIN, 11));
            setBackground(Color.WHITE);
            setForeground(Color.BLACK);
            setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            User user = taUsers.get(row);

            if (column == 3) { // View Profile
                setText("View Profile");
                setForeground(Color.BLACK);
            } else if (column == 4) { // View CV
                setText("View CV");
                setForeground(Color.BLACK);
            } else if (column == 5) { // Status Action
                if (user.getStatus() == AccountStatus.ACTIVE) {
                    setText("Disable");
                    setForeground(new Color(239, 68, 68));
                } else {
                    setText("Activate");
                    setForeground(new Color(59, 130, 246));
                }
            } else if (column == 6) { // Reset Password
                setText("Reset Pwd");
                setForeground(Color.BLACK);
            }

            return this;
        }
    }

    // Button Editor
    private class ButtonEditor extends AbstractCellEditor implements TableCellEditor, ActionListener {
        private JButton button;
        private int currentRow;
        private int currentColumn;

        public ButtonEditor() {
            button = new JButton();
            button.setFont(new Font("SansSerif", Font.PLAIN, 11));
            button.setBackground(Color.WHITE);
            button.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
            button.setFocusPainted(false);
            button.addActionListener(this);
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            currentRow = row;
            currentColumn = column;

            User user = taUsers.get(row);

            if (column == 3) { // View Profile
                button.setText("View Profile");
                button.setForeground(Color.BLACK);
            } else if (column == 4) { // View CV
                button.setText("View CV");
                button.setForeground(Color.BLACK);
            } else if (column == 5) { // Status Action
                if (user.getStatus() == AccountStatus.ACTIVE) {
                    button.setText("Disable");
                    button.setForeground(new Color(239, 68, 68));
                } else {
                    button.setText("Activate");
                    button.setForeground(new Color(59, 130, 246));
                }
            } else if (column == 6) { // Reset Password
                button.setText("Reset Pwd");
                button.setForeground(Color.BLACK);
            }

            return button;
        }

        @Override
        public Object getCellEditorValue() {
            return button.getText();
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            User user = taUsers.get(currentRow);

            if (currentColumn == 3) { // View Profile
                viewProfile(user);
            } else if (currentColumn == 4) { // View CV
                viewCV(user);
            } else if (currentColumn == 5) { // Status Action
                if (user.getStatus() == AccountStatus.ACTIVE) {
                    disableTA(user);
                } else {
                    activateTA(user);
                }
            } else if (currentColumn == 6) { // Reset Password
                resetPassword(user);
            }

            fireEditingStopped();
        }
    }

    private void viewProfile(User user) {
        TAProfile profile = profileService.getProfileByTaId(user.getUserId());
        if (profile == null) {
            JOptionPane.showMessageDialog(this,
                    "Profile not completed yet.\nEmail: " + user.getEmail(),
                    "TA Profile", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("=== TA Profile ===\n\n");
        sb.append("Name: ").append(profile.getFullName()).append("\n");
        sb.append("Chinese Name: ").append(profile.getChineseName() != null ? profile.getChineseName() : "N/A").append("\n");
        sb.append("Email: ").append(profile.getEmail()).append("\n");
        sb.append("Student ID: ").append(profile.getStudentId() != null ? profile.getStudentId() : "N/A").append("\n");
        sb.append("Phone: ").append(profile.getPhone() != null ? profile.getPhone() : "N/A").append("\n");
        sb.append("Gender: ").append(profile.getGender() != null ? profile.getGender().getEnglishName() : "N/A").append("\n");
        sb.append("School: ").append(profile.getSchool() != null ? profile.getSchool() : "N/A").append("\n");
        sb.append("Supervisor: ").append(profile.getSupervisor() != null ? profile.getSupervisor() : "N/A").append("\n");
        sb.append("Major: ").append(profile.getMajor() != null ? profile.getMajor() : "N/A").append("\n");
        sb.append("Student Type: ").append(profile.getStudentType() != null ? profile.getStudentType().getEnglishName() : "N/A").append("\n");
        sb.append("Current Year: ").append(profile.getCurrentYear() != null ? profile.getCurrentYear().getEnglishName() : "N/A").append("\n");
        sb.append("Campus: ").append(profile.getCampus() != null ? profile.getCampus().getChineseName() : "N/A").append("\n");
        sb.append("Available Hours: ").append(profile.getAvailableWorkingHours()).append(" hours/week\n");
        sb.append("\nSkills: ").append(profile.getSkillTags() != null && !profile.getSkillTags().isEmpty()
                ? String.join(", ", profile.getSkillTags()) : "None").append("\n");
        sb.append("\nPrevious Experience:\n").append(profile.getPreviousExperience() != null ? profile.getPreviousExperience() : "None");

        JOptionPane.showMessageDialog(this, sb.toString(),
                "TA Profile - " + user.getEmail(), JOptionPane.INFORMATION_MESSAGE);
    }

    private void viewCV(User user) {
        cvService.refreshCVs(user.getUserId());

        CVInfo defaultCV = cvService.getDefaultCV(user.getUserId());
        if (defaultCV == null) {
            JOptionPane.showMessageDialog(this,
                    "No CV uploaded yet.\nEmail: " + user.getEmail(),
                    "CV Not Found", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            byte[] fileData = cvService.downloadCV(user.getUserId(), defaultCV.getCvId());
            if (fileData == null || fileData.length == 0) {
                JOptionPane.showMessageDialog(this, "Failed to read CV file.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String extension = defaultCV.getOriginalFileName() != null && defaultCV.getOriginalFileName().contains(".")
                    ? defaultCV.getOriginalFileName().substring(defaultCV.getOriginalFileName().lastIndexOf('.') + 1)
                    : "pdf";

            java.io.File tempFile = java.io.File.createTempFile("cv_", "." + extension);
            tempFile.deleteOnExit();

            try (java.io.FileOutputStream fos = new java.io.FileOutputStream(tempFile)) {
                fos.write(fileData);
            }

            if (java.awt.Desktop.isDesktopSupported()) {
                java.awt.Desktop.getDesktop().open(tempFile);
            } else {
                JOptionPane.showMessageDialog(this, "CV file saved to: " + tempFile.getAbsolutePath(),
                        "CV File", JOptionPane.INFORMATION_MESSAGE);
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to open CV: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void disableTA(User user) {
        userService.disableAccount(user.getEmail());
        JOptionPane.showMessageDialog(this, "TA disabled: " + user.getEmail());
        loadData();
        if (refreshCallback != null) refreshCallback.run();
    }

    private void activateTA(User user) {
        userService.updateAccountStatus(user.getEmail(), AccountStatus.ACTIVE);
        JOptionPane.showMessageDialog(this, "TA activated: " + user.getEmail());
        loadData();
        if (refreshCallback != null) refreshCallback.run();
    }

    private void resetPassword(User user) {
        userService.resetPasswordByAdmin(user.getEmail(), "000000");
        JOptionPane.showMessageDialog(this, "Password reset to 000000\nEmail: " + user.getEmail());
    }

    private String getStatusText(AccountStatus status) {
        if (status == AccountStatus.ACTIVE) return "Active";
        if (status == AccountStatus.PENDING) return "Inactive";
        return "Disabled";
    }

    public void refresh() {
        loadData();
    }
}