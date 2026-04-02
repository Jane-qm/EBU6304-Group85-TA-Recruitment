package auth;

import common.entity.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class MOHomeFrame extends JFrame {

    private final User user;

    public MOHomeFrame(User user) {
        this.user = user;
        initUI();
    }

    private void initUI() {
        setTitle("MO Dashboard");
        setSize(760, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBorder(new EmptyBorder(40, 40, 40, 40));
        root.setBackground(new Color(245, 247, 251));

        JLabel title = new JLabel("Welcome, MO");
        title.setFont(new Font("SansSerif", Font.BOLD, 28));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        root.add(title);
        root.add(Box.createVerticalStrut(40));

        root.add(createButton("📌 Post Job"));
        root.add(Box.createVerticalStrut(20));

        root.add(createButton("👀 View Applicants"));
        root.add(Box.createVerticalStrut(20));

        root.add(createButton("📨 Send Offer"));

        add(root);
    }

    private JButton createButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.BOLD, 18));
        btn.setMaximumSize(new Dimension(300, 60));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setBackground(new Color(37, 99, 235));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        return btn;
    }
}
