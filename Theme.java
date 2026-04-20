import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Centralised theme / design system for the Social Network UI.
 * Modern dark theme with sophisticated gradients and subtle shadows.
 */
public class Theme {

    // ── Enhanced Colour Palette ───────────────────────────────────────────────
    public static final Color BG_DARKEST    = new Color(0x0B0B0E);
    public static final Color BG_DARK       = new Color(0x111114);
    public static final Color BG_CARD       = new Color(0x151518);
    public static final Color BG_PANEL      = new Color(0x111114);
    public static final Color BG_INPUT      = new Color(0x202026);
    public static final Color BG_SIDEBAR    = new Color(0x0B0B0E);
    public static final Color BG_HOVER      = new Color(0x1A1A22);

    public static final Color ACCENT_BLUE   = new Color(0x1877F2); // 0x4F8BF9
    public static final Color ACCENT_PURPLE = new Color(0x3B28CC); // 0x9B7BFF
    public static final Color ACCENT_CYAN   = new Color(0x00, 0xD4, 0xAA);
    public static final Color ACCENT_PINK   = new Color(0xFF, 0x6B, 0x9D);
    public static final Color ACCENT_ORANGE = new Color(0xFF, 0xB3, 0x47);
    public static final Color ACCENT_TEAL   = new Color(0x20, 0xC9, 0x97);

    public static final Color TEXT_PRIMARY   = new Color(0xF8F8FA);
    public static final Color TEXT_SECONDARY = new Color(0xB8B8C8);
    public static final Color TEXT_MUTED     = new Color(0x787888);
    public static final Color TEXT_SUCCESS   = new Color(0x43E089);
    public static final Color TEXT_ERROR     = new Color(0xFF6B9D);
    public static final Color TEXT_WARNING   = new Color(0xFFB347);

    public static final Color BORDER_SUBTLE  = new Color(0x23, 0x23, 0x2A);
    public static final Color BORDER_ACCENT  = new Color(0x33, 0x33, 0x40);
    public static final Color BORDER_FOCUS   = new Color(0x4F, 0x8B, 0xF9);
    public static final Color ACCENT_GLOW    = new Color(0x9B, 0x7B, 0xFF, 180);

    // Shadow colors for depth
    public static final Color SHADOW_LIGHT   = new Color(0x00, 0x00, 0x00, 20);
    public static final Color SHADOW_MEDIUM  = new Color(0x00, 0x00, 0x00, 40);
    public static final Color SHADOW_HEAVY   = new Color(0x00, 0x00, 0x00, 60);

    // ── Enhanced Typography ──────────────────────────────────────────────────
    public static final Font FONT_TITLE    = new Font("Inter", Font.BOLD,  20);
    public static final Font FONT_SUBTITLE = new Font("Inter", Font.BOLD,  15);
    public static final Font FONT_BODY     = new Font("Inter", Font.PLAIN, 13);
    public static final Font FONT_SMALL    = new Font("Inter", Font.PLAIN, 12);
    public static final Font FONT_MONO     = new Font("Consolas",  Font.PLAIN, 13);
    public static final Font FONT_NAV      = new Font("Inter", Font.BOLD,  13);
    public static final Font FONT_LABEL    = new Font("Inter", Font.PLAIN, 12);
    public static final Font FONT_STAT     = new Font("Inter", Font.BOLD,  28);
    public static final Font FONT_BUTTON   = new Font("Inter", Font.BOLD,  13);

    // ── Enhanced Spacing ─────────────────────────────────────────────────────
    public static final int PAD_XS = 6;
    public static final int PAD_S  = 10;
    public static final int PAD_M  = 16;
    public static final int PAD_L  = 24;
    public static final int PAD_XL = 32;
    public static final int PAD_XXL = 48;

    // ── Gradient paint helper ────────────────────────────────────────────────
    public static GradientPaint accentGradient(int x, int y, int width) {
        return new GradientPaint(x, y, ACCENT_BLUE, x + width, y, ACCENT_PURPLE);
    }

