import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

/**
 * Panel for BFS-based friend suggestions ranked by PriorityQueue (max-heap).
 */
public class SuggestFriendsPanel extends JPanel {

    private final SocialNetwork network;

    private JTextField userField;
    private JPanel     resultsPanel;
    private JLabel     resultsTitle;

    public SuggestFriendsPanel(SocialNetwork network) {
        this.network = network;
        setOpaque(false);
        setLayout(new BorderLayout(0, Theme.PAD_L));
        setBorder(new javax.swing.border.EmptyBorder(
                Theme.PAD_L, Theme.PAD_XL, Theme.PAD_L, Theme.PAD_XL));
        build();
    }

    private void build() {
        add(Theme.sectionTitle("💡 Friend Suggestions",
                "BFS traversal + Max-Heap ranking by mutual friends"), BorderLayout.NORTH);

        JPanel body = new JPanel(new BorderLayout(0, Theme.PAD_L));
        body.setOpaque(false);

        body.add(buildInputCard(), BorderLayout.NORTH);
        body.add(buildResultsSection(), BorderLayout.CENTER);

        add(body, BorderLayout.CENTER);
    }

    private JPanel buildInputCard() {
        JPanel card = Theme.card();
        card.setLayout(new FlowLayout(FlowLayout.LEFT, Theme.PAD_M, Theme.PAD_S));

        card.add(Theme.label("🎯 Target User:", Theme.FONT_LABEL, Theme.TEXT_SECONDARY));

        userField = Theme.textField("Enter username…");
        userField.setPreferredSize(new Dimension(220, 38));
        userField.addActionListener(this::onSuggest);
        card.add(userField);

        JButton suggestBtn = Theme.primaryButton("  Get Suggestions  ");
        suggestBtn.addActionListener(this::onSuggest);
        card.add(suggestBtn);

        card.add(Box.createHorizontalStrut(Theme.PAD_L));
        JLabel info = Theme.label(
                "Algorithm: BFS (depth-2) → count mutuals → PriorityQueue max-heap",
                Theme.FONT_SMALL, Theme.TEXT_MUTED);
        card.add(info);

        return card;
    }

    private JPanel buildResultsSection() {
        JPanel wrapper = new JPanel(new BorderLayout(0, Theme.PAD_S));
        wrapper.setOpaque(false);

        resultsTitle = Theme.label("Enter a username to see suggestions", Theme.FONT_SUBTITLE, Theme.TEXT_MUTED);
        wrapper.add(resultsTitle, BorderLayout.NORTH);

        resultsPanel = new JPanel();
        resultsPanel.setOpaque(false);
        resultsPanel.setLayout(new BoxLayout(resultsPanel, BoxLayout.Y_AXIS));

        wrapper.add(Theme.scrollPane(resultsPanel), BorderLayout.CENTER);
        return wrapper;
    }

    private void onSuggest(ActionEvent e) {
        String input = userField.getText().trim();
        String ph    = "Enter username…";

        resultsPanel.removeAll();

        if (input.isEmpty() || input.equals(ph)) {
            resultsTitle.setText("⚠ Please enter a username.");
            resultsTitle.setForeground(Theme.TEXT_WARNING);
            resultsPanel.revalidate(); resultsPanel.repaint();
            return;
        }

        if (!network.userExists(input)) {
            resultsTitle.setText("✘ User not found: " + input);
            resultsTitle.setForeground(Theme.TEXT_ERROR);
            resultsPanel.revalidate(); resultsPanel.repaint();
            return;
        }

        List<SocialNetwork.SuggestionEntry> suggestions = network.suggestFriends(input, 10);

        if (suggestions.isEmpty()) {
            resultsTitle.setText("No suggestions found for " + input + "  — try adding more connections.");
            resultsTitle.setForeground(Theme.TEXT_MUTED);
        } else {
            resultsTitle.setText("💡  Top " + suggestions.size() + " suggestion(s) for  " + input);
            resultsTitle.setForeground(Theme.TEXT_PRIMARY);

            int rank = 1;
            for (SocialNetwork.SuggestionEntry entry : suggestions) {
                JPanel card = buildSuggestionCard(rank++, entry);
                card.setAlignmentX(Component.LEFT_ALIGNMENT);
                resultsPanel.add(card);
                resultsPanel.add(Box.createVerticalStrut(Theme.PAD_S));
            }
        }

        resultsPanel.revalidate();
        resultsPanel.repaint();
    }

