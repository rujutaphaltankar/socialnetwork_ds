import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Map;

/**
 * Enhanced Dashboard Panel according to modern redesign.
 */
public class DashboardPanel extends JPanel {

    private final SocialNetwork network;
    private final Runnable refreshCallback;

    // Stat labels
    private JLabel usersCountLabel;
    private JLabel connCountLabel;
    private JLabel avgConnLabel;
    private JLabel suggMadeLabel;
    private int suggestionsCount = 4; // Mock value for display

    // Feed / Lists
    private JPanel connectedUsersList;
    private JPanel activityFeedList;

    public DashboardPanel(SocialNetwork network, Runnable refreshCallback) {
        this.network = network;
        this.refreshCallback = refreshCallback;
        
        setOpaque(false);
        setLayout(new BorderLayout());

        JPanel mainContent = new JPanel();
        mainContent.setLayout(new BoxLayout(mainContent, BoxLayout.Y_AXIS));
        mainContent.setOpaque(false);
        mainContent.setBorder(new javax.swing.border.EmptyBorder(Theme.PAD_L, Theme.PAD_XL, Theme.PAD_XL, Theme.PAD_XL));

        mainContent.add(buildStatsRow());
        mainContent.add(Box.createVerticalStrut(Theme.PAD_L));
        mainContent.add(buildToolsGrid());
        mainContent.add(Box.createVerticalStrut(Theme.PAD_L));
        mainContent.add(buildBottomRow());

        JScrollPane scroll = Theme.scrollPane(mainContent);
        add(scroll, BorderLayout.CENTER);
    }

    // ── Components ────────────────────────────────────────────────────────────

    private JPanel buildStatsRow() {
        JPanel row = new JPanel(new GridLayout(1, 4, Theme.PAD_L, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        usersCountLabel = Theme.label("0", Theme.FONT_STAT, Theme.TEXT_PRIMARY);
        connCountLabel  = Theme.label("0", Theme.FONT_STAT, Theme.TEXT_PRIMARY);
        avgConnLabel    = Theme.label("0.0", Theme.FONT_STAT, Theme.TEXT_PRIMARY);
        suggMadeLabel   = Theme.label(String.valueOf(suggestionsCount), Theme.FONT_STAT, Theme.TEXT_PRIMARY);

        row.add(buildStatCard("Total users", usersCountLabel, "+2 this week", Theme.TEXT_SUCCESS));
        row.add(buildStatCard("Friendships", connCountLabel, "+3 this week", Theme.TEXT_SUCCESS));
        row.add(buildStatCard("Avg. connections", avgConnLabel, "+0.4 this week", Theme.TEXT_SUCCESS));
        row.add(buildStatCard("Suggestions made", suggMadeLabel, "-1 vs last week", Theme.TEXT_ERROR));

        refreshStats();
        return row;
    }

    private JPanel buildStatCard(String title, JLabel valueLabel, String deltaText, Color deltaColor) {
        JPanel card = Theme.card();
        card.setLayout(new BorderLayout());
        
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);

        JLabel tLabel = Theme.label(title, Theme.FONT_SMALL, Theme.TEXT_MUTED);
        JLabel dLabel = Theme.label(deltaText, Theme.FONT_SMALL.deriveFont(11f), deltaColor);

        // Decorator bar at the bottom
        JPanel decorator = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                if(deltaColor == Theme.TEXT_SUCCESS) {
                    g2.setPaint(new GradientPaint(0,0,Theme.ACCENT_TEAL,getWidth(),0,new Color(0x1877F2)));
                } else {
                    g2.setPaint(new GradientPaint(0,0,Theme.ACCENT_PINK,getWidth(),0,Theme.TEXT_ERROR));
                }
                g2.fillRoundRect(0,0,24,3,3,3);
                g2.dispose();
            }
        };
        decorator.setOpaque(false);
        decorator.setPreferredSize(new Dimension(100, 10));

        p.add(tLabel);
        p.add(Box.createVerticalStrut(8));
        p.add(valueLabel);
        p.add(Box.createVerticalStrut(4));
        p.add(dLabel);
        p.add(Box.createVerticalStrut(8));
        p.add(decorator);

