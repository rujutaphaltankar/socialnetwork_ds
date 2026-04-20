import java.sql.*;
import java.util.*;

/**
 * Manages database connections and operations for the social network.
 * Uses SQLite for local persistence.
 */
public class DatabaseManager {

    private static final String DB_URL = "jdbc:sqlite:socialnetwork.db";
    private static Connection connection;

    /**
     * Initializes the database connection and creates tables if they don't exist.
     */
    public static void initialize() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(DB_URL);
            createTables();
            System.out.println("✓ Database connected successfully.");
        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("✗ Database connection failed: " + e.getMessage());
            // Fallback to file-based persistence if database fails
        }
    }

    /**
     * Creates the necessary tables.
     */
    private static void createTables() throws SQLException {
        String createUsersTable = """
            CREATE TABLE IF NOT EXISTS users (
                username TEXT PRIMARY KEY,
                display_name TEXT NOT NULL,
                joined_at TEXT NOT NULL,
                friend_count INTEGER DEFAULT 0
            );
            """;

        String createFriendshipsTable = """
            CREATE TABLE IF NOT EXISTS friendships (
                user_a TEXT NOT NULL,
                user_b TEXT NOT NULL,
                PRIMARY KEY (user_a, user_b),
                FOREIGN KEY (user_a) REFERENCES users(username),
                FOREIGN KEY (user_b) REFERENCES users(username)
            );
            """;

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createUsersTable);
            stmt.execute(createFriendshipsTable);
        }
    }

    /**
     * Saves a user to the database.
     */
    public static void saveUser(User user) {
        if (connection == null) return;
        String sql = "INSERT OR REPLACE INTO users (username, display_name, joined_at, friend_count) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getDisplayName());
            pstmt.setString(3, user.getJoinedAt());
            pstmt.setInt(4, user.getFriendCount());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error saving user: " + e.getMessage());
        }
    }

    /**
     * Saves a friendship to the database.
     */
    public static void saveFriendship(String userA, String userB) {
        if (connection == null) return;
        String sql = "INSERT OR IGNORE INTO friendships (user_a, user_b) VALUES (?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            // Ensure consistent ordering
            if (userA.compareTo(userB) < 0) {
                pstmt.setString(1, userA);
                pstmt.setString(2, userB);
            } else {
                pstmt.setString(1, userB);
                pstmt.setString(2, userA);
            }
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error saving friendship: " + e.getMessage());
        }
    }

    /**
     * Loads all users from the database.
     */
    public static HashMap<String, User> loadUsers() {
        HashMap<String, User> users = new HashMap<>();
        if (connection == null) return users;

        String sql = "SELECT * FROM users";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String username = rs.getString("username");
                String displayName = rs.getString("display_name");
                String joinedAt = rs.getString("joined_at");
                int friendCount = rs.getInt("friend_count");

                User user = new User(username, displayName, joinedAt, friendCount);
                users.put(username, user);
            }
        } catch (SQLException e) {
            System.err.println("Error loading users: " + e.getMessage());
        }
        return users;
    }

    /**
     * Loads all friendships from the database.
     */
    public static HashMap<String, List<String>> loadFriendships() {
        HashMap<String, List<String>> friendships = new HashMap<>();
        if (connection == null) return friendships;

        String sql = "SELECT user_a, user_b FROM friendships";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String userA = rs.getString("user_a");
                String userB = rs.getString("user_b");

                friendships.computeIfAbsent(userA, k -> new ArrayList<>()).add(userB);
                friendships.computeIfAbsent(userB, k -> new ArrayList<>()).add(userA);
            }
        } catch (SQLException e) {
            System.err.println("Error loading friendships: " + e.getMessage());
        }
        return friendships;
    }

    /**
     * Gets the total number of connections.
     */
    public static int getTotalConnections() {
        if (connection == null) return 0;
        String sql = "SELECT COUNT(*) FROM friendships";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error getting total connections: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Closes the database connection.
     */
    public static void close() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("✓ Database connection closed.");
            } catch (SQLException e) {
                System.err.println("Error closing database: " + e.getMessage());
            }
        }
    }
}