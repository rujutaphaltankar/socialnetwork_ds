/**
 * A dummy Java class for demonstration purposes.
 * This class simulates a simple statistics tracker for a social graph.
 */
public class NetworkStatsTracker {
    private int totalNodes;
    private int totalEdges;

    public NetworkStatsTracker(int nodes, int edges) {
        this.totalNodes = nodes;
        this.totalEdges = edges;
    }

    public double getAverageDegree() {
        if (totalNodes == 0) return 0;
        return (double) 2 * totalEdges / totalNodes;
    }

    public static void main(String[] args) {
        NetworkStatsTracker tracker = new NetworkStatsTracker(50, 120);
        System.out.println("--- Network Statistics Demo ---");
        System.out.println("Nodes: 50");
        System.out.println("Edges: 120");
        System.out.println("Average Degree: " + tracker.getAverageDegree());
    }
}
