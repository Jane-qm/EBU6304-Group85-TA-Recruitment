package ui.admin;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.AbstractCellEditor;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import infrastructure.audit.AdminAuditLogger;
import modules.cv.CVInfo;
import modules.cv.CVService;
import modules.profile.TAProfile;
import modules.profile.TAProfileService;
import modules.user.AccountStatus;
import modules.user.User;
import modules.user.UserRole;
import modules.user.UserService;
import modules.auth.AuthService;
import ui.common.TableListActionStyle;
import ui.common.TableScrollUtil;
import ui.common.TaProfileViewer;

/**
 * TA Management Panel for Admin
 * Table actions: borderless bold text (blue / green / red by intent).
 * TA can only be registered via registration page, not added/imported here
 *
 * @version 1.1
 * @contributor Jiaze Wang
 * @update
 * - Added admin audit logging for TA account lifecycle actions
 */
public class TAManagementPanel extends JPanel {
    private final UserService userService = UserService.getInstance();
    private final TAProfileService profileService = new TAProfileService();
    private final CVService cvService = new CVService();
    private JTable table;
    private DefaultTableModel tableModel;
    private Runnable refreshCallback;
    private final String adminEmail;
    private List<User> allTaUsers;
    private List<User> taUsers;

    private JTextField searchField;
    private JComboBox<String> searchAttrCombo;

    public TAManagementPanel(Runnable refreshCallback) {
        this(refreshCallback, null);
    }

    public TAManagementPanel(Runnable refreshCallback, String adminEmail) {
        this.refreshCallback = refreshCallback;
        this.adminEmail = (adminEmail == null || adminEmail.isBlank()) ? "unknown" : adminEmail.trim();
        setLayout(new BorderLayout());
        setBackground(new Color(248, 250, 252));
        setBorder(new EmptyBorder(20, 20, 20, 20));
        initUI();
        loadData();
    }