        card.add(p, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildToolsGrid() {
        JPanel grid = new JPanel(new GridLayout(2, 2, Theme.PAD_L, Theme.PAD_L));
        grid.setOpaque(false);
        grid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 700));

        grid.add(buildAddUserCard());
        grid.add(buildAddFriendshipCard());
        grid.add(buildMutualFriendsCard());
        grid.add(buildSuggestFriendsCard());

        return grid;
    }

    // ── Tool Cards ────────────────────────────────────────────────────────────

    private JPanel buildAddUserCard() {
        JPanel card = Theme.card();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(Theme.label("Add user", Theme.FONT_SUBTITLE, Theme.TEXT_PRIMARY), BorderLayout.WEST);
        header.add(Theme.badge("Hash Table", new Color(0x1F1F2A), Theme.ACCENT_PURPLE), BorderLayout.EAST);
        
        card.add(header);
        card.add(Box.createVerticalStrut(Theme.PAD_M));

        JLabel l1 = Theme.label("FULL NAME", Theme.FONT_SMALL.deriveFont(10f), Theme.TEXT_MUTED);
        JTextField nameField = Theme.textField("e.g. Maya Patel");
        nameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        JLabel l2 = Theme.label("USERNAME", Theme.FONT_SMALL.deriveFont(10f), Theme.TEXT_MUTED);
        JTextField usernameField = Theme.textField("@username");
        usernameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        JButton addBtn = Theme.secondaryButton("Add to network");
        addBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        addBtn.addActionListener(e -> {
            String uname = usernameField.getText().replace("@", "").trim();
            if(!uname.isEmpty() && !uname.equals("username")) {
                network.addUser(uname); // Add user creates dummy user for now without full name
                refreshCallback.run();
                usernameField.setText("");
                nameField.setText("");
            }
        });

        l1.setAlignmentX(Component.LEFT_ALIGNMENT);
        nameField.setAlignmentX(Component.LEFT_ALIGNMENT);
        l2.setAlignmentX(Component.LEFT_ALIGNMENT);
        usernameField.setAlignmentX(Component.LEFT_ALIGNMENT);
        addBtn.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(l1); card.add(Box.createVerticalStrut(4)); card.add(nameField);
        card.add(Box.createVerticalStrut(Theme.PAD_M));
        card.add(l2); card.add(Box.createVerticalStrut(4)); card.add(usernameField);
        card.add(Box.createVerticalStrut(Theme.PAD_L));
        card.add(addBtn);
        
        return card;
    }

    private JPanel buildAddFriendshipCard() {
        JPanel card = Theme.card();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(Theme.label("Add friendship", Theme.FONT_SUBTITLE, Theme.TEXT_PRIMARY), BorderLayout.WEST);
        header.add(Theme.badge("Graph edge", new Color(0x1A2536), Theme.ACCENT_BLUE), BorderLayout.EAST);
        
        card.add(header);
        card.add(Box.createVerticalStrut(Theme.PAD_M));

        JLabel l1 = Theme.label("USER A", Theme.FONT_SMALL.deriveFont(10f), Theme.TEXT_MUTED);
        JTextField userAField = Theme.textField("Alice");
        userAField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        JLabel l2 = Theme.label("USER B", Theme.FONT_SMALL.deriveFont(10f), Theme.TEXT_MUTED);
        JTextField userBField = Theme.textField("Bob");
        userBField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        JPanel btnRow = new JPanel(new GridLayout(1, 2, Theme.PAD_M, 0));
        btnRow.setOpaque(false);
        btnRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        
        JButton clearBtn = Theme.secondaryButton("Clear");
        JButton connBtn = Theme.primaryButton("Connect");

        connBtn.addActionListener(e -> {
            String uA = userAField.getText().trim();
            String uB = userBField.getText().trim();
            if(!uA.isEmpty() && !uA.equals("Alice") && !uB.isEmpty() && !uB.equals("Bob")) {
                network.addFriend(uA, uB);
                refreshCallback.run();
                userAField.setText("");
                userBField.setText("");
            }
        });

        clearBtn.addActionListener(e -> {
            userAField.setText("");
            userBField.setText("");
        });

        btnRow.add(clearBtn);
        btnRow.add(connBtn);

        l1.setAlignmentX(Component.LEFT_ALIGNMENT);
        userAField.setAlignmentX(Component.LEFT_ALIGNMENT);
        l2.setAlignmentX(Component.LEFT_ALIGNMENT);
        userBField.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        card.add(l1); card.add(Box.createVerticalStrut(4)); card.add(userAField);
        card.add(Box.createVerticalStrut(Theme.PAD_S));
        card.add(l2); card.add(Box.createVerticalStrut(4)); card.add(userBField);
        card.add(Box.createVerticalStrut(Theme.PAD_L));
        card.add(btnRow);

        return card;
    }

    private JPanel buildMutualFriendsCard() {
        JPanel card = Theme.card();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(Theme.label("Find mutual friends", Theme.FONT_SUBTITLE, Theme.TEXT_PRIMARY), BorderLayout.WEST);
        header.add(Theme.badge("BFS · Queue", new Color(0x1B2A26), Theme.ACCENT_TEAL), BorderLayout.EAST);
        
        card.add(header);
        card.add(Box.createVerticalStrut(Theme.PAD_M));

        JLabel l1 = Theme.label("BETWEEN", Theme.FONT_SMALL.deriveFont(10f), Theme.TEXT_MUTED);
        
        JPanel inputRow = new JPanel(new BorderLayout(8, 0));
        inputRow.setOpaque(false);
        inputRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        
        JTextField t1 = Theme.textField("Alice");
        JTextField t2 = Theme.textField("Eve");
        JLabel dash = Theme.label("—", Theme.FONT_BODY, Theme.TEXT_MUTED);
        
        inputRow.add(t1, BorderLayout.WEST);
        inputRow.add(dash, BorderLayout.CENTER);
        inputRow.add(t2, BorderLayout.EAST);
        t1.setPreferredSize(new Dimension(100, 38));
        t2.setPreferredSize(new Dimension(100, 38));

        JButton findBtn = Theme.primaryButton("Find mutuals", Theme.ACCENT_TEAL, new Color(0x138A66));
        findBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        
        JPanel resultsArea = new JPanel();
        resultsArea.setOpaque(false);
        resultsArea.setLayout(new BoxLayout(resultsArea, BoxLayout.Y_AXIS));
        resultsArea.setAlignmentX(Component.LEFT_ALIGNMENT);

        findBtn.addActionListener(e -> {
            resultsArea.removeAll();
            String u1 = t1.getText().trim();
            String u2 = t2.getText().trim();
            if (!u1.isEmpty() && !u2.isEmpty() && !u1.equals("Alice")) {
                List<User> mutuals = network.findMutualFriends(u1, u2);
                resultsArea.add(Theme.label(mutuals.size() + " mutual friends found", Theme.FONT_SMALL, Theme.TEXT_MUTED));
                resultsArea.add(Box.createVerticalStrut(8));
                
                JPanel resGrid = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
                resGrid.setOpaque(false);
                
                for(User m : mutuals) {
                    JPanel chip = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4)) {
                        @Override protected void paintComponent(Graphics g) {
                            Graphics2D g2 = (Graphics2D) g.create();
                            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                            g2.setColor(Theme.BG_INPUT);
                            g2.fillRoundRect(0,0,getWidth(),getHeight(), 12,12);
                        }
                    };
                    chip.setOpaque(false);
                    JLabel initial = Theme.label(m.getDisplayName().substring(0,1), Theme.FONT_SMALL.deriveFont(10f), Color.WHITE);
                    JPanel circle = new JPanel(){
                        @Override protected void paintComponent(Graphics g) {
                            Graphics2D g2 = (Graphics2D) g.create();
                            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                            g2.setColor(Theme.ACCENT_BLUE);
                            g2.fillOval(0,0,16,16);
                        }
                        @Override public Dimension getPreferredSize() { return new Dimension(16,16); }
                    };
                    circle.setOpaque(false);
                    circle.add(initial);
                    chip.add(circle);
                    chip.add(Theme.label(m.getDisplayName(), Theme.FONT_SMALL, Theme.TEXT_SECONDARY));
                    resGrid.add(chip);
                }
                resultsArea.add(resGrid);
            }
            resultsArea.revalidate();
            resultsArea.repaint();
        });

        l1.setAlignmentX(Component.LEFT_ALIGNMENT);
        inputRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        findBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        resultsArea.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(l1); card.add(Box.createVerticalStrut(4)); card.add(inputRow);
        card.add(Box.createVerticalStrut(Theme.PAD_S));
        card.add(findBtn);
        card.add(Box.createVerticalStrut(Theme.PAD_M));
        
        // Wrap results in a subtle bounded box
        JPanel resultsBox = new JPanel(new BorderLayout());
        resultsBox.setOpaque(false);
        resultsBox.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.BORDER_SUBTLE, 1),
            new javax.swing.border.EmptyBorder(12, 12, 12, 12)
        ));
        resultsBox.add(resultsArea, BorderLayout.CENTER);
        resultsBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        
        card.add(resultsBox);

        return card;
    }

    private JPanel buildSuggestFriendsCard() {
        JPanel card = Theme.card();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(Theme.label("Suggest friends", Theme.FONT_SUBTITLE, Theme.TEXT_PRIMARY), BorderLayout.WEST);
        header.add(Theme.badge("Heap · priority", new Color(0x332819), Theme.ACCENT_ORANGE), BorderLayout.EAST);
        
        card.add(header);
        card.add(Box.createVerticalStrut(Theme.PAD_M));

        JLabel l1 = Theme.label("SUGGEST FOR", Theme.FONT_SMALL.deriveFont(10f), Theme.TEXT_MUTED);
        JTextField targetField = Theme.textField("Select user...");
        targetField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        JButton suggestBtn = Theme.primaryButton("Run suggestions", new Color(0xBD6D00), new Color(0x8C5200));
        suggestBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        JPanel resultsGrid = new JPanel(new GridLayout(2, 2, 8, 8));
        resultsGrid.setOpaque(false);
        resultsGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));

        suggestBtn.addActionListener(e -> {
            resultsGrid.removeAll();
            String u = targetField.getText().trim();
            if(!u.isEmpty() && !u.equals("Select user...")) {
                List<SocialNetwork.SuggestionEntry> suggestions = network.suggestFriends(u, 4);
                suggestionsCount++;
                refreshStats();

                for(SocialNetwork.SuggestionEntry val : suggestions) {
                    resultsGrid.add(buildSuggestionItemCard(val.getUser().getDisplayName(), val.getMutualCount()));
                }
            }
            resultsGrid.revalidate();
            resultsGrid.repaint();
        });

        l1.setAlignmentX(Component.LEFT_ALIGNMENT);
        targetField.setAlignmentX(Component.LEFT_ALIGNMENT);
        suggestBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        resultsGrid.setAlignmentX(Component.LEFT_ALIGNMENT);

        card.add(l1); card.add(Box.createVerticalStrut(4)); card.add(targetField);
        card.add(Box.createVerticalStrut(Theme.PAD_S));
        card.add(suggestBtn);
        card.add(Box.createVerticalStrut(Theme.PAD_M));
        card.add(resultsGrid);

        return card;
    }

    private JPanel buildSuggestionItemCard(String name, int mutuals) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Theme.BORDER_SUBTLE, 1),
            new javax.swing.border.EmptyBorder(12, 8, 12, 8)
        ));

        // Big avatar
        JPanel avatar = new JPanel(){
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0x28283E));
                g2.fillOval(0,0,32,32);
                g2.setColor(Theme.ACCENT_PURPLE);
                g2.setFont(Theme.FONT_BODY.deriveFont(Font.BOLD, 12f));
                String inits = name.substring(0, Math.min(2, name.length())).toUpperCase();
                FontMetrics fm = g2.getFontMetrics();
                int x = (32 - fm.stringWidth(inits)) / 2;
                int y = (32 - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(inits, x, y);
                g2.dispose();
            }
            @Override public Dimension getPreferredSize() { return new Dimension(32, 32); }
            @Override public Dimension getMaximumSize() { return new Dimension(32, 32); }
        };
        avatar.setOpaque(false);
        avatar.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel nameLbl = Theme.label(name, Theme.FONT_BODY.deriveFont(Font.BOLD), Theme.TEXT_PRIMARY);
        nameLbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel mutLbl = Theme.label(mutuals + " mutuals", Theme.FONT_SMALL, Theme.TEXT_MUTED);
        mutLbl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton connectBtn = Theme.secondaryButton("+ Connect");
        connectBtn.setFont(Theme.FONT_SMALL);
        connectBtn.setPreferredSize(new Dimension(80, 24));
        connectBtn.setMaximumSize(new Dimension(100, 28));
        connectBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

        p.add(avatar);
        p.add(Box.createVerticalStrut(8));
        p.add(nameLbl);
        p.add(mutLbl);
        p.add(Box.createVerticalStrut(12));
        p.add(connectBtn);
        return p;
    }

    // ── Bottom Lists ──────────────────────────────────────────────────────────

    private JPanel buildBottomRow() {
        JPanel row = new JPanel(new GridLayout(1, 2, Theme.PAD_L, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 400));

        // Connected users panel
        JPanel usersPanel = Theme.card();
        usersPanel.setLayout(new BorderLayout());
        JPanel uh = new JPanel(new BorderLayout());
        uh.setOpaque(false);
        uh.add(Theme.label("Connected users", Theme.FONT_SUBTITLE, Theme.TEXT_PRIMARY), BorderLayout.WEST);
        uh.add(Theme.label("View all →", Theme.FONT_SMALL, Theme.ACCENT_PURPLE), BorderLayout.EAST);
        uh.setBorder(new javax.swing.border.EmptyBorder(0,0,12,0));
        usersPanel.add(uh, BorderLayout.NORTH);

        connectedUsersList = new JPanel();
        connectedUsersList.setOpaque(false);
        connectedUsersList.setLayout(new BoxLayout(connectedUsersList, BoxLayout.Y_AXIS));
        usersPanel.add(Theme.scrollPane(connectedUsersList), BorderLayout.CENTER);

        // Activity feed panel
        JPanel activityPanel = Theme.card();
        activityPanel.setLayout(new BorderLayout());
        JPanel ah = new JPanel(new BorderLayout());
        ah.setOpaque(false);
        ah.add(Theme.label("Activity feed", Theme.FONT_SUBTITLE, Theme.TEXT_PRIMARY), BorderLayout.WEST);
        ah.add(Theme.label("See all →", Theme.FONT_SMALL, Theme.ACCENT_PURPLE), BorderLayout.EAST);
        ah.setBorder(new javax.swing.border.EmptyBorder(0,0,12,0));
        activityPanel.add(ah, BorderLayout.NORTH);

        activityFeedList = new JPanel();
        activityFeedList.setOpaque(false);
        activityFeedList.setLayout(new BoxLayout(activityFeedList, BoxLayout.Y_AXIS));
        activityPanel.add(Theme.scrollPane(activityFeedList), BorderLayout.CENTER);

        row.add(usersPanel);
        row.add(activityPanel);

        refreshLists();
        return row;
    }

    private void refreshLists() {
        if(connectedUsersList == null) return;
        connectedUsersList.removeAll();
        java.util.List<User> users = network.getAllUsers();
        for(User u : users) {
             JPanel row = new JPanel(new BorderLayout());
             row.setOpaque(false);
             row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
             row.setBorder(BorderFactory.createCompoundBorder(
                 BorderFactory.createLineBorder(Theme.BORDER_SUBTLE, 1),
                 new javax.swing.border.EmptyBorder(8, 12, 8, 12)
             ));

             JPanel avatar = new JPanel(){
                 @Override protected void paintComponent(Graphics g) {
                     Graphics2D g2 = (Graphics2D) g.create();
                     g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                     g2.setColor(new Color(0x212130));
                     g2.fillOval(0,0,32,32);
                     g2.setColor(Theme.ACCENT_PURPLE);
                     g2.setFont(Theme.FONT_BODY.deriveFont(Font.BOLD, 12f));
                     String nameStr = u.getDisplayName();
                     String inits = nameStr.substring(0, Math.min(2, nameStr.length())).toUpperCase();
                     FontMetrics fm = g2.getFontMetrics();
                     int x = (32 - fm.stringWidth(inits)) / 2;
                     int y = (32 - fm.getHeight()) / 2 + fm.getAscent();
                     g2.drawString(inits, x, y);
                     g2.dispose();
                 }
                 @Override public Dimension getPreferredSize() { return new Dimension(32, 32); }
             };
             avatar.setOpaque(false);

             JPanel info = new JPanel();
             info.setOpaque(false);
             info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
             info.setBorder(new javax.swing.border.EmptyBorder(0, 12, 0, 0));
             info.add(Theme.label(u.getDisplayName(), Theme.FONT_BODY.deriveFont(Font.BOLD), Theme.TEXT_PRIMARY));
             info.add(Theme.label(u.getFriendCount() + " friends · active", Theme.FONT_SMALL, Theme.TEXT_MUTED));

             JPanel badgeHolder = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 8));
             badgeHolder.setOpaque(false);
             if (u.getFriendCount() > 2) {
                 badgeHolder.add(Theme.badge("hub", new Color(0x1F1F2A), Theme.ACCENT_PURPLE));
             } else {
                 badgeHolder.add(Theme.badge("+", new Color(0x1F1F2A), Theme.TEXT_SECONDARY));
             }

             row.add(avatar, BorderLayout.WEST);
             row.add(info, BorderLayout.CENTER);
             row.add(badgeHolder, BorderLayout.EAST);
             
             connectedUsersList.add(row);
             connectedUsersList.add(Box.createVerticalStrut(8));
        }

        connectedUsersList.revalidate();
        connectedUsersList.repaint();

        if(activityFeedList == null) return;
        activityFeedList.removeAll();
        java.util.List<String> logs = network.getActivityLog();
        int i=0;
        for(String lg : logs) {
            JPanel row = new JPanel(new BorderLayout());
            row.setOpaque(false);
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            
            Color dotColor = (i%3==0) ? Theme.ACCENT_PURPLE : (i%2==0) ? Theme.ACCENT_BLUE : Theme.TEXT_SUCCESS;
            if(lg.contains("joined")) dotColor = Theme.ACCENT_BLUE;
            if(lg.contains("friends")) dotColor = Theme.TEXT_SUCCESS;
            
            final Color clr = dotColor;

            JPanel dot = new JPanel(){
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(clr);
                    g2.fillOval(0,4,8,8);
                }
                @Override public Dimension getPreferredSize() { return new Dimension(16, 16); }
            };
            dot.setOpaque(false);

            JPanel info = new JPanel();
            info.setOpaque(false);
            info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
            info.add(Theme.label(lg.replace("✦ ", "").replace("⟷ ", ""), Theme.FONT_SMALL, Theme.TEXT_SECONDARY));
            info.add(Theme.label("Just now", Theme.FONT_SMALL.deriveFont(9f), Theme.TEXT_MUTED));

            row.add(dot, BorderLayout.WEST);
            row.add(info, BorderLayout.CENTER);
            
            activityFeedList.add(row);
            activityFeedList.add(Box.createVerticalStrut(12));
            i++;
        }

        activityFeedList.revalidate();
        activityFeedList.repaint();
    }

    private void refreshStats() {
        if(usersCountLabel == null) return;
        Map<String, Object> stats = network.getStats();
        usersCountLabel.setText(stats.get("users").toString());
        connCountLabel.setText(stats.get("edges").toString());
        avgConnLabel.setText(stats.get("avgDeg").toString());
        suggMadeLabel.setText(String.valueOf(suggestionsCount));
    }

    public void refresh() {
        refreshStats();
        refreshLists();
    }
}
