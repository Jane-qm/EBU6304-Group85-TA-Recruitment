package ui.common;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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

import modules.job.Job;
import modules.user.MO;
import modules.user.User;
import modules.user.UserService;

/**
 * Job / course posting detail (same layout as TA catalog "Detail").
 */
public final class JobDetailDialog {

    private static final Color PRIMARY_BLUE = new Color(59, 130, 246);

    private JobDetailDialog() {
    }

    public static void show(Component parent, Job job) {
        if (job == null) {
            return;
        }
        UserService userService = UserService.getInstance();
        User mo = job.getMoUserId() != null ? userService.findById(job.getMoUserId()) : null;

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        panel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel(job.getModuleCode() + " - " + job.getTitle());
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        titleLabel.setForeground(PRIMARY_BLUE);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(15));

        panel.add(new JSeparator());
        panel.add(Box.createVerticalStrut(15));

        JPanel infoPanel = new JPanel(new java.awt.GridLayout(0, 2, 10, 10));
        infoPanel.setBackground(Color.WHITE);
        infoPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        infoPanel.add(createInfoLabel("Module Code:"));
        infoPanel.add(createValueLabel(job.getModuleCode()));

        infoPanel.add(createInfoLabel("Module Organiser (MO):"));
        infoPanel.add(createValueLabel(moDisplayName(mo)));

        infoPanel.add(createInfoLabel("MO Email:"));
        infoPanel.add(createValueLabel(moEmail(mo)));

        infoPanel.add(createInfoLabel("Weekly Hours:"));
        infoPanel.add(createValueLabel(job.getWeeklyHours() + " hours/week"));

        infoPanel.add(createInfoLabel("TA positions (headcount):"));
        infoPanel.add(createValueLabel(formatHeadcount(job)));

        infoPanel.add(createInfoLabel("Application Deadline:"));
        infoPanel.add(createValueLabel(formatApplicationDeadline(job)));

        infoPanel.add(createInfoLabel("Offer response due:"));
        String offerRaw = formatOfferResponseDeadline(job);
        infoPanel.add(createValueLabel(
                offerRaw == null || offerRaw.isBlank() || "Not set".equals(offerRaw) ? "—" : offerRaw));

        infoPanel.add(createInfoLabel("Status:"));
        String statusText = job.isApplicable() ? "Open for Applications"
                : (job.isExpired() ? "Closed - Deadline Passed" : String.valueOf(job.getStatus()));
        JLabel statusLabel = createValueLabel(statusText);
        if (job.isApplicable()) {
            statusLabel.setForeground(new Color(34, 197, 94));
        } else {
            statusLabel.setForeground(new Color(239, 68, 68));
        }
        infoPanel.add(statusLabel);

