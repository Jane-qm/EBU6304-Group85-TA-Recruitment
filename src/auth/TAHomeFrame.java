package auth;

import common.entity.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class TAHomeFrame extends JFrame {

    private final User user;

    public TAHomeFrame(User user) {
        this.user = user;
        initUI();
    }

    private void initUI() {
        setTitle("TA Dashboard");
        setSize(760, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBorder(new EmptyBorder(40, 40, 40, 40));
        root.setBackground(new Color(245, 247, 251));

        JLabel title = new JLabel("Welcome, TA");
        title.setFont(new Font("SansSerif", Font.BOLD, 28));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        root.add(title);
        root.add(Box.createVerticalStrut(40));

        root.add(createButton("📄 View Jobs"));
        root.add(Box.createVerticalStrut(20));

        root.add(createButton("📝 Apply for Job"));
        root.add(Box.createVerticalStrut(20));

        root.add(createButton("📬 View Offers"));

        add(root);
    }

    private JButton createButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.BOLD, 18));
        btn.setMaximumSize(new Dimension(300, 60));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setBackground(new Color(16, 185, 129)); // 绿色 = TA
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        return btn;
    }
}
