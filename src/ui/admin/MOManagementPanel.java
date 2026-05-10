package ui.admin;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.JPasswordField;
import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import modules.user.AccountStatus;
import modules.user.User;
import modules.user.UserRole;
import modules.user.UserService;
import modules.user.MO;
import modules.auth.AuthService;
import ui.common.TableListActionStyle;
import ui.common.TableScrollUtil;

/**
 * MO Management Panel for Admin
 * Table actions: borderless bold text (blue / green / red by intent).
 */
public class MOManagementPanel extends JPanel {
    private final UserService userService = UserService.getInstance();
    private JTable table;
    private DefaultTableModel tableModel;
    private Runnable refreshCallback;
    private List<User> moUsers;

    public MOManagementPanel(Runnable refreshCallback) {
        this.refreshCallback = refreshCallback;
        setLayout(new BorderLayout());
        setBackground(new Color(248, 250, 252));
        setBorder(new EmptyBorder(20, 20, 20, 20));
        initUI();
        loadData();
    }

    private void initUI() {
        JLabel titleLabel = new JLabel("MO Management");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        add(titleLabel, BorderLayout.NORTH);

        // Top button panel
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        topPanel.setBackground(new Color(248, 250, 252));

        JButton importBtn = createButton("📥 Import MO from CSV");
        importBtn.addActionListener(e -> importMOFromCSV());

        JButton addBtn = createButton("➕ Add Single MO");
        addBtn.addActionListener(e -> showAddMODialog());

        topPanel.add(importBtn);
        topPanel.add(addBtn);
        add(topPanel, BorderLayout.NORTH);

        // Table
        String[] columns = {"Name", "Email", "Status", "Actions", "Reset Password"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 3 || column == 4;
            }
        };

        table = new JTable(tableModel);
        table.setRowHeight(45);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));

        // Set custom renderer and editor for button columns
        table.getColumnModel().getColumn(3).setCellRenderer(new ButtonRenderer());
        table.getColumnModel().getColumn(3).setCellEditor(new ButtonEditor());
        table.getColumnModel().getColumn(4).setCellRenderer(new ButtonRenderer());
        table.getColumnModel().getColumn(4).setCellEditor(new ButtonEditor());

        TableScrollUtil.ColumnSpec[] moCols = {
                TableScrollUtil.ColumnSpec.flex(96, 150),
                TableScrollUtil.ColumnSpec.flex(130, 280),
                TableScrollUtil.ColumnSpec.fixed(100),
                TableScrollUtil.ColumnSpec.fixed(224),
                TableScrollUtil.ColumnSpec.fixed(140),
        };

        JScrollPane moScroll = TableScrollUtil.wrapTable(table);
        TableScrollUtil.installResponsiveColumns(table, moScroll, moCols);
        add(moScroll, BorderLayout.CENTER);
    }

    private JButton createButton(String text) {
        JButton button = new JButton(text);
        TableListActionStyle.applyToButton(button, text);
        button.setFont(new Font("SansSerif", Font.BOLD, 12));
        return button;
    }

    // ui/admin/MOManagementPanel.java

    private void loadData() {
        tableModel.setRowCount(0);
        moUsers = userService.listAllUsers().stream()
                .filter(u -> u.getRole() == UserRole.MO)
                .toList();

        for (User user : moUsers) {
            // 优先使用存储的姓名，如果没有则从邮箱提取前缀
            String name;
            if (user instanceof MO && ((MO) user).getName() != null && !((MO) user).getName().isEmpty()) {
                name = ((MO) user).getName();
            } else {
                name = extractNameFromEmail(user.getEmail());
            }
            String email = user.getEmail();
            String status = getStatusText(user.getStatus());

            tableModel.addRow(new Object[]{
                    name, email, status,
                    "Status", "Reset Pwd"
            });
        }
    }

    private void importMOFromCSV() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select MO CSV File");
        fileChooser.setFileFilter(new FileNameExtensionFilter("CSV files", "csv"));

        if (fileChooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        UserService.MOImportResult result = userService.importMOFromCSV(fileChooser.getSelectedFile().getAbsolutePath());

        String message = String.format("Import completed!\nSuccess: %d\nFailed: %d",
                result.successCount, result.failCount);
        if (!result.errors.isEmpty()) {
            message += "\n\nErrors:\n" + String.join("\n", result.errors);
        }
        JOptionPane.showMessageDialog(this, message, "Import Result",
                result.failCount > 0 ? JOptionPane.WARNING_MESSAGE : JOptionPane.INFORMATION_MESSAGE);

        loadData();
        if (refreshCallback != null) refreshCallback.run();
    }

    private void showAddMODialog() {
        JTextField emailField = new JTextField(20);
        JTextField nameField = new JTextField(20);
        JPasswordField passwordField = new JPasswordField(20);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        panel.add(emailField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1;
        panel.add(nameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        panel.add(passwordField, gbc);

        int result = JOptionPane.showConfirmDialog(this, panel, "Add New MO",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String email = emailField.getText().trim();
            String password = new String(passwordField.getPassword());
            String name = nameField.getText().trim();

            if (email.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Email and password are required.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (password.length() < 6) {
                JOptionPane.showMessageDialog(this, "Password must be at least 6 characters.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                if (userService.findByEmail(email) != null) {
                    JOptionPane.showMessageDialog(this, "Email already exists: " + email, "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                userService.register(email, password, UserRole.MO);
                JOptionPane.showMessageDialog(this, "MO created successfully!\nEmail: " + email, "Success", JOptionPane.INFORMATION_MESSAGE);
                loadData();
                if (refreshCallback != null) refreshCallback.run();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Failed to create MO: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Button Renderer
    private class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            User user = moUsers.get(row);

            if (column == 3) {
                if (user.getStatus() == AccountStatus.ACTIVE) {
                    setText("Disable");
                } else {
                    setText("Activate");
                }
            } else if (column == 4) {
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

            User user = moUsers.get(row);

            if (column == 3) {
                if (user.getStatus() == AccountStatus.ACTIVE) {
                    button.setText("Disable");
                } else {
                    button.setText("Activate");
                }
            } else if (column == 4) {
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
            User user = moUsers.get(currentRow);

            if (currentColumn == 3) {
                if (user.getStatus() == AccountStatus.ACTIVE) {
                    disableMO(user);
                } else {
                    activateMO(user);
                }
            } else if (currentColumn == 4) {
                resetPassword(user);
            }

            fireEditingStopped();
        }
    }

    private void activateMO(User user) {
        userService.updateAccountStatus(user.getEmail(), AccountStatus.ACTIVE);
        JOptionPane.showMessageDialog(this, "MO activated: " + user.getEmail());
        loadData();
        if (refreshCallback != null) refreshCallback.run();
    }

    private void disableMO(User user) {
        userService.disableAccount(user.getEmail());
        JOptionPane.showMessageDialog(this, "MO disabled: " + user.getEmail());
        loadData();
        if (refreshCallback != null) refreshCallback.run();
    }

    private void resetPassword(User user) {
        try {
            new AuthService().resetPassword(user.getEmail(), "000000");
            JOptionPane.showMessageDialog(this, "Password reset to 000000\nEmail: " + user.getEmail());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Reset failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String getStatusText(AccountStatus status) {
        if (status == AccountStatus.ACTIVE) return "Active";
        if (status == AccountStatus.PENDING) return "Inactive";
        return "Disabled";
    }

    private String extractNameFromEmail(String email) {
        if (email == null) return "Unknown";
        int atIndex = email.indexOf('@');
        return atIndex > 0 ? email.substring(0, atIndex) : email;
    }

    public void refresh() {
        loadData();
    }
}