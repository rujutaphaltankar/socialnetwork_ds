import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

/**
 * Panel for finding mutual friends between two users.
 * Uses set-intersection on the adjacency lists.
 */
public class MutualFriendsPanel extends JPanel {

    private final SocialNetwork network;

    private JComboBox<String> userAField, userBField;
    private JPanel     resultsPanel;
    private JLabel     resultsTitle;

    public MutualFriendsPanel(SocialNetwork network) {
        this.network = network;
        setOpaque(false);
        setLayout(new BorderLayout(0, Theme.PAD_L));
        setBorder(new javax.swing.border.EmptyBorder(
                Theme.PAD_L, Theme.PAD_XL, Theme.PAD_L, Theme.PAD_XL));
        build();
    }

    private void build() {
        add(Theme.sectionTitle("🔗 Mutual Friends", "Find common friends shared between two users"), BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout(0, Theme.PAD_L));
        body.setOpaque(false);

        body.add(buildInputCard(), BorderLayout.NORTH);
        body.add(buildResultsCard(), BorderLayout.CENTER);

        add(body, BorderLayout.CENTER);
    }

    private JPanel buildInputCard() {
        JPanel card = Theme.card();
        card.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new java.awt.Insets(Theme.PAD_S, Theme.PAD_M, Theme.PAD_S, Theme.PAD_M);

        // User A section
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        card.add(Theme.label("👤 User A:", Theme.FONT_LABEL, Theme.TEXT_SECONDARY), gbc);

        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        userAField = createUserComboBox();
        userAField.setPreferredSize(new Dimension(200, 44));
        userAField.setMinimumSize(new Dimension(150, 44));
        card.add(userAField, gbc);

        // Intersection symbol
        gbc.gridx = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        card.add(Theme.label(" ∩ ", new Font("Segoe UI", Font.BOLD, 18), Theme.ACCENT_BLUE), gbc);

        // User B section
        gbc.gridx = 3; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        card.add(Theme.label("👤 User B:", Theme.FONT_LABEL, Theme.TEXT_SECONDARY), gbc);

        gbc.gridx = 4; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        userBField = createUserComboBox();
        userBField.setPreferredSize(new Dimension(200, 44));
        userBField.setMinimumSize(new Dimension(150, 44));
        card.add(userBField, gbc);

        // Button
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 5; gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER; gbc.weightx = 0; gbc.insets = new java.awt.Insets(Theme.PAD_L, Theme.PAD_M, Theme.PAD_M, Theme.PAD_M);
        JButton findBtn = Theme.primaryButton("Find Mutual Friends");
        findBtn.addActionListener(this::onFind);
        card.add(findBtn, gbc);

        return card;
    }

    private JPanel buildResultsCard() {
        JPanel card = Theme.card();
        card.setLayout(new BorderLayout(0, Theme.PAD_S));

        resultsTitle = new JLabel("Select two users above to find their mutual friends") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (!getText().startsWith("Select")) {
                    g2.setColor(Theme.BG_INPUT);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        resultsTitle.setFont(Theme.FONT_SUBTITLE);
        resultsTitle.setForeground(Theme.TEXT_SECONDARY);
        resultsTitle.setOpaque(false);
        resultsTitle.setBorder(new javax.swing.border.EmptyBorder(8, 16, 8, 16));
        card.add(resultsTitle, BorderLayout.NORTH);

        resultsPanel = new JPanel();
        resultsPanel.setOpaque(false);
        resultsPanel.setLayout(new WrapLayout(FlowLayout.LEFT, Theme.PAD_S, Theme.PAD_S));

        card.add(Theme.scrollPane(resultsPanel), BorderLayout.CENTER);
        return card;
    }

    private void onFind(ActionEvent e) {
        String a = (String) userAField.getSelectedItem();
        String b = (String) userBField.getSelectedItem();

        System.out.println("DEBUG: Finding mutual friends between '" + a + "' and '" + b + "'");

        resultsPanel.removeAll();
        resultsTitle.setText("🔍 Searching for mutual friends...");
        resultsTitle.setForeground(Theme.ACCENT_BLUE);
        resultsPanel.revalidate(); resultsPanel.repaint();

        // Small delay to show the searching message
        Timer searchTimer = new Timer(200, ev -> performSearch(a, b));
        searchTimer.setRepeats(false);
        searchTimer.start();
    }

    private void performSearch(String a, String b) {
        if (a == null || b == null || a.equals("Select user…") || b.equals("Select user…")) {
            resultsTitle.setText("⚠ Please select both users.");
            resultsTitle.setForeground(Theme.TEXT_WARNING);
            resultsPanel.revalidate(); resultsPanel.repaint();
            return;
        }

        if (!network.userExists(a)) {
            resultsTitle.setText("✘ User not found: " + a);
            resultsTitle.setForeground(Theme.TEXT_ERROR);
            resultsPanel.revalidate(); resultsPanel.repaint();
            return;
        }
        if (!network.userExists(b)) {
            resultsTitle.setText("✘ User not found: " + b);
            resultsTitle.setForeground(Theme.TEXT_ERROR);
            resultsPanel.revalidate(); resultsPanel.repaint();
            return;
        }

        List<User> mutuals = network.findMutualFriends(a, b);
        System.out.println("DEBUG: Found " + mutuals.size() + " mutual friends");

        if (mutuals.isEmpty()) {
            resultsTitle.setText("🤷 " + a + " and " + b + " have no mutual friends.");
            resultsTitle.setForeground(Theme.TEXT_MUTED);
        } else {
            resultsTitle.setText("🔗 " + mutuals.size() + " mutual friend" + (mutuals.size() == 1 ? "" : "s") + " between " + a + " and " + b);
            resultsTitle.setForeground(Theme.TEXT_SUCCESS);
            for (User u : mutuals) {
                resultsPanel.add(buildUserChip(u));
                System.out.println("DEBUG: Mutual friend: " + u.getDisplayName());
            }
        }

        resultsPanel.revalidate();
        resultsPanel.repaint();
        System.out.println("DEBUG: Results panel updated");
    }

    private JPanel buildUserChip(User u) {
        JPanel chip = new JPanel(new FlowLayout(FlowLayout.LEFT, Theme.PAD_S, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.BG_INPUT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 24, 24);
                GradientPaint gp = new GradientPaint(0, 0, Theme.ACCENT_BLUE, getWidth(), 0, Theme.ACCENT_PURPLE);
                g2.setPaint(gp);
                Stroke old = g2.getStroke();
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 24, 24);
                g2.setStroke(old);
                g2.dispose();
            }
        };
        chip.setOpaque(false);
        chip.setBorder(new javax.swing.border.EmptyBorder(6, 12, 6, 16));

