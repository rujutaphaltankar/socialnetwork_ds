import java.util.*;

/**
 * Core engine of the Social Network Mini Engine.
 *
 * Data Structures Used:
 *  - HashMap<String, User>              → Hash Table for O(1) user lookup
 *  - HashMap<String, List<String>>      → Adjacency List (Graph)
 *  - Queue<String>                      → BFS traversal (friend-of-friend)
 *  - PriorityQueue<String[]>            → Max-Heap for ranked suggestions
 */
public class SocialNetwork {

    // ── Hash Table: username → User object ──────────────────────────────────
    private final HashMap<String, User> userTable;

    // ── Graph: Adjacency List (username → list of friend usernames) ─────────
    private final HashMap<String, List<String>> adjacencyList;

    // ── Activity log ────────────────────────────────────────────────────────
    private final List<String> activityLog;

    private int totalConnections; // each friendship counted once

    public SocialNetwork() {
        userTable      = new HashMap<>();
        adjacencyList  = new HashMap<>();
        activityLog    = new ArrayList<>();
        totalConnections = 0;
    }

    /**
     * Constructor that loads data from database if available.
     */
    public SocialNetwork(boolean loadFromDisk) {
        DatabaseManager.initialize();
        
        HashMap<String, User> loadedUsers = DatabaseManager.loadUsers();
        HashMap<String, List<String>> loadedFriendships = DatabaseManager.loadFriendships();
        
        if (loadFromDisk && !loadedUsers.isEmpty()) {
            this.userTable = loadedUsers;
            this.adjacencyList = loadedFriendships;
            // Ensure all users have an entry in adjacencyList even if they have no friends
            for (String username : userTable.keySet()) {
                this.adjacencyList.putIfAbsent(username, new ArrayList<>());
            }
            this.totalConnections = DatabaseManager.getTotalConnections();
            this.activityLog = new ArrayList<>();
            System.out.println("✓ Loaded " + userTable.size() + " users from database.");
        } else {
            userTable      = new HashMap<>();
            adjacencyList  = new HashMap<>();
            activityLog    = new ArrayList<>();
            totalConnections = 0;
        }
    }

    /**
     * Saves the current state to database.
     */
    private void saveData() {
        // Data is saved incrementally, no need for full save
    }

