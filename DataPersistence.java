import java.io.*;
import java.util.*;

/**
 * Handles saving and loading social network data to/from disk.
 */
public class DataPersistence {

    private static final String DATA_FILE = "socialnetwork_data.dat";

    /**
     * Saves the current network state to disk.
     */
    public static void save(HashMap<String, User> userTable, HashMap<String, List<String>> adjacencyList, int totalConnections) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            oos.writeObject(userTable);
            oos.writeObject(adjacencyList);
            oos.writeInt(totalConnections);
        } catch (IOException e) {
            System.err.println("Error saving data: " + e.getMessage());
        }
    }

    /**
     * Loads network data from disk.
     * Returns null if file doesn't exist or loading fails.
     */
    public static NetworkData load() {
        File file = new File(DATA_FILE);
        if (!file.exists()) return null;

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(DATA_FILE))) {
            @SuppressWarnings("unchecked")
            HashMap<String, User> userTable = (HashMap<String, User>) ois.readObject();
            @SuppressWarnings("unchecked")
            HashMap<String, List<String>> adjacencyList = (HashMap<String, List<String>>) ois.readObject();
            int totalConnections = ois.readInt();

            return new NetworkData(userTable, adjacencyList, totalConnections);
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading data: " + e.getMessage());
            return null;
        }
    }

    /**
     * Container for loaded network data.
     */
    public static class NetworkData {
        public final HashMap<String, User> userTable;
        public final HashMap<String, List<String>> adjacencyList;
        public final int totalConnections;

        public NetworkData(HashMap<String, User> userTable, HashMap<String, List<String>> adjacencyList, int totalConnections) {
            this.userTable = userTable;
            this.adjacencyList = adjacencyList;
            this.totalConnections = totalConnections;
        }
    }
}