        // Avatar
        JPanel avatar = buildMiniAvatar(u.getDisplayName());
        JLabel name   = Theme.label(u.getDisplayName(), Theme.FONT_BODY, Theme.TEXT_PRIMARY);
        JLabel frnd   = Theme.label("  " + u.getFriendCount() + " friends", Theme.FONT_SMALL, Theme.TEXT_MUTED);

        chip.add(avatar);
        chip.add(name);
        chip.add(frnd);
        return chip;
    }

    private JPanel buildMiniAvatar(String name) {
        return new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, Theme.ACCENT_BLUE, 22, 22, Theme.ACCENT_PURPLE);
                g2.setPaint(gp);
                g2.fillOval(0, 0, 22, 22);
                g2.setColor(Color.WHITE);
                g2.setFont(Theme.FONT_SMALL.deriveFont(Font.BOLD, 10f));
                String init = name.isEmpty() ? "?" : String.valueOf(name.charAt(0)).toUpperCase();
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(init, (22 - fm.stringWidth(init))/2, (22 - fm.getHeight())/2 + fm.getAscent());
                g2.dispose();
            }
            @Override public Dimension getPreferredSize() { return new Dimension(22, 22); }
        };
    }

    private JComboBox<String> createUserComboBox() {
        JComboBox<String> combo = Theme.comboBox();
        combo.setPreferredSize(new Dimension(200, 44));
        combo.setMinimumSize(new Dimension(150, 44));
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
        if (userAField != null) refreshUserList(userAField);
        if (userBField != null) refreshUserList(userBField);
    }
}

/**
 * FlowLayout that wraps components onto new rows as needed.
 */
class WrapLayout extends FlowLayout {
    public WrapLayout(int align, int hgap, int vgap) { super(align, hgap, vgap); }

    @Override public Dimension preferredLayoutSize(Container target) {
        return layoutSize(target, true);
    }
    @Override public Dimension minimumLayoutSize(Container target) {
        return layoutSize(target, false);
    }

    private Dimension layoutSize(Container target, boolean pref) {
        synchronized (target.getTreeLock()) {
            int targetWidth = target.getSize().width;
            if (targetWidth == 0) targetWidth = Integer.MAX_VALUE;

            int hgap = getHgap(), vgap = getVgap();
            Insets insets = target.getInsets();
            int maxWidth = targetWidth - (insets.left + insets.right + hgap * 2);

            Dimension dim = new Dimension(0, 0);
            int rowWidth = 0, rowHeight = 0;

            int n = target.getComponentCount();
            for (int i = 0; i < n; i++) {
                Component c = target.getComponent(i);
                if (c.isVisible()) {
                    Dimension d = pref ? c.getPreferredSize() : c.getMinimumSize();
                    if (rowWidth + d.width > maxWidth) {
                        dim.height += rowHeight + vgap;
                        rowWidth = d.width + hgap;
                        rowHeight = d.height;
                    } else {
                        rowWidth += d.width + hgap;
                        rowHeight = Math.max(rowHeight, d.height);
                    }
                    dim.width = Math.max(dim.width, rowWidth);
                }
            }
            dim.height += rowHeight + vgap * 2;
            dim.height += insets.top + insets.bottom;
            return dim;
        }
    }
}