    private void initUI() {
        JPanel north = new JPanel();
        north.setLayout(new BoxLayout(north, BoxLayout.Y_AXIS));
        north.setOpaque(false);

        JLabel titleLabel = new JLabel("TA Management");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        north.add(titleLabel);

        JPanel searchRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        searchRow.setOpaque(false);
        searchRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        searchRow.add(new JLabel("Search:"));
        searchField = new JTextField(20);
        searchField.setFont(new Font("SansSerif", Font.PLAIN, 14));
        searchAttrCombo = new JComboBox<>(new String[]{"All fields", "Name", "Email", "Status"});
        searchAttrCombo.setFont(new Font("SansSerif", Font.PLAIN, 14));
        searchRow.add(searchField);
        searchRow.add(searchAttrCombo);
        north.add(searchRow);

        add(north, BorderLayout.NORTH);

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
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 15));

        // Set custom renderer and editor for button columns
        for (int col = 3; col <= 6; col++) {
            table.getColumnModel().getColumn(col).setCellRenderer(new ButtonRenderer());
            table.getColumnModel().getColumn(col).setCellEditor(new ButtonEditor());
        }

        TableScrollUtil.ColumnSpec[] taCols = {
                TableScrollUtil.ColumnSpec.flex(88, 140),
                TableScrollUtil.ColumnSpec.flex(120, 250),
                TableScrollUtil.ColumnSpec.fixed(100),
                TableScrollUtil.ColumnSpec.fixed(108),
                TableScrollUtil.ColumnSpec.fixed(88),
                TableScrollUtil.ColumnSpec.fixed(148),
                TableScrollUtil.ColumnSpec.fixed(134),
        };

        JScrollPane taScroll = TableScrollUtil.wrapTable(table);
        TableScrollUtil.installResponsiveColumns(table, taScroll, taCols);
        add(taScroll, BorderLayout.CENTER);

        DocumentListener dl = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                applySearchFilter();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                applySearchFilter();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                applySearchFilter();
            }
        };
        searchField.getDocument().addDocumentListener(dl);
        searchAttrCombo.addActionListener(e -> applySearchFilter());
    }

    private JButton createButton(String text) {
        JButton button = new JButton(text);
        TableListActionStyle.applyToButton(button, text);
        button.setFont(new Font("SansSerif", Font.BOLD, 14));
        return button;
    }

    private void applySearchFilter() {
        String q = searchField.getText().trim().toLowerCase();
        String attrRaw = (String) searchAttrCombo.getSelectedItem();
        final String attr = attrRaw == null ? "All fields" : attrRaw;
        taUsers = allTaUsers.stream()
                .filter(u -> taMatchesSearch(u, q, attr))
                .toList();

        tableModel.setRowCount(0);
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

    private boolean taMatchesSearch(User user, String q, String attr) {
        if (q.isEmpty()) {
            return true;
        }
        TAProfile profile = profileService.getProfileByTaId(user.getUserId());
        String name = (profile != null && profile.getFullName() != null && !profile.getFullName().isEmpty())
                ? profile.getFullName() : "Not updated";
        String email = user.getEmail() != null ? user.getEmail() : "";
        String status = getStatusText(user.getStatus());
        return switch (attr) {
            case "Name" -> name.toLowerCase().contains(q);
            case "Email" -> email.toLowerCase().contains(q);
            case "Status" -> status.toLowerCase().contains(q);
            default -> name.toLowerCase().contains(q)
                    || email.toLowerCase().contains(q)
                    || status.toLowerCase().contains(q);
        };
    }

    private void loadData() {
        allTaUsers = userService.listAllUsers().stream()
                .filter(u -> u.getRole() == UserRole.TA)
                .toList();
        applySearchFilter();
    }

    // Button Renderer
    private class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            User user = taUsers.get(row);

            if (column == 3) { // View Profile
                setText("View Profile");
            } else if (column == 4) { // View CV
                setText("View CV");
            } else if (column == 5) { // Status Action
                if (user.getStatus() == AccountStatus.ACTIVE) {
                    setText("Disable");
                } else {
                    setText("Activate");
                }
            } else if (column == 6) { // Reset Password
                setText("Reset Pwd");
            }

            TableListActionStyle.applyToButton(this, getText());
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
            } else if (column == 4) { // View CV
                button.setText("View CV");
            } else if (column == 5) { // Status Action
                if (user.getStatus() == AccountStatus.ACTIVE) {
                    button.setText("Disable");
                } else {
                    button.setText("Activate");
                }
            } else if (column == 6) { // Reset Password
                button.setText("Reset Pwd");
            }

            TableListActionStyle.applyToButton(button, button.getText());
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
        TaProfileViewer.show(this, user, profile, null);
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
        AdminAuditLogger.log(adminEmail, "DISABLE_TA", auditTarget(user, "SUCCESS"));
        JOptionPane.showMessageDialog(this, "TA disabled: " + user.getEmail());
        loadData();
        if (refreshCallback != null) refreshCallback.run();
    }

    private void activateTA(User user) {
        userService.updateAccountStatus(user.getEmail(), AccountStatus.ACTIVE);
        AdminAuditLogger.log(adminEmail, "ACTIVATE_TA", auditTarget(user, "SUCCESS"));
        JOptionPane.showMessageDialog(this, "TA activated: " + user.getEmail());
        loadData();
        if (refreshCallback != null) refreshCallback.run();
    }

    private void resetPassword(User user) {
        try {
            new AuthService().resetPassword(user.getEmail(), "000000");
            AdminAuditLogger.log(adminEmail, "RESET_TA_PASSWORD", auditTarget(user, "SUCCESS"));
            JOptionPane.showMessageDialog(this, "Password reset to 000000\nEmail: " + user.getEmail());
        } catch (Exception ex) {
            AdminAuditLogger.log(adminEmail, "RESET_TA_PASSWORD", auditTarget(user, "FAILED: " + ex.getMessage()));
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Reset failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String auditTarget(User user, String result) {
        String targetEmail = user == null ? "unknown" : user.getEmail();
        return targetEmail + " RESULT=" + result;
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