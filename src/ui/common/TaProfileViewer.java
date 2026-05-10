package ui.common;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

import modules.profile.TAProfile;
import modules.user.User;

/**
 * Read-only TA profile presentation for MO / Admin, aligned with TA course-detail dialog styling.
 */
public final class TaProfileViewer {

    private static final Color PRIMARY_BLUE = new Color(59, 130, 246);
    private static final Color LABEL_FG = new Color(55, 65, 81);
    private static final Color VALUE_FG = new Color(30, 35, 45);
    private static final Color MUTED_FG = new Color(107, 114, 128);

    private TaProfileViewer() {
    }

    /**
     * @param applicationStatement optional; shown when non-null/non-blank (e.g. from an application row).
     */
    public static void show(Component parent, User user, TAProfile profile, String applicationStatement) {
        if (profile == null) {
            return;
        }

        String email = user != null && user.getEmail() != null ? user.getEmail() : profile.getEmail();
        String nameTitle = profile.getFullName();
        if (nameTitle == null || nameTitle.isBlank()) {
            nameTitle = email != null ? email : "TA Profile";
        }

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        panel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel(nameTitle);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        titleLabel.setForeground(PRIMARY_BLUE);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(titleLabel);

        if (email != null && !email.isBlank()) {
            panel.add(Box.createVerticalStrut(4));
            JLabel emailLine = new JLabel(email);
            emailLine.setFont(new Font("SansSerif", Font.PLAIN, 15));
            emailLine.setForeground(MUTED_FG);
            emailLine.setAlignmentX(Component.LEFT_ALIGNMENT);
            panel.add(emailLine);
        }

        panel.add(Box.createVerticalStrut(14));
        panel.add(new JSeparator());
        panel.add(Box.createVerticalStrut(14));

        JPanel infoPanel = new JPanel(new java.awt.GridLayout(0, 2, 10, 10));
        infoPanel.setBackground(Color.WHITE);
        infoPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        addRow(infoPanel, "Chinese name:", nv(profile.getChineseName()));
        addRow(infoPanel, "Student ID:", nv(profile.getStudentId()));
        addRow(infoPanel, "Phone:", nv(profile.getPhone()));
        addRow(infoPanel, "Gender:",
                profile.getGender() != null ? profile.getGender().getEnglishName() : "N/A");
        addRow(infoPanel, "School:", nv(profile.getSchool()));
        addRow(infoPanel, "Supervisor:", nv(profile.getSupervisor()));
        addRow(infoPanel, "Major:", nv(profile.getMajor()));
        addRow(infoPanel, "Student type:",
                profile.getStudentType() != null ? profile.getStudentType().getEnglishName() : "N/A");
        addRow(infoPanel, "Current year:",
                profile.getCurrentYear() != null ? profile.getCurrentYear().getEnglishName() : "N/A");
        addRow(infoPanel, "Campus:",
                profile.getCampus() != null ? profile.getCampus().getEnglishName() : "N/A");
        addRow(infoPanel, "Available hours:", profile.getAvailableWorkingHours() + " hours/week");

        panel.add(infoPanel);
        panel.add(Box.createVerticalStrut(14));
        panel.add(new JSeparator());
        panel.add(Box.createVerticalStrut(14));

        JLabel skillsTitle = new JLabel("Skills");
        skillsTitle.setFont(new Font("SansSerif", Font.BOLD, 16));
        skillsTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(skillsTitle);
        panel.add(Box.createVerticalStrut(8));

        JPanel skillsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        skillsPanel.setBackground(Color.WHITE);
        skillsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        List<String> skills = profile.getSkillTags();
        if (skills != null && !skills.isEmpty()) {
            for (String skill : skills) {
                if (skill == null || skill.isBlank()) {
                    continue;
                }
                JLabel tag = new JLabel("  " + skill.trim() + "  ");
                tag.setFont(new Font("SansSerif", Font.PLAIN, 14));
                tag.setBackground(new Color(243, 246, 251));
                tag.setForeground(PRIMARY_BLUE);
                tag.setOpaque(true);
                tag.setBorder(BorderFactory.createLineBorder(new Color(220, 224, 230)));
                skillsPanel.add(tag);
            }
        }
        if (skillsPanel.getComponentCount() == 0) {
            skillsPanel.add(new JLabel("No skills listed"));
        }
        panel.add(skillsPanel);

        panel.add(Box.createVerticalStrut(14));
        panel.add(new JSeparator());
        panel.add(Box.createVerticalStrut(14));

        addTextSection(panel, "Previous experience",
                profile.getPreviousExperience() != null ? profile.getPreviousExperience() : "None provided.");

        if (applicationStatement != null && !applicationStatement.isBlank()) {
            panel.add(Box.createVerticalStrut(14));
            panel.add(new JSeparator());
            panel.add(Box.createVerticalStrut(14));
            addTextSection(panel, "Application statement", applicationStatement.trim());
        }

        JScrollPane scroll = new JScrollPane(panel);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(220, 224, 230)));
        scroll.setPreferredSize(new Dimension(560, 480));
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        ScrollPaneTopHelper.installScrollStartsAtTop(scroll);

        Component dlgParent = parent;
        if (dlgParent == null || !dlgParent.isDisplayable()) {
            dlgParent = null;
        }

        JOptionPane.showMessageDialog(dlgParent, scroll,
                "TA Profile",
                JOptionPane.PLAIN_MESSAGE);
    }

    private static void addRow(JPanel grid, String label, String value) {
        grid.add(infoLabel(label));
        grid.add(valueLabel(value));
    }

    private static JLabel infoLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.BOLD, 15));
        l.setForeground(LABEL_FG);
        return l;
    }

    private static JLabel valueLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", Font.PLAIN, 15));
        l.setForeground(VALUE_FG);
        return l;
    }

    private static String nv(String s) {
        return s == null || s.isBlank() ? "N/A" : s;
    }

    private static void addTextSection(JPanel column, String heading, String body) {
        JLabel h = new JLabel(heading);
        h.setFont(new Font("SansSerif", Font.BOLD, 16));
        h.setAlignmentX(Component.LEFT_ALIGNMENT);
        column.add(h);
        column.add(Box.createVerticalStrut(8));

        JTextArea area = new JTextArea(6, 48);
        area.setText(body);
        area.setFont(new Font("SansSerif", Font.PLAIN, 15));
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setEditable(false);
        area.setBackground(Color.WHITE);
        area.setBorder(BorderFactory.createLineBorder(new Color(220, 224, 230)));
        area.setAlignmentX(Component.LEFT_ALIGNMENT);
        column.add(area);
    }
}