    // ════════════════════════════════════════════════════════════════════════
    //  ADD USER
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Adds a new user to the network.
     * Inserts into HashMap and initialises empty adjacency list.
     *
     * @param username unique identifier
     * @return true if added, false if already exists
     */
    public boolean addUser(String username) {
        String key = username.toLowerCase().trim();

        if (key.isEmpty()) throw new IllegalArgumentException("Username cannot be empty.");
        if (userTable.containsKey(key)) return false; // duplicate

        User user = new User(username);
        userTable.put(key, user);              // O(1) insertion into hash table
        adjacencyList.put(key, new ArrayList<>()); // empty edge list

        log("✦ New user joined: " + user.getDisplayName());
        DatabaseManager.saveUser(user);
        return true;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  ADD FRIEND (Bidirectional Edge)
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Creates a friendship (bidirectional edge) between two users.
     *
     * @param usernameA first user
     * @param usernameB second user
     * @return result message
     */
    public String addFriend(String usernameA, String usernameB) {
        String a = usernameA.toLowerCase().trim();
        String b = usernameB.toLowerCase().trim();

        if (!userTable.containsKey(a)) return "User not found: " + usernameA;
        if (!userTable.containsKey(b)) return "User not found: " + usernameB;
        if (a.equals(b))               return "A user cannot friend themselves.";

        List<String> friendsOfA = adjacencyList.get(a);
        if (friendsOfA.contains(b))    return usernameA + " and " + usernameB + " are already friends.";

        // Add bidirectional edge in graph
        friendsOfA.add(b);
        adjacencyList.get(b).add(a);

        // Update friend counts
        userTable.get(a).incrementFriendCount();
        userTable.get(b).incrementFriendCount();
        totalConnections++;

        log("⟷ " + usernameA + " and " + usernameB + " became friends");
        DatabaseManager.saveFriendship(usernameA, usernameB);
        return "SUCCESS";
    }

    // ════════════════════════════════════════════════════════════════════════
    //  FIND MUTUAL FRIENDS (Set Intersection)
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Finds common friends between two users.
     * Algorithm: intersection of two adjacency lists → O(min(d_a, d_b))
     *
     * @return list of mutual friends (User objects)
     */
    public List<User> findMutualFriends(String usernameA, String usernameB) {
        String a = usernameA.toLowerCase().trim();
        String b = usernameB.toLowerCase().trim();

        if (!userTable.containsKey(a) || !userTable.containsKey(b)) return Collections.emptyList();

        // Use HashSet for O(1) lookup during intersection
        Set<String> friendsOfA = new HashSet<>(adjacencyList.get(a));

        List<User> mutuals = new ArrayList<>();
        for (String friend : adjacencyList.get(b)) {
            if (friendsOfA.contains(friend)) {
                mutuals.add(userTable.get(friend));
            }
        }
        return mutuals;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  SUGGEST FRIENDS (BFS + Max-Heap)
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Suggests friends for a given user.
     *
     * Algorithm:
     *  1. BFS from the target user — explore depth-2 neighbours (friends-of-friends).
     *  2. Count mutual friends for each candidate.
     *  3. Push candidates into a max-heap (PriorityQueue) ranked by mutual count.
     *  4. Return top suggestions.
     *
     * @param username target user
     * @param limit    max suggestions to return
     * @return ranked list of [User, mutualCount] pairs
     */
    public List<SuggestionEntry> suggestFriends(String username, int limit) {
        String start = username.toLowerCase().trim();
        if (!userTable.containsKey(start)) return Collections.emptyList();

        List<String> friendsList = adjacencyList.get(start);
        if (friendsList == null) friendsList = new ArrayList<>();
        Set<String> directFriends = new HashSet<>(friendsList);
        Map<String, Integer> mutualCount = new HashMap<>();

        // ── BFS using Queue ─────────────────────────────────────────────────
        Queue<String> queue = new LinkedList<>();
        Set<String>   visited = new HashSet<>();

        queue.add(start);
        visited.add(start);

        int depth = 0;
        while (!queue.isEmpty() && depth < 2) {
            int levelSize = queue.size();
            depth++;

            for (int i = 0; i < levelSize; i++) {
                String current = queue.poll();
                for (String neighbor : adjacencyList.get(current)) {
                    if (!visited.contains(neighbor)) {
                        visited.add(neighbor);
                        queue.add(neighbor);

                        // Only depth-2 nodes are candidates (not direct friends, not self)
                        if (depth == 2 && !directFriends.contains(neighbor) && !neighbor.equals(start)) {
                            // Count mutual friends with start
                            int mutuals = countMutualFriends(start, neighbor);
                            mutualCount.put(neighbor, mutuals);
                        }
                    }
                }
            }
        }

        // ── Max-Heap (PriorityQueue) ranked by mutual friend count ───────────
        PriorityQueue<Map.Entry<String, Integer>> maxHeap =
            new PriorityQueue<>((x, y) -> y.getValue() - x.getValue());

        maxHeap.addAll(mutualCount.entrySet());

        // ── Extract top suggestions ──────────────────────────────────────────
        List<SuggestionEntry> suggestions = new ArrayList<>();
        int count = 0;
        while (!maxHeap.isEmpty() && count < limit) {
            Map.Entry<String, Integer> entry = maxHeap.poll();
            User user = userTable.get(entry.getKey());
            suggestions.add(new SuggestionEntry(user, entry.getValue()));
            count++;
        }

        return suggestions;
    }

    /** Helper: count mutual friends between two users without creating a list */
    private int countMutualFriends(String a, String b) {
        Set<String> setA = new HashSet<>(adjacencyList.get(a));
        int count = 0;
        for (String f : adjacencyList.get(b)) {
            if (setA.contains(f)) count++;
        }
        return count;
    }

    // ════════════════════════════════════════════════════════════════════════
    //  UTILITY / GETTERS
    // ════════════════════════════════════════════════════════════════════════

    public boolean userExists(String username) {
        return userTable.containsKey(username.toLowerCase().trim());
    }

    public User getUser(String username) {
        return userTable.get(username.toLowerCase().trim());
    }

    public int getTotalUsers()       { return userTable.size(); }
    public int getTotalConnections() { return totalConnections; }

    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>(userTable.values());
        users.sort(Comparator.comparing(User::getDisplayName));
        return users;
    }

    public List<User> getFriendsOf(String username) {
        String key = username.toLowerCase().trim();
        if (!adjacencyList.containsKey(key)) return Collections.emptyList();
        List<User> friends = new ArrayList<>();
        for (String f : adjacencyList.get(key)) {
            friends.add(userTable.get(f));
        }
        return friends;
    }

    public List<String> getActivityLog() {
        List<String> copy = new ArrayList<>(activityLog);
        Collections.reverse(copy);   // newest first
        return copy;
    }

    private void log(String message) {
        activityLog.add(message);
        if (activityLog.size() > 50) activityLog.remove(0); // keep last 50
    }

    // ════════════════════════════════════════════════════════════════════════
    //  INNER CLASS: Suggestion Entry
    // ════════════════════════════════════════════════════════════════════════

    /** Holds a suggested user with their mutual-friend count. */
    public static class SuggestionEntry {
        private final User user;
        private final int  mutualCount;

        public SuggestionEntry(User user, int mutualCount) {
            this.user        = user;
            this.mutualCount = mutualCount;
        }

        public User getUser()       { return user; }
        public int  getMutualCount(){ return mutualCount; }
    }

    // ── Additional methods for JS interface ──

    public Map<String, Object> getStats() {
        int users = userTable.size();
        int edges = totalConnections;
        double avgDeg = users == 0 ? 0 : (double) edges * 2 / users;
        double density = users <= 1 ? 0 : (double) edges * 2 / (users * (users - 1)) * 100;
        Map<String, Object> stats = new HashMap<>();
        stats.put("users", users);
        stats.put("edges", edges);
        stats.put("avgDeg", String.format("%.1f", avgDeg));
        stats.put("density", (int) Math.round(density));
        return stats;
    }

    public List<String> allUsers() {
        return new ArrayList<>(userTable.keySet());
    }

    public List<String> getFriends(String name) {
        return adjacencyList.getOrDefault(name.toLowerCase().trim(), new ArrayList<>());
    }

    public boolean addFriendship(String a, String b) {
        return "SUCCESS".equals(addFriend(a, b));
    }

    public boolean removeFriendship(String a, String b) {
        return "SUCCESS".equals(removeFriend(a, b));
    }

    // Add removeFriend method
    public String removeFriend(String usernameA, String usernameB) {
        String a = usernameA.toLowerCase().trim();
        String b = usernameB.toLowerCase().trim();

        if (!userTable.containsKey(a)) return "User not found: " + usernameA;
        if (!userTable.containsKey(b)) return "User not found: " + usernameB;
        if (a.equals(b))               return "A user cannot remove themselves.";

        List<String> friendsOfA = adjacencyList.get(a);
        if (!friendsOfA.contains(b))    return usernameA + " and " + usernameB + " are not friends.";

        // Remove bidirectional edge in graph
        friendsOfA.remove(b);
        adjacencyList.get(b).remove(a);

        // Update friend counts
        userTable.get(a).decrementFriendCount();
        userTable.get(b).decrementFriendCount();
        totalConnections--;

        log("Removed friendship: " + usernameA + " and " + usernameB);
        return "SUCCESS";
    }

    public List<String> getMutualFriends(String a, String b) {
        List<String> friendsA = getFriends(a);
        List<String> friendsB = getFriends(b);
        List<String> mutuals = new ArrayList<>();
        for (String f : friendsA) {
            if (friendsB.contains(f)) mutuals.add(f);
        }
        return mutuals;
    }

    public List<String[]> getSuggestions(String name, int limit) {
        String key = name.toLowerCase().trim();
        if (!userTable.containsKey(key)) return new ArrayList<>();
        List<String> friends = adjacencyList.get(key);
        Map<String, List<String>> mutualsMap = new HashMap<>();
        for (String f : friends) {
            for (String ff : adjacencyList.get(f)) {
                if (!ff.equals(key) && !friends.contains(ff)) {
                    mutualsMap.computeIfAbsent(ff, k -> new ArrayList<>()).add(f);
                }
            }
        }
        PriorityQueue<String[]> pq = new PriorityQueue<>((a, b) -> Integer.parseInt(b[1]) - Integer.parseInt(a[1]));
        for (Map.Entry<String, List<String>> entry : mutualsMap.entrySet()) {
            String sug = entry.getKey();
            List<String> mutuals = entry.getValue();
            String[] arr = new String[2 + mutuals.size()];
            arr[0] = sug;
            arr[1] = String.valueOf(mutuals.size());
            for (int i = 0; i < mutuals.size(); i++) arr[2 + i] = mutuals.get(i);
            pq.add(arr);
        }
        List<String[]> result = new ArrayList<>();
        while (!pq.isEmpty()) result.add(pq.poll());
        return result.subList(0, Math.min(result.size(), limit));
    }

    public String serialize() {
        StringBuilder sb = new StringBuilder();
        sb.append("users:");
        for (String user : userTable.keySet()) {
            sb.append(user).append(",");
        }
        if (sb.length() > 0 && sb.charAt(sb.length() - 1) == ',') sb.setLength(sb.length() - 1);
        sb.append("\n");
        sb.append("friends:");
        for (Map.Entry<String, List<String>> entry : adjacencyList.entrySet()) {
            for (String friend : entry.getValue()) {
                if (entry.getKey().compareTo(friend) < 0) {
                    sb.append(entry.getKey()).append("-").append(friend).append(",");
                }
            }
        }
        if (sb.length() > 0 && sb.charAt(sb.length() - 1) == ',') sb.setLength(sb.length() - 1);
        return sb.toString();
    }

    public void deserialize(String str) {
        userTable.clear();
        adjacencyList.clear();
        activityLog.clear();
        totalConnections = 0;
        String[] lines = str.split("\n");
        for (String line : lines) {
            if (line.startsWith("users:")) {
                String[] users = line.substring(6).split(",");
                for (String u : users) if (!u.isEmpty()) addUser(u);
            } else if (line.startsWith("friends:")) {
                String[] friends = line.substring(8).split(",");
                for (String f : friends) if (!f.isEmpty()) {
                    String[] pair = f.split("-");
                    if (pair.length == 2) addFriend(pair[0], pair[1]);
                }
            }
        }
    }

    public String popLog() {
        if (!activityLog.isEmpty()) return activityLog.remove(activityLog.size() - 1);
        return "";
    }
}
