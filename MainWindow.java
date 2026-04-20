import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Map;

/**
 * Main application window.
 * Contains a dark sidebar navigation + a content area that swaps panels.
 */
public class MainWindow extends JFrame {

    private final SocialNetwork network;

    // Panels
    private DashboardPanel    dashboardPanel;
    private AddUserPanel      addUserPanel;
    private AddFriendPanel    addFriendPanel;
    private MutualFriendsPanel mutualPanel;
    private SuggestFriendsPanel suggestPanel;
    private GraphPanel        graphPanel;

    // Sidebar nav buttons
    private final JButton[] navButtons = new JButton[6];
    private final JPanel[] navBadges = new JPanel[6]; 
    private int selectedNav = 0;

    // Bottom Stats
    private JLabel usersStatLabel;
    private JLabel edgesStatLabel;
    private JLabel densityStatLabel;

    // Content container
    private JPanel contentArea;
    private JLabel topTitleLabel;

    public MainWindow() {
        super("SocialGraph Mini Engine");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1080, 720);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);

        // App icon (Unicode fallback)
        setIconImage(createAppIcon());

        // Load persisted data or create new network
        network = new SocialNetwork(true);
        
        // Seed demo data only if this is first run (no users loaded)
        if (network.getTotalUsers() == 0) {
            seedDemoData();
        }
        
        // Add shutdown hook to close database connection
        Runtime.getRuntime().addShutdownHook(new Thread(DatabaseManager::close));
        
        buildUI();
        refreshShellStats();
        setVisible(true);
    }

    private void seedDemoData() {
        String[] users = {"Alice", "Bob", "Charlie", "Diana", "Eve", "Frank"};
        for (String u : users) network.addUser(u);

        network.addFriend("Alice", "Bob");
        network.addFriend("Alice", "Charlie");
        network.addFriend("Bob", "Diana");
        network.addFriend("Charlie", "Diana");
        network.addFriend("Diana", "Eve");
        network.addFriend("Eve", "Frank");
        network.addFriend("Bob", "Charlie");
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(Theme.BG_DARKEST);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        root.setOpaque(true);

        root.add(buildSidebar(), BorderLayout.WEST);
        
        JPanel rightSide = new JPanel(new BorderLayout());
        rightSide.setOpaque(false);
        rightSide.add(buildTopBar(), BorderLayout.NORTH);
        rightSide.add(buildContent(), BorderLayout.CENTER);
        
        root.add(rightSide, BorderLayout.CENTER);

        setContentPane(root);
    }

    // ── Top Bar ───────────────────────────────────────────────────────────────
    private JPanel buildTopBar() {
        JPanel topBar = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(Theme.BG_PANEL);
                g2.fillRect(0,0,getWidth(),getHeight());
                g2.setColor(Theme.BORDER_SUBTLE);
                g2.drawLine(0,getHeight()-1,getWidth(),getHeight()-1);
                g2.dispose();
            }
        };
        topBar.setOpaque(false);
        topBar.setPreferredSize(new Dimension(0, 64));
        topBar.setBorder(new javax.swing.border.EmptyBorder(0, Theme.PAD_L, 0, Theme.PAD_L));

        topTitleLabel = Theme.label("Dashboard", Theme.FONT_TITLE, Theme.TEXT_PRIMARY);
        topBar.add(topTitleLabel, BorderLayout.WEST);

        JPanel rightControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, Theme.PAD_M, 13));
        rightControls.setOpaque(false);

        // Search Input
        JTextField searchField = Theme.textField("Search users...");
        searchField.setPreferredSize(new Dimension(200, 38));
        
        // Search Icon overlay (simplified)
        JPanel searchBox = new JPanel(new BorderLayout(8, 0));
        searchBox.setOpaque(false);
        JLabel searchIcon = Theme.label("🔍", Theme.FONT_BODY, Theme.TEXT_MUTED);
        searchBox.add(searchIcon, BorderLayout.WEST);
        searchBox.add(searchField, BorderLayout.CENTER);

        // Avatar
        JPanel avatar = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0x282832));
                g2.fillOval(0,0,36,36);
                g2.setColor(Theme.BORDER_FOCUS);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawOval(0,0,35,35);
                g2.setColor(Theme.TEXT_PRIMARY);
                g2.setFont(Theme.FONT_BODY.deriveFont(Font.BOLD, 12f));
                FontMetrics fm = g2.getFontMetrics();
                int x = (36 - fm.stringWidth("AL")) / 2;
                int y = (36 - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString("AL", x, y);
                g2.dispose();
            }
            @Override public Dimension getPreferredSize() { return new Dimension(36, 36); }
        };

        rightControls.add(searchBox);
        rightControls.add(avatar);

        topBar.add(rightControls, BorderLayout.EAST);
        return topBar;
    }

    // ── Sidebar ───────────────────────────────────────────────────────────────
    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(Theme.BG_SIDEBAR);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(Theme.BORDER_SUBTLE);
                g2.drawLine(getWidth()-1, 0, getWidth()-1, getHeight());
                g2.dispose();
            }
        };
        sidebar.setOpaque(false);
        sidebar.setPreferredSize(new Dimension(240, 0));

        JPanel navList = new JPanel();
        navList.setOpaque(false);
        navList.setLayout(new BoxLayout(navList, BoxLayout.Y_AXIS));

        // Logo
        navList.add(buildLogo());
        navList.add(Box.createVerticalStrut(Theme.PAD_S));

        // Sections
        navList.add(buildNavHeader("MAIN"));
        addNavItem(navList, 0, "⊞", "Dashboard", true);
        addNavItem(navList, 1, "👤", "Users", false);

        navList.add(Box.createVerticalStrut(Theme.PAD_S));
        navList.add(buildNavHeader("TOOLS"));
        addNavItem(navList, 2, "🔗", "Add friendship", false);
        addNavItem(navList, 3, "🎯", "Mutual friends", false);
        addNavItem(navList, 4, "⭐", "Suggestions", false);

        navList.add(Box.createVerticalStrut(Theme.PAD_S));
        navList.add(buildNavHeader("GRAPH"));
        addNavItem(navList, 5, "⛦", "BFS explorer", false);

        sidebar.add(navList, BorderLayout.NORTH);

        // Footer Stats
        sidebar.add(buildSidebarFooter(), BorderLayout.SOUTH);

        return sidebar;
    }

    private JPanel buildNavHeader(String title) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, Theme.PAD_L, 8));
        p.setOpaque(false);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        p.add(Theme.label(title, Theme.FONT_SMALL.deriveFont(10f), Theme.TEXT_MUTED));
        return p;
    }

    private void addNavItem(JPanel parent, int idx, String icon, String label, boolean selected) {
        JPanel row = new JPanel(new BorderLayout()) {
            private boolean hovered = false;
            {
                addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) { hovered = true; repaint(); }
                    public void mouseExited(MouseEvent e) { hovered = false; repaint(); }
                    public void mouseClicked(MouseEvent e) { navigate(idx, label); }
                });
            }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean sel = (Boolean) getClientProperty("selected");

                if (sel) {
                    g2.setColor(new Color(0x1F1F27)); // subtle selection bg
                    g2.fillRoundRect(12, 0, getWidth()-24, getHeight(), 8, 8);
                } else if (hovered) {
                    g2.setColor(Theme.BG_HOVER);
                    g2.fillRoundRect(12, 0, getWidth()-24, getHeight(), 8, 8);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        row.putClientProperty("selected", selected);
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        row.setBorder(new javax.swing.border.EmptyBorder(0, Theme.PAD_L + 4, 0, Theme.PAD_L + 4));
        row.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel iconLbl = Theme.label(icon, Theme.FONT_TITLE, selected ? Theme.TEXT_PRIMARY : Theme.TEXT_SECONDARY);
        iconLbl.setPreferredSize(new Dimension(30, 40));
        
        JLabel txtLbl = Theme.label(label, Theme.FONT_BODY, selected ? Theme.TEXT_PRIMARY : Theme.TEXT_SECONDARY);

        JPanel left = new JPanel(new BorderLayout());
        left.setOpaque(false);
        left.add(iconLbl, BorderLayout.WEST);
        left.add(txtLbl, BorderLayout.CENTER);

        row.add(left, BorderLayout.CENTER);

        // Optional badge container setup
        JPanel badgeContainer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 10));
        badgeContainer.setOpaque(false);
        navBadges[idx] = badgeContainer;
        row.add(badgeContainer, BorderLayout.EAST);

        // We wrap the row in a JButton to reuse logic or just keep it as a JPanel
        // Let's store the row in the array so we can update selection state
        JButton dummyBtn = new JButton();
        dummyBtn.putClientProperty("rowPanel", row);
        dummyBtn.putClientProperty("iconLbl", iconLbl);
        dummyBtn.putClientProperty("txtLbl", txtLbl);
        navButtons[idx] = dummyBtn; 

        parent.add(row);
    }
    
    private void updateBadge(int idx, String text) {
        JPanel container = navBadges[idx];
        container.removeAll();
        if (text != null && !text.isEmpty()) {
            JPanel badge = new JPanel() {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(0x1F1F2A)); // Dark purple tinted
                    g2.fillRoundRect(0,0,getWidth(),getHeight(), 10, 10);
                    g2.dispose();
                }
            };
            badge.setOpaque(false);
            badge.setBorder(new javax.swing.border.EmptyBorder(2, 6, 2, 6));
            badge.add(Theme.label(text, Theme.FONT_SMALL, Theme.ACCENT_PURPLE));
            container.add(badge);
        }
        container.revalidate();
        container.repaint();
    }

    private JPanel buildSidebarFooter() {
        JPanel p = new JPanel(new GridLayout(1, 3)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(Theme.BORDER_SUBTLE);
                g2.drawLine(0, 0, getWidth(), 0);
                g2.dispose();
            }
        };
        p.setOpaque(false);
        p.setPreferredSize(new Dimension(0, 70));
        p.setBorder(new javax.swing.border.EmptyBorder(10, 0, 10, 0));

        usersStatLabel = Theme.label("0", Theme.FONT_SUBTITLE, Theme.TEXT_PRIMARY);
        edgesStatLabel = Theme.label("0", Theme.FONT_SUBTITLE, Theme.TEXT_PRIMARY);
        densityStatLabel = Theme.label("0%", Theme.FONT_SUBTITLE, Theme.TEXT_PRIMARY);

        p.add(buildFooterStatBox(usersStatLabel, "Users"));
        p.add(buildFooterStatBox(edgesStatLabel, "Edges"));
        p.add(buildFooterStatBox(densityStatLabel, "Density"));

        return p;
    }

    private JPanel buildFooterStatBox(JLabel valLabel, String title) {
        JPanel box = new JPanel();
        box.setOpaque(false);
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        valLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        JLabel tLbl = Theme.label(title, Theme.FONT_SMALL.deriveFont(10f), Theme.TEXT_MUTED);
        tLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        box.add(Box.createVerticalGlue());
        box.add(valLabel);
        box.add(tLbl);
        box.add(Box.createVerticalGlue());
        return box;
    }

    private JPanel buildLogo() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, Theme.PAD_L, Theme.PAD_M));
        p.setOpaque(false);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));

        JPanel iconPanel = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Icon background
                GradientPaint gp = new GradientPaint(0,0,new Color(0x281B4B),32,32,new Color(0x3B28CC));
                g2.setPaint(gp);
                g2.fillRoundRect(0,0,32,32,10,10);

                // Draw network icon
                g2.setColor(Color.WHITE);
                g2.fillOval(14, 14, 4, 4);
                g2.fillOval(8, 8, 3, 3);
                g2.fillOval(21, 8, 3, 3);
                g2.fillOval(8, 21, 3, 3);
                
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawLine(16,16, 9,9);
                g2.drawLine(16,16, 22,9);
                g2.drawLine(16,16, 9,22);

                g2.dispose();
            }
            @Override public Dimension getPreferredSize() { return new Dimension(32, 32); }
        };

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        JLabel l1 = Theme.label("SocialGraph", Theme.FONT_SUBTITLE, Theme.TEXT_PRIMARY);
        JLabel l2 = Theme.label("Mini Engine v2.0", Theme.FONT_SMALL.deriveFont(10f), Theme.TEXT_MUTED);
        text.add(l1); text.add(l2);

        p.add(iconPanel);
        p.add(text);
        return p;
    }

    // ── Content area ──────────────────────────────────────────────────────────
    private JPanel buildContent() {
        Runnable refresh = this::refreshAll;

        dashboardPanel  = new DashboardPanel(network, refresh);
        addUserPanel    = new AddUserPanel(network, refresh);
        addFriendPanel  = new AddFriendPanel(network, refresh);
        mutualPanel     = new MutualFriendsPanel(network);
        suggestPanel    = new SuggestFriendsPanel(network);
        graphPanel      = new GraphPanel(network);

        contentArea = new JPanel(new CardLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(Theme.BG_PANEL);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        contentArea.setOpaque(true);

        contentArea.add(dashboardPanel,  "0");
        contentArea.add(addUserPanel,    "1");
        contentArea.add(addFriendPanel,  "2");
        contentArea.add(mutualPanel,     "3");
        contentArea.add(suggestPanel,    "4");
        contentArea.add(graphPanel,      "5");

        return contentArea;
    }

    // ── Navigation ────────────────────────────────────────────────────────────
    public void navigate(int idx, String title) {
        // Deselect current
        JButton oldBtn = navButtons[selectedNav];
        JPanel oldRow = (JPanel) oldBtn.getClientProperty("rowPanel");
        oldRow.putClientProperty("selected", false);
        ((JLabel)oldBtn.getClientProperty("iconLbl")).setForeground(Theme.TEXT_SECONDARY);
        ((JLabel)oldBtn.getClientProperty("txtLbl")).setForeground(Theme.TEXT_SECONDARY);
        oldRow.repaint();

        selectedNav = idx;
        
        // Select new
        JButton newBtn = navButtons[idx];
        JPanel newRow = (JPanel) newBtn.getClientProperty("rowPanel");
        newRow.putClientProperty("selected", true);
        ((JLabel)newBtn.getClientProperty("iconLbl")).setForeground(Theme.TEXT_PRIMARY);
        ((JLabel)newBtn.getClientProperty("txtLbl")).setForeground(Theme.TEXT_PRIMARY);
        newRow.repaint();

        // Top bar title update
        if (topTitleLabel != null && title != null) {
            topTitleLabel.setText(title);
        }

        // Show panel
        CardLayout cl = (CardLayout) contentArea.getLayout();
        cl.show(contentArea, String.valueOf(idx));
    }

    // ── Refresh all panels that show live data ────────────────────────────────
    public void refreshAll() {
        dashboardPanel.refresh();
        addFriendPanel.refresh();
        mutualPanel.refresh();
        graphPanel.refresh();
        
        refreshShellStats();
    }

    public void refreshShellStats() {
        Map<String, Object> stats = network.getStats();
        
        // Update Bottom Stats
        if(usersStatLabel != null) usersStatLabel.setText(stats.get("users").toString());
        if(edgesStatLabel != null) edgesStatLabel.setText(stats.get("edges").toString());
        if(densityStatLabel != null) densityStatLabel.setText(stats.get("density").toString() + "%");
        
        // Update Badges
        updateBadge(1, stats.get("users").toString());
        // Suggestion badge... roughly simulate or keep static for this example.
        updateBadge(4, "4"); 
    }

    // ── App icon ──────────────────────────────────────────────────────────────
    private Image createAppIcon() {
        int sz = 64;
        java.awt.image.BufferedImage img =
                new java.awt.image.BufferedImage(sz, sz, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        GradientPaint gp = new GradientPaint(0,0,new Color(0x3B28CC),sz,sz,new Color(0x9B7BFF));
        g2.setPaint(gp);
        g2.fillRoundRect(0,0,sz,sz,16,16);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Segoe UI", Font.BOLD, 28));
        g2.drawString("SG", 10, 42);
        g2.dispose();
        return img;
    }

    // ── Entry point ───────────────────────────────────────────────────────────
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {}

        // Global defaults
        UIManager.put("Panel.background",          Theme.BG_DARK);
        UIManager.put("OptionPane.background",     Theme.BG_DARK);
        UIManager.put("Label.foreground",          Theme.TEXT_PRIMARY);
        UIManager.put("TextField.background",      Theme.BG_INPUT);
        UIManager.put("TextField.foreground",      Theme.TEXT_PRIMARY);
        UIManager.put("TextField.caretForeground", Theme.ACCENT_BLUE);
        UIManager.put("ScrollPane.background",     Theme.BG_DARK);
        UIManager.put("Viewport.background",       Theme.BG_DARK);

        SwingUtilities.invokeLater(MainWindow::new);
    }
}