    private JPanel buildSuggestionCard(int rank, SocialNetwork.SuggestionEntry entry) {
        JPanel card = new JPanel(new BorderLayout(Theme.PAD_M, 0)) {
            private boolean hovered = false;
            {
                addMouseListener(new java.awt.event.MouseAdapter() {
                    public void mouseEntered(java.awt.event.MouseEvent e) { hovered = true;  repaint(); }
                    public void mouseExited(java.awt.event.MouseEvent e)  { hovered = false; repaint(); }
                });
            }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(hovered ? new Color(0x22223A) : Theme.BG_CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(hovered ? Theme.BORDER_ACCENT : Theme.BORDER_SUBTLE);
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 12, 12);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new javax.swing.border.EmptyBorder(Theme.PAD_M, Theme.PAD_M, Theme.PAD_M, Theme.PAD_M));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));

        // ── Rank badge ───────────────────────────────────────────────────────
        JPanel badge = buildRankBadge(rank);

        // ── Avatar ───────────────────────────────────────────────────────────
        JPanel avatar = buildAvatar(entry.getUser().getDisplayName());

        // ── Name + friends ───────────────────────────────────────────────────
        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));

        JLabel name = Theme.label(entry.getUser().getDisplayName(), Theme.FONT_SUBTITLE, Theme.TEXT_PRIMARY);
        JLabel fcount = Theme.label(
                entry.getUser().getFriendCount() + " friends  ·  Joined " + entry.getUser().getJoinedAt(),
                Theme.FONT_SMALL, Theme.TEXT_SECONDARY);
        name.setAlignmentX(Component.LEFT_ALIGNMENT);
        fcount.setAlignmentX(Component.LEFT_ALIGNMENT);
        info.add(name);
        info.add(Box.createVerticalStrut(3));
        info.add(fcount);

        // ── Mutual count pill ────────────────────────────────────────────────
        JPanel pill = buildMutualPill(entry.getMutualCount());

        // ── Bar fill ─────────────────────────────────────────────────────────
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, Theme.PAD_S, 0));
        left.setOpaque(false);
        left.add(badge);
        left.add(avatar);

        card.add(left,  BorderLayout.WEST);
        card.add(info,  BorderLayout.CENTER);
        card.add(pill,  BorderLayout.EAST);

        return card;
    }

    private JPanel buildRankBadge(int rank) {
        return new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color c = rank == 1 ? new Color(0xFFD700) :
                          rank == 2 ? new Color(0xC0C0C0) :
                          rank == 3 ? new Color(0xCD7F32) : Theme.TEXT_MUTED;
                g2.setColor(c);
                g2.setFont(Theme.FONT_NAV);
                FontMetrics fm = g2.getFontMetrics();
                String txt = "#" + rank;
                g2.drawString(txt, (getWidth() - fm.stringWidth(txt))/2,
                        (getHeight() - fm.getHeight())/2 + fm.getAscent());
                g2.dispose();
            }
            @Override public Dimension getPreferredSize() { return new Dimension(32, 32); }
        };
    }

    private JPanel buildAvatar(String name) {
        return new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0,0,Theme.ACCENT_PURPLE,36,36,Theme.ACCENT_BLUE);
                g2.setPaint(gp);
                g2.fillOval(0,0,36,36);
                g2.setColor(Color.WHITE);
                g2.setFont(Theme.FONT_NAV.deriveFont(14f));
                String init = name.isEmpty() ? "?" : String.valueOf(name.charAt(0)).toUpperCase();
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(init,(36-fm.stringWidth(init))/2,(36-fm.getHeight())/2+fm.getAscent());
                g2.dispose();
            }
            @Override public Dimension getPreferredSize() { return new Dimension(36,36); }
            @Override public Dimension getMinimumSize()   { return new Dimension(36,36); }
        };
    }

    private JPanel buildMutualPill(int count) {
        return new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0,0,Theme.ACCENT_BLUE,getWidth(),0,Theme.ACCENT_PURPLE);
                g2.setPaint(gp);
                g2.fillRoundRect(0,4,getWidth(),getHeight()-8,20,20);
                g2.setColor(Color.WHITE);
                g2.setFont(Theme.FONT_SMALL.deriveFont(Font.BOLD));
                String txt = count + " mutual";
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(txt,(getWidth()-fm.stringWidth(txt))/2,
                        (getHeight()-fm.getHeight())/2+fm.getAscent());
                g2.dispose();
            }
            @Override public Dimension getPreferredSize() { return new Dimension(80, 38); }
        };
    }
}
