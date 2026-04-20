/**
 * SocialNetwork.js - Core Graph Engine
 * 
 * Data Structures:
 * - Map<string, object> users: O(1) user lookup
 * - Map<string, Set<string>> adjacencyList: Adjacency list for the graph
 */
export class SocialNetwork {
  constructor() {
    this.users = new Map();
    this.adjacencyList = new Map();
    this.activityLog = [];
    this.load();
  }

  // ── USER OPERATIONS ────────────────────────────────────────────────────────

  addUser(username, fullName = "") {
    const key = username.toLowerCase().trim();
    if (!key || this.users.has(key)) return false;

    const user = {
      username: key,
      displayName: username.trim(),
      fullName: fullName || username.trim(),
      friendsCount: 0,
      joinedAt: new Date().toISOString()
    };

    this.users.set(key, user);
    this.adjacencyList.set(key, new Set());
    
    this.log(`✦ New user joined: ${user.displayName}`);
    this.save();
    return true;
  }

  getUser(username) {
    return this.users.get(username.toLowerCase().trim());
  }

  getAllUsers() {
    return Array.from(this.users.values())
      .sort((a, b) => a.displayName.localeCompare(b.displayName));
  }

  // ── FRIENDSHIP OPERATIONS ──────────────────────────────────────────────────

  addFriend(userA, userB) {
    const a = userA.toLowerCase().trim();
    const b = userB.toLowerCase().trim();

    if (!this.users.has(a) || !this.users.has(b) || a === b) return false;
    if (this.adjacencyList.get(a).has(b)) return false;

    // Bidirectional edge
    this.adjacencyList.get(a).add(b);
    this.adjacencyList.get(b).add(a);

    // Update counts
    this.users.get(a).friendsCount++;
    this.users.get(b).friendsCount++;

    this.log(`⟷ ${this.users.get(a).displayName} and ${this.users.get(b).displayName} became friends`);
    this.save();
    return true;
  }

  getFriendsOf(username) {
    const key = username.toLowerCase().trim();
    const friendsKeys = this.adjacencyList.get(key) || new Set();
    return Array.from(friendsKeys).map(k => this.users.get(k));
  }

  // ── DS ALGORITHMS ──────────────────────────────────────────────────────────

  /**
   * Intersection of two adjacency lists
   * O(min(da, db))
   */
  findMutualFriends(userA, userB) {
    const aKeys = this.adjacencyList.get(userA.toLowerCase().trim()) || new Set();
    const bKeys = this.adjacencyList.get(userB.toLowerCase().trim()) || new Set();

    const mutuals = [];
    for (const key of aKeys) {
      if (bKeys.has(key)) {
        mutuals.push(this.users.get(key));
      }
    }
    return mutuals;
  }

  /**
   * BFS Level-2 exploration + Ranking
   * Finds friends of friends who are not already direct friends
   */
  suggestFriends(username, limit = 4) {
    const start = username.toLowerCase().trim();
    if (!this.users.has(start)) return [];

    const directFriends = this.adjacencyList.get(start);
    const mutualCounts = new Map();

    // BFS - Depth 1 (direct friends)
    for (const friendKey of directFriends) {
      // BFS - Depth 2 (friends of friends)
      const fofKeys = this.adjacencyList.get(friendKey);
      for (const fofKey of fofKeys) {
        if (fofKey !== start && !directFriends.has(fofKey)) {
          mutualCounts.set(fofKey, (mutualCounts.get(fofKey) || 0) + 1);
        }
      }
    }

    // Rank candidates by mutual count (Max-Heap simulation via sort)
    return Array.from(mutualCounts.entries())
      .map(([key, count]) => ({
        user: this.users.get(key),
        mutualCount: count
      }))
      .sort((a, b) => b.mutualCount - a.mutualCount)
      .slice(0, limit);
  }

  // ── REMOVE OPERATIONS ──────────────────────────────────────────────────────