    // ── Factory: styled JLabel ───────────────────────────────────────────────
    public static JLabel label(String text, Font font, Color color) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(font);
        lbl.setForeground(color);
        lbl.setOpaque(false);
        return lbl;
    }

    // ── Factory: badge ───────────────────────────────────────────────────────
    public static JPanel badge(String text, Color bg, Color fg) {
        JPanel p = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        p.setOpaque(false);
        p.setLayout(new BorderLayout());
        p.setBorder(new EmptyBorder(3, 8, 3, 8));
        JLabel l = label(text, FONT_SMALL, fg);
        p.add(l, BorderLayout.CENTER);
        return p;
    }

    // ── Factory: enhanced styled JTextField ──────────────────────────────────
    public static JTextField textField(String placeholder) {
        JTextField field = new JTextField() {
            private boolean focused = false;

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Background with subtle shadow
                if (focused) {
                    g2.setColor(BG_INPUT);
                    g2.fillRoundRect(1, 1, getWidth()-2, getHeight()-2, 8, 8);
                    // Focus glow
                    g2.setColor(new Color(BORDER_FOCUS.getRed(), BORDER_FOCUS.getGreen(), BORDER_FOCUS.getBlue(), 60));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                } else {
                    g2.setColor(BG_INPUT);
                    g2.fillRoundRect(1, 1, getWidth()-2, getHeight()-2, 8, 8);
                }

                // Border
                g2.setColor(focused ? BORDER_FOCUS : BORDER_SUBTLE);
                g2.setStroke(new BasicStroke(focused ? 2f : 1f));
                g2.drawRoundRect(focused ? 1 : 0, focused ? 1 : 0,
                               getWidth() - (focused ? 2 : 1), getHeight() - (focused ? 2 : 1), 8, 8);

                g2.dispose();
                super.paintComponent(g);
            }
        };
        field.setOpaque(false);
        field.setFont(FONT_BODY);
        field.setForeground(TEXT_PRIMARY);
        field.setCaretColor(ACCENT_BLUE);
        field.setSelectionColor(new Color(0x4F, 0x8B, 0xF9, 100));
        field.setSelectedTextColor(Color.WHITE);
        field.setBorder(new EmptyBorder(10, 14, 10, 14));
        field.setPreferredSize(new Dimension(220, 38));

        // Enhanced placeholder handling
        field.putClientProperty("placeholder", placeholder);
        field.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                field.repaint();
                if (field.getText().equals(placeholder)) {
                    field.setText("");
                    field.setForeground(TEXT_PRIMARY);
                }
            }
            @Override public void focusLost(FocusEvent e) {
                field.repaint();
                if (field.getText().isBlank()) {
                    field.setText(placeholder);
                    field.setForeground(TEXT_MUTED);
                }
            }
        });
        field.setText(placeholder);
        field.setForeground(TEXT_MUTED);
        return field;
    }

    // ── Factory: enhanced primary button ─────────────────────────────────────
    public static JButton primaryButton(String text) {
        return new ModernGradientButton(text, new Color(0x1877F2), new Color(0x3B28CC), true);
    }
    
    public static JButton primaryButton(String text, Color c1, Color c2) {
        return new ModernGradientButton(text, c1, c2, true);
    }

    public static JButton secondaryButton(String text) {
        return new ModernGradientButton(text, BG_INPUT, BG_INPUT, false);
    }

    // ── Factory: enhanced styled JComboBox ───────────────────────────────────
    public static JComboBox<String> comboBox() {
        JComboBox<String> combo = new JComboBox<String>() {
            private boolean focused = false;

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Background
                g2.setColor(BG_INPUT);
                g2.fillRoundRect(1, 1, getWidth()-2, getHeight()-2, 8, 8);

                // Focus glow
                if (focused) {
                    g2.setColor(new Color(BORDER_FOCUS.getRed(), BORDER_FOCUS.getGreen(), BORDER_FOCUS.getBlue(), 60));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                }

                // Border
                g2.setColor(focused ? BORDER_FOCUS : BORDER_SUBTLE);
                g2.setStroke(new BasicStroke(focused ? 2f : 1f));
                g2.drawRoundRect(focused ? 1 : 0, focused ? 1 : 0,
                               getWidth() - (focused ? 2 : 1), getHeight() - (focused ? 2 : 1), 8, 8);

                g2.dispose();
                super.paintComponent(g);
            }
        };

        combo.setOpaque(false);
        combo.setFont(FONT_BODY);
        combo.setForeground(TEXT_PRIMARY);
        combo.setBackground(BG_INPUT);
        combo.setBorder(new EmptyBorder(10, 14, 10, 14));
        combo.setPreferredSize(new Dimension(220, 38));

        // Focus listener for visual feedback
        combo.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { combo.repaint(); }
            @Override public void focusLost(FocusEvent e) { combo.repaint(); }
        });

        // Custom renderer for better styling
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setFont(FONT_BODY);
                setForeground(isSelected ? Color.WHITE : TEXT_PRIMARY);
                setBackground(isSelected ? BORDER_FOCUS : BG_INPUT);
                setBorder(new EmptyBorder(8, 16, 8, 16));
                return this;
            }
        });

        return combo;
    }

    // ── Factory: enhanced card panel with shadows ────────────────────────────
    public static JPanel card() {
        JPanel panel = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Card background
                g2.setColor(BG_CARD);
                g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, 16, 16);

                // Subtle border
                g2.setColor(BORDER_SUBTLE);
                g2.setStroke(new BasicStroke(1f));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 16, 16);

                g2.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(PAD_M, PAD_M, PAD_M, PAD_M));
        return panel;
    }

    // ── Factory: section title ────────────────────────────────────────────────
    public static JPanel sectionTitle(String title, String subtitle) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        JLabel t = label(title, FONT_TITLE, TEXT_PRIMARY);
        t.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(t);
        if (subtitle != null && !subtitle.isEmpty()) {
            JLabel s = label(subtitle, FONT_BODY, TEXT_SECONDARY);
            s.setAlignmentX(Component.LEFT_ALIGNMENT);
            p.add(Box.createVerticalStrut(4));
            p.add(s);
        }
        return p;
    }

    // ── Scroll pane styling ───────────────────────────────────────────────────
    public static JScrollPane scrollPane(Component view) {
        JScrollPane sp = new JScrollPane(view);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getVerticalScrollBar().setUnitIncrement(12);
        sp.getVerticalScrollBar().setUI(new DarkScrollBarUI());
        sp.getHorizontalScrollBar().setVisible(false);
        return sp;
    }

    // ── Inner: Modern Gradient Button ────────────────────────────────────────
    public static class ModernGradientButton extends JButton {
        private final Color c1, c2;
        private final boolean isGradient;
        private boolean hovered = false;
        private boolean pressed = false;

        public ModernGradientButton(String text, Color c1, Color c2, boolean isGradient) {
            super(text);
            this.c1 = c1; this.c2 = c2;
            this.isGradient = isGradient;
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setFont(FONT_BUTTON);
            setForeground(Color.WHITE);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(10, 20, 10, 20));
            addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e) { hovered = true;  repaint(); }
                public void mouseExited(MouseEvent e)  { hovered = false; repaint(); }
                public void mousePressed(MouseEvent e)  { pressed = true;  repaint(); }
                public void mouseReleased(MouseEvent e) { pressed = false; repaint(); }
            });
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Color bg1 = pressed ? c1.darker() : hovered ? c1.brighter() : c1;
            Color bg2 = pressed ? c2.darker() : hovered ? c2.brighter() : c2;
            
            if (isGradient) {
                GradientPaint gp = new GradientPaint(0, 0, bg1, getWidth(), 0, bg2);
                g2.setPaint(gp);
            } else {
                g2.setColor(bg1);
            }
            g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, 8, 8);

            if (!isGradient) {
                g2.setColor(Theme.BORDER_SUBTLE);
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 8, 8);
            }

            g2.dispose();
            super.paintComponent(g);
        }
    }

    // ── Inner: Placeholder focus listener ────────────────────────────────────
    public static class PlaceholderFocusListener extends FocusAdapter {
        private final JTextField field;
        private final String     placeholder;

        public PlaceholderFocusListener(JTextField field, String placeholder) {
            this.field = field;
            this.placeholder = placeholder;
        }

        @Override public void focusGained(FocusEvent e) {
            if (field.getText().equals(placeholder)) {
                field.setText("");
                field.setForeground(TEXT_PRIMARY);
            }
        }

        @Override public void focusLost(FocusEvent e) {
            if (field.getText().isBlank()) {
                field.setText(placeholder);
                field.setForeground(TEXT_MUTED);
            }
        }
    }

    // ── Inner: Dark scroll bar UI ─────────────────────────────────────────────
    public static class DarkScrollBarUI extends javax.swing.plaf.basic.BasicScrollBarUI {
        @Override protected void configureScrollBarColors() {
            thumbColor      = BORDER_SUBTLE;
            trackColor      = BG_DARK;
        }
        @Override protected JButton createDecreaseButton(int o) { return zeroButton(); }
        @Override protected JButton createIncreaseButton(int o) { return zeroButton(); }
        private JButton zeroButton() {
            JButton b = new JButton();
            b.setPreferredSize(new Dimension(0, 0));
            return b;
        }
        @Override protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(thumbColor);
            g2.fillRoundRect(r.x+2, r.y+2, r.width-4, r.height-4, 6, 6);
        }
    }
}
