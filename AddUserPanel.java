import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Panel for adding new users to the network.
 */
public class AddUserPanel extends JPanel {

    private final SocialNetwork network;
    private final Runnable onUpdate;

    private JTextField usernameField;
    private JLabel     feedbackLabel;

    public AddUserPanel(SocialNetwork network, Runnable onUpdate) {
        this.network  = network;
        this.onUpdate = onUpdate;
        setOpaque(false);
        setLayout(new BorderLayout(0, Theme.PAD_L));
        setBorder(new javax.swing.border.EmptyBorder(
                Theme.PAD_L, Theme.PAD_XL, Theme.PAD_L, Theme.PAD_XL));
        build();
    }

    private void build() {
        add(Theme.sectionTitle("👤 Add User", "Register a new member in the social network"), BorderLayout.NORTH);

        // ── Centre form ──────────────────────────────────────────────────────
        JPanel centre = new JPanel(new GridBagLayout());
        centre.setOpaque(false);

        JPanel form = buildForm();
        centre.add(form);

        add(centre, BorderLayout.CENTER);
    }

    private JPanel buildForm() {
        JPanel card = Theme.card();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setPreferredSize(new Dimension(440, 280));

        // Header
        JLabel icon = Theme.label("👤", new Font("Segoe UI Emoji", Font.PLAIN, 36), Theme.TEXT_PRIMARY);
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel title = Theme.label("👤 Create New User", Theme.FONT_SUBTITLE, Theme.TEXT_PRIMARY);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitle = Theme.label("Enter a unique username below", Theme.FONT_BODY, Theme.TEXT_SECONDARY);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Input
        usernameField = Theme.textField("Enter username…");
        usernameField.setAlignmentX(Component.CENTER_ALIGNMENT);
        usernameField.setMaximumSize(new Dimension(320, 38));

        // Button
        JButton addBtn = Theme.primaryButton("  Add User  ");
        addBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        addBtn.addActionListener(this::onAddUser);

        // Allow Enter key
        usernameField.addActionListener(this::onAddUser);

        feedbackLabel = new JLabel("") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (!getText().isEmpty()) {
                    g2.setColor(Theme.BG_INPUT);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        feedbackLabel.setFont(Theme.FONT_SUBTITLE.deriveFont(Font.BOLD));
        feedbackLabel.setForeground(Theme.TEXT_SUCCESS);
        feedbackLabel.setOpaque(false);
        feedbackLabel.setBorder(new javax.swing.border.EmptyBorder(8, 16, 8, 16));
        feedbackLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(Box.createVerticalStrut(Theme.PAD_M));
        card.add(icon);
        card.add(Box.createVerticalStrut(Theme.PAD_S));
        card.add(title);
        card.add(Box.createVerticalStrut(4));
        card.add(subtitle);
        card.add(Box.createVerticalStrut(Theme.PAD_L));
        card.add(usernameField);
        card.add(Box.createVerticalStrut(Theme.PAD_M));
        card.add(addBtn);
        card.add(Box.createVerticalStrut(Theme.PAD_S));
        card.add(feedbackLabel);
        card.add(Box.createVerticalStrut(Theme.PAD_M));
        return card;
    }

    private void onAddUser(ActionEvent e) {
        String input = usernameField.getText().trim();
        String placeholder = "Enter username…";

        System.out.println("DEBUG: Adding user '" + input + "'");

        if (input.isEmpty() || input.equals(placeholder)) {
            showFeedback("⚠ Please enter a username.", Theme.TEXT_WARNING);
            return;
        }

        try {
            boolean added = network.addUser(input);
            System.out.println("DEBUG: User addition result: " + added);
            if (added) {
                showFeedback("✔ User \"" + input + "\" added successfully!", Theme.TEXT_SUCCESS);
                usernameField.setText(placeholder);
                usernameField.setForeground(Theme.TEXT_MUTED);
                onUpdate.run();
                System.out.println("DEBUG: User added and UI updated");
            } else {
                showFeedback("⚠ Username \"" + input + "\" already exists.", Theme.TEXT_WARNING);
            }
        } catch (IllegalArgumentException ex) {
            showFeedback("✘ " + ex.getMessage(), Theme.TEXT_ERROR);
            System.out.println("DEBUG: Error adding user: " + ex.getMessage());
        }
    }

    private void showFeedback(String msg, Color color) {
        System.out.println("DEBUG: Showing feedback: " + msg);
        feedbackLabel.setText(msg);
        feedbackLabel.setForeground(color);
        feedbackLabel.repaint();

        // Make feedback stay longer and be more visible
        Timer t = new Timer(20000, ev -> {  // 20 seconds
            feedbackLabel.setText("");
            feedbackLabel.repaint();
            System.out.println("DEBUG: Feedback cleared");
        });
        t.setRepeats(false);
        t.start();
    }
}