        if (job.getCreatedAt() != null) {
            infoPanel.add(createInfoLabel("Posted Date:"));
            infoPanel.add(createValueLabel(job.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))));
        }

        panel.add(infoPanel);
        panel.add(Box.createVerticalStrut(15));

        panel.add(new JSeparator());
        panel.add(Box.createVerticalStrut(15));

        JLabel skillsTitle = new JLabel("Required Skills");
        skillsTitle.setFont(new Font("SansSerif", Font.BOLD, 16));
        skillsTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(skillsTitle);
        panel.add(Box.createVerticalStrut(8));

        JPanel skillsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        skillsPanel.setBackground(Color.WHITE);
        skillsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        java.util.List<String> skills = job.getRequiredSkills();
        if (skills != null && !skills.isEmpty()) {
            for (String skill : skills) {
                JLabel skillTag = new JLabel("  " + skill + "  ");
                skillTag.setFont(new Font("SansSerif", Font.PLAIN, 14));
                skillTag.setBackground(new Color(243, 246, 251));
                skillTag.setForeground(PRIMARY_BLUE);
                skillTag.setOpaque(true);
                skillTag.setBorder(BorderFactory.createLineBorder(new Color(220, 224, 230)));
                skillsPanel.add(skillTag);
            }
        } else {
            skillsPanel.add(new JLabel("No specific skills required"));
        }
        panel.add(skillsPanel);
        panel.add(Box.createVerticalStrut(15));

        panel.add(new JSeparator());
        panel.add(Box.createVerticalStrut(15));

        JLabel descTitle = new JLabel("Details");
        descTitle.setFont(new Font("SansSerif", Font.BOLD, 16));
        descTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(descTitle);
        panel.add(Box.createVerticalStrut(8));

        JTextArea descArea = new JTextArea(8, 50);
        String detailsOnly = extractDetailsText(job);
        descArea.setText(detailsOnly.isBlank() ? "No additional details provided." : detailsOnly);
        descArea.setFont(new Font("SansSerif", Font.PLAIN, 15));
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setEditable(false);
        descArea.setBackground(Color.WHITE);
        descArea.setBorder(BorderFactory.createLineBorder(new Color(220, 224, 230)));

        JScrollPane descScroll = new JScrollPane(descArea);
        descScroll.setAlignmentX(Component.LEFT_ALIGNMENT);
        descScroll.setMaximumSize(new Dimension(500, 150));
        panel.add(descScroll);

        JScrollPane outer = new JScrollPane(panel);
        ScrollPaneTopHelper.installScrollStartsAtTop(outer);
        JOptionPane.showMessageDialog(parent, outer, "Course Details", JOptionPane.PLAIN_MESSAGE);
    }

    public static String formatApplicationDeadline(Job job) {
        if (job == null) {
            return "Not set";
        }
        if (job.getApplicationDeadline() != null) {
            return job.getApplicationDeadline().toLocalDate().toString();
        }
        String desc = job.getDescription();
        if (desc == null) {
            return "Not set";
        }
        for (String line : desc.split("\\R")) {
            if (line.trim().startsWith("Deadline:")) {
                String deadlinePart = line.substring(line.indexOf("Deadline:") + 9).trim();
                return deadlinePart.isEmpty() ? "Not set" : deadlinePart;
            }
        }
        return "Not set";
    }

    /**
     * Human-readable offer response deadline (date, end of day stored on {@link Job}).
     */
    public static String formatOfferResponseDeadline(Job job) {
        if (job == null) {
            return "Not set";
        }
        LocalDateTime o = job.getOfferResponseDeadline();
        if (o != null) {
            return o.toLocalDate().toString();
        }
        String desc = job.getDescription();
        if (desc == null) {
            return "Not set";
        }
        for (String line : desc.split("\\R")) {
            if (line.startsWith("Offer response due:")) {
                String v = line.substring("Offer response due:".length()).trim();
                return v.isEmpty() ? "Not set" : v;
            }
        }
        return "Not set";
    }

    public static String formatHeadcount(Job job) {
        if (job == null) {
            return "—";
        }
        if (job.getHeadcount() > 0) {
            return String.valueOf(job.getHeadcount());
        }
        String desc = job.getDescription();
        if (desc == null) {
            return "—";
        }
        for (String line : desc.split("\\R")) {
            if (line.startsWith("Headcount:")) {
                String v = line.substring("Headcount:".length()).trim();
                return v.isEmpty() ? "—" : v;
            }
        }
        return "—";
    }

    /**
     * Free-text details only (content after {@code Details:}), not the structured header block.
     */
    public static String extractDetailsText(Job job) {
        if (job == null) {
            return "";
        }
        String desc = job.getDescription();
        if (desc == null || desc.isBlank()) {
            return "";
        }
        String[] lines = desc.split("\\R", -1);
        boolean inDetails = false;
        StringBuilder detailsBuf = new StringBuilder();
        for (String ln : lines) {
            if (inDetails) {
                if (detailsBuf.length() > 0) {
                    detailsBuf.append('\n');
                }
                detailsBuf.append(ln);
                continue;
            }
            if (ln.startsWith("Details:")) {
                inDetails = true;
                detailsBuf.append(ln.substring("Details:".length()).trim());
            }
        }
        String out = detailsBuf.toString().trim();
        if (!out.isEmpty()) {
            return out;
        }
        // Legacy: no structured block — show full text so nothing is hidden
        if (!desc.startsWith("Skills:")) {
            return desc.trim();
        }
        return "";
    }

    private static JLabel createInfoLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 13));
        label.setForeground(new Color(55, 65, 81));
        return label;
    }

    private static JLabel createValueLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.PLAIN, 15));
        label.setForeground(new Color(30, 35, 45));
        return label;
    }

    private static String moDisplayName(User mo) {
        if (mo == null) {
            return "—";
        }
        if (mo instanceof MO) {
            String n = ((MO) mo).getName();
            if (n != null && !n.isBlank()) {
                return n.trim();
            }
        }
        String email = mo.getEmail();
        if (email == null) {
            return "—";
        }
        int at = email.indexOf('@');
        return at > 0 ? email.substring(0, at) : email;
    }

    private static String moEmail(User mo) {
        if (mo == null || mo.getEmail() == null) {
            return "—";
        }
        return mo.getEmail();
    }
}
