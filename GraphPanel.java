import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Graph visualization panel.
 * Displays users as nodes and friendships as edges.
 */
public class GraphPanel extends JPanel {

    private final SocialNetwork network;

    public GraphPanel(SocialNetwork network) {
        this.network = network;
        setBackground(Theme.BG_PANEL);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        List<String> users = network.allUsers();
        Collections.sort(users, String.CASE_INSENSITIVE_ORDER);
        if (users.isEmpty()) {
            g2.setColor(Theme.TEXT_MUTED);
            g2.setFont(Theme.FONT_BODY);
            FontMetrics fm = g2.getFontMetrics();
            String msg = "No users to display";
            int x = (getWidth() - fm.stringWidth(msg)) / 2;
            int y = getHeight() / 2;
            g2.drawString(msg, x, y);
            g2.dispose();
            return;
        }

        // Calculate positions: circle layout
        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;
        int radius = Math.min(getWidth(), getHeight()) / 3;
        Map<String, Point> positions = new HashMap<>();
        for (int i = 0; i < users.size(); i++) {
            double angle = 2 * Math.PI * i / users.size();
            int x = centerX + (int) (radius * Math.cos(angle));
            int y = centerY + (int) (radius * Math.sin(angle));
            positions.put(users.get(i), new Point(x, y));
        }

        // Draw edges
        g2.setColor(Theme.BORDER_ACCENT);
        g2.setStroke(new BasicStroke(2));
        for (String user : users) {
            Point p1 = positions.get(user);
            List<String> friends = network.getFriends(user);
            for (String friend : friends) {
                if (user.compareTo(friend) < 0) { // draw each edge once
                    Point p2 = positions.get(friend);
                    g2.draw(new Line2D.Double(p1.x, p1.y, p2.x, p2.y));
                }
            }
        }

        // Draw nodes
        int nodeSize = 50;
        g2.setStroke(new BasicStroke(2));
        for (String user : users) {
            Point p = positions.get(user);
            // Node with gradient
            GradientPaint nodeGradient = new GradientPaint(
                p.x - nodeSize/2, p.y - nodeSize/2, Theme.ACCENT_BLUE,
                p.x + nodeSize/2, p.y + nodeSize/2, Theme.ACCENT_PURPLE
            );
            g2.setPaint(nodeGradient);
            g2.fill(new Ellipse2D.Double(p.x - nodeSize/2, p.y - nodeSize/2, nodeSize, nodeSize));
            
            // Border
            g2.setColor(Theme.TEXT_PRIMARY);
            g2.draw(new Ellipse2D.Double(p.x - nodeSize/2, p.y - nodeSize/2, nodeSize, nodeSize));

            // Inner highlight
            g2.setColor(new Color(255, 255, 255, 60));
            g2.fill(new Ellipse2D.Double(p.x - nodeSize/2 + 3, p.y - nodeSize/2 + 3, nodeSize - 6, nodeSize - 6));

            // Initials
            g2.setColor(Theme.TEXT_PRIMARY);
            g2.setFont(Theme.FONT_BODY.deriveFont(Font.BOLD, 14f));
            FontMetrics fm = g2.getFontMetrics();
            String initials = user.substring(0, Math.min(2, user.length())).toUpperCase();
            int tx = p.x - fm.stringWidth(initials) / 2;
            int ty = p.y + fm.getAscent() / 2;
            g2.drawString(initials, tx, ty);
        }

        g2.dispose();
    }

    public void refresh() {
        repaint();
    }
}