  removeUser(username) {
    const key = username.toLowerCase().trim();
    if (!this.users.has(key)) return false;

    // Remove all friendships first
    const friends = this.adjacencyList.get(key) || new Set();
    for (const fKey of friends) {
      this.adjacencyList.get(fKey)?.delete(key);
      const fUser = this.users.get(fKey);
      if (fUser) fUser.friendsCount--;
    }

    this.adjacencyList.delete(key);
    this.users.delete(key);

    this.log(`✦ User removed: ${username}`);
    this.save();
    return true;
  }

  removeFriend(userA, userB) {
    const a = userA.toLowerCase().trim();
    const b = userB.toLowerCase().trim();

    if (!this.adjacencyList.get(a)?.has(b)) return false;

    this.adjacencyList.get(a).delete(b);
    this.adjacencyList.get(b).delete(a);

    this.users.get(a).friendsCount--;
    this.users.get(b).friendsCount--;

    this.log(`✦ Friendship removed: ${this.users.get(a)?.displayName || userA} ⟷ ${this.users.get(b)?.displayName || userB}`);
    this.save();
    return true;
  }

  // ── BFS TRAVERSAL ─────────────────────────────────────────────────────────

  /**
   * Full BFS traversal returning nodes grouped by depth level.
   * Uses a Queue (array with shift/push) for level-order traversal.
   * @returns {string[][]} Array of levels, each containing display names
   */
  bfs(username) {
    const start = username.toLowerCase().trim();
    if (!this.users.has(start)) return [];

    const visited = new Set();
    const queue = [start];
    visited.add(start);

    const levels = [];

    while (queue.length > 0) {
      const levelSize = queue.length;
      const currentLevel = [];

      for (let i = 0; i < levelSize; i++) {
        const node = queue.shift();
        currentLevel.push(this.users.get(node).displayName);

        for (const neighbor of (this.adjacencyList.get(node) || new Set())) {
          if (!visited.has(neighbor)) {
            visited.add(neighbor);
            queue.push(neighbor);
          }
        }
      }

      levels.push(currentLevel);
    }

    return levels;
  }

  // ── STATS & LOGGING ────────────────────────────────────────────────────────

  getStats() {
    const usersCount = this.users.size;
    let totalConnections = 0;
    this.adjacencyList.forEach(friends => totalConnections += friends.size);
    const edges = totalConnections / 2;

    const avgDeg = usersCount === 0 ? 0 : (edges * 2) / usersCount;
    const density = usersCount <= 1 ? 0 : (edges * 2) / (usersCount * (usersCount - 1)) * 100;

    return {
      users: usersCount,
      edges: edges,
      avgDeg: avgDeg.toFixed(1),
      density: Math.round(density)
    };
  }

  log(message) {
    this.activityLog.unshift({
      text: message,
      timestamp: new Date().getTime()
    });
    if (this.activityLog.length > 50) this.activityLog.pop();
  }

  // ── PERSISTENCE ───────────────────────────────────────────────────────────

  save() {
    const data = {
      users: Array.from(this.users.entries()),
      adjacencyList: Array.from(this.adjacencyList.entries()).map(([k, v]) => [k, Array.from(v)]),
      activityLog: this.activityLog
    };
    localStorage.setItem('social_graph_data', JSON.stringify(data));
  }

  load() {
    const raw = localStorage.getItem('social_graph_data');
    if (!raw) return;

    try {
      const data = JSON.parse(raw);
      this.users = new Map(data.users);
      this.adjacencyList = new Map(data.adjacencyList.map(([k, v]) => [k, new Set(v)]));
      this.activityLog = data.activityLog || [];
    } catch (e) {
      console.error("Failed to load data", e);
    }
  }

  seed() {
    if (this.users.size > 0) return;
    
    const demoUsers = ["Alice", "Bob", "Charlie", "Diana", "Eve", "Frank", "Grace"];
    demoUsers.forEach(u => this.addUser(u));

    this.addFriend("Alice", "Bob");
    this.addFriend("Alice", "Charlie");
    this.addFriend("Bob", "Diana");
    this.addFriend("Charlie", "Diana");
    this.addFriend("Diana", "Eve");
    this.addFriend("Eve", "Frank");
    this.addFriend("Bob", "Charlie");
    this.addFriend("Grace", "Alice");
  }
}
