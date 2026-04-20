import java.io.Serializable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Represents a user in the Social Network.
 * Stores user identity and metadata.
 */
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String username;
    private final String displayName;
    private final LocalDate joinedAt;
    private int friendCount;

    public User(String username) {
        this.username = username.toLowerCase().trim();
        this.displayName = username.trim();
        this.joinedAt = LocalDate.now();
        this.friendCount = 0;
    }

    /**
     * Constructor for loading from database.
     */
    public User(String username, String displayName, String joinedAt, int friendCount) {
        this.username = username;
        this.displayName = displayName;
        this.joinedAt = LocalDate.parse(joinedAt, DateTimeFormatter.ofPattern("MMM dd, yyyy"));
        this.friendCount = friendCount;
    }

    // ── Getters ──────────────────────────────────────────────────────────────

    public String getUsername()    { return username; }
    public String getDisplayName() { return displayName; }
    public int    getFriendCount() { return friendCount; }

    public String getJoinedAt() {
        return joinedAt.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"));
    }

    // ── Mutators ─────────────────────────────────────────────────────────────

    public void incrementFriendCount() { friendCount++; }
    public void decrementFriendCount() { if (friendCount > 0) friendCount--; }

    // ── Utility ──────────────────────────────────────────────────────────────

    @Override
    public String toString() {
        return displayName + " (@" + username + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof User)) return false;
        return username.equals(((User) obj).username);
    }

    @Override
    public int hashCode() {
        return username.hashCode();
    }
}
