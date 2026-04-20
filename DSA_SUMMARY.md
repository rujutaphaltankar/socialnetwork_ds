# Data Structures & Algorithms (DSA) Summary

This document provides a high-level overview of the DSA principles applied in the SocialGraph Engine.

## 1. Graph Data Structure
A social network is fundamentally a **Graph** $G = (V, E)$, where:
- **Vertices (V)**: Represent users.
- **Edges (E)**: Represent relationships/friendships.
- **Implementation**: We use an **Adjacency List** (specifically a `Map<String, Set<String>>`) to achieve $O(1)$ lookup and space efficiency for sparse networks.

## 2. Breadth-First Search (BFS)
BFS is the primary traversal algorithm used for:
- **Level Discovery**: Finding friends of friends.
- **Visualizing Connectivity**: Animating how information or connections spread level-by-level.
- **Complexity**: $O(V + E)$.

## 3. Heaps & Priority Queues
Used for **Friend Suggestions**:
- Candidates are ranked based on the number of "Mutual Friends".
- A **Max-Heap** structure (or simulated priority sorting) ensures the most relevant suggestions appear first.
- **Complexity**: $O(\log n)$ for insertions/extractions.

## 4. Hash Tables (Maps)
Used for user lookups:
- Ensuring that retrieving a user's profile by their unique username is an $O(1)$ operation.

## 5. Algorithm Logic (Pseudocode)

### 5.1 Mutual Friends Discovery
```text
FUNCTION findMutualFriends(User A, User B):
    List mutuals = []
    Set friendsA = graph.getFriends(A)
    Set friendsB = graph.getFriends(B)
    
    FOR EACH friend IN friendsA:
        IF friendsB.contains(friend):
            mutuals.add(friend)
            
    RETURN mutuals
```

### 5.2 Breadth-First Search (Traversal)
```text
FUNCTION BFS(StartNode):
    Queue q = [StartNode]
    Set visited = {StartNode}
    
    WHILE q is NOT empty:
        current = q.pop()
        PRINT current
        
        FOR EACH neighbor IN graph.getNeighbors(current):
            IF neighbor NOT IN visited:
                visited.add(neighbor)
                q.push(neighbor)
```
