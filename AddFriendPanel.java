import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Panel for creating friendships between users.
 */
public class AddFriendPanel extends JPanel {

    private final SocialNetwork network;
    private final Runnable      onUpdate;

    private JComboBox<String> userACombo;
    private JComboBox<String> userBCombo;
    private JLabel     feedbackLabel;

    public AddFriendPanel(SocialNetwork network, Runnable onUpdate) {
        this.network  = network;
        this.onUpdate = onUpdate;
        setOpaque(false);
        setLayout(new BorderLayout(0, Theme.PAD_L));
        setBorder(new javax.swing.border.EmptyBorder(
                Theme.PAD_L, Theme.PAD_XL, Theme.PAD_L, Theme.PAD_XL));
        build();
    }

    private void build() {
        add(Theme.sectionTitle("Add Friend", "Create a friendship connection between two users"), BorderLayout.NORTH);

        JPanel centre = new JPanel(new GridBagLayout());
        centre.setOpaque(false);
        centre.add(buildForm());
        add(centre, BorderLayout.CENTER);
    }

    private JPanel buildForm() {
        JPanel card = Theme.card();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setPreferredSize(new Dimension(460, 320));

        // Icon + titles
        JLabel icon = Theme.label("🤝", new Font("Segoe UI Emoji", Font.PLAIN, 36), Theme.TEXT_PRIMARY);
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel title = Theme.label("Connect Two Users", Theme.FONT_SUBTITLE, Theme.TEXT_PRIMARY);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel sub = Theme.label("Creates a bidirectional edge in the graph", Theme.FONT_BODY, Theme.TEXT_SECONDARY);
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Fields
        userACombo = createUserComboBox();
        userACombo.setAlignmentX(Component.CENTER_ALIGNMENT);
        userACombo.setMaximumSize(new Dimension(320, 44));
        userACombo.setPreferredSize(new Dimension(280, 44));

        // Connector label
        JLabel connector = Theme.label("⟷", Theme.FONT_STAT, Theme.ACCENT_BLUE);
        connector.setAlignmentX(Component.CENTER_ALIGNMENT);

        userBCombo = createUserComboBox();
        userBCombo.setAlignmentX(Component.CENTER_ALIGNMENT);
        userBCombo.setMaximumSize(new Dimension(320, 44));
        userBCombo.setPreferredSize(new Dimension(280, 44));

        // Button
        JButton connectBtn = Theme.primaryButton("  Connect  ");
        connectBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        connectBtn.addActionListener(this::onConnect);

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
        card.add(sub);
        card.add(Box.createVerticalStrut(Theme.PAD_L));
        card.add(userACombo);
        card.add(Box.createVerticalStrut(4));
        card.add(connector);
        card.add(Box.createVerticalStrut(4));
        card.add(userBCombo);
        card.add(Box.createVerticalStrut(Theme.PAD_M));
        card.add(connectBtn);
        card.add(Box.createVerticalStrut(Theme.PAD_S));
        card.add(feedbackLabel);
        card.add(Box.createVerticalStrut(Theme.PAD_M));
        return card;
    }

    private void onConnect(ActionEvent e) {
        String a = (String) userACombo.getSelectedItem();
        String b = (String) userBCombo.getSelectedItem();

        if (a == null || b == null || a.equals("Select user…") || b.equals("Select user…")) {
            showFeedback("⚠ Please select both users.", Theme.TEXT_WARNING);
            return;
        }

        String result = network.addFriend(a, b);

        if (result.equals("SUCCESS")) {
            showFeedback("✔ " + a + " and " + b + " are now friends!", Theme.TEXT_SUCCESS);
            onUpdate.run();
        } else {
            boolean warn = result.contains("already") || result.contains("themselves");
            showFeedback((warn ? "⚠ " : "✘ ") + result, warn ? Theme.TEXT_WARNING : Theme.TEXT_ERROR);
        }
    }

    private void showFeedback(String msg, Color color) {
        System.out.println("DEBUG: Showing feedback: " + msg);
        feedbackLabel.setText(msg);
        feedbackLabel.setForeground(color);
        feedbackLabel.repaint();
        Timer t = new Timer(20000, ev -> {  // 20 seconds
            feedbackLabel.setText("");
            feedbackLabel.repaint();
            System.out.println("DEBUG: Feedback cleared");
        });
        t.setRepeats(false);
        t.start();
    }

    private JComboBox<String> createUserComboBox() {
        JComboBox<String> combo = Theme.comboBox();
        combo.setPreferredSize(new Dimension(280, 44));
        combo.setMaximumSize(new Dimension(320, 44));
        refreshUserList(combo);
        return combo;
    }

    private void refreshUserList(JComboBox<String> combo) {
        combo.removeAllItems();
        combo.addItem("Select user…");
        for (User user : network.getAllUsers()) {
            combo.addItem(user.getDisplayName());
        }
    }

    /** Called when users are added to refresh combo boxes */
    public void refresh() {
        if (userACombo != null) refreshUserList(userACombo);
        if (userBCombo != null) refreshUserList(userBCombo);
    }
}
