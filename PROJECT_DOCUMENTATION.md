# SocialGraph Mini Engine v2.0 — Comprehensive Project Documentation

## 1. Executive Summary
SocialGraph Mini Engine is a high-performance web-based dashboard designed to analyze and visualize social network structures. It transitions the original Java Swing architecture into a modern Single Page Application (SPA) built with vanilla JavaScript and CSS. The project's primary focus is on demonstrating core Computer Science data structures (Graphs, Hash Tables, Heaps) through interactive features like force-directed visualizations, BFS traversals, and ranked connection suggestions.

---

## 2. System Architecture
The application follows a modular, client-side only architecture to ensure portability and high performance without the need for a backend server.

- **Storage Layer**: Uses Browser `localStorage` for JSON-serialized persistence.
- **Engine Layer (`SocialNetwork.js`)**: Encapsulates all graph theory logic and state management.
- **Visualization Layer (`graph.js`)**: A custom physics-based engine for rendering nodes and edges on HTML5 Canvas.
- **Controller Layer (`app.js`)**: Manages the DOM, handles navigation, and syncs the Engine with the UI.
- **Presentation Layer (`index.html` / `index.css`)**: Implements a premium, responsive dark-themed design system.

---

## 3. Data Structure Specifications

### 3.1 Hash Tables (Users)
- **Implementation**: `Map<string, object>` / ES6 Map
- **Logic**: Used to store user profiles where the unique `username` (lowercased) acts as the key.
- **Complexity**: **O(1)** for insertion, deletion, and lookup.
- **Motivation**: Ensures that finding any user in a network remains instantaneous, regardless of scale.

### 3.2 Adjacency List (Graph)
- **Implementation**: `Map<string, Set<string>>`
- **Logic**: Each key is a username, and the value is a `Set` containing the usernames of their friends.
- **Complexity**: **O(1)** to check if two users are friends; **O(deg(u))** to list all friends.
- **Motivation**: More space-efficient than an adjacency matrix for sparse social networks and allows for fast lookup.

### 3.3 Queues (Traversal)
- **Implementation**: Standard JavaScript Arrays using `.push()` and `.shift()`.
- **Logic**: Essential for Breadth-First Search (BFS) to maintain the "front" of the traversal in level-order.

### 3.4 Max-Heap Simulation (Ranking)
- **Implementation**: Array-based sorting of candidate objects based on priority values.
- **Logic**: When generating suggestions, candidates are ranked by their mutual friend count in descending order.

---

## 4. Algorithm Walkthroughs

### 4.1 Breadth-First Search (BFS)
Used in the **BFS Explorer** and **Suggestions Engine**.
- **Process**: Starts at a node, explores all neighbors (depth 1), then all neighbors of neighbors (depth 2), and so on.
- **Implementation**: Uses a `visited` Set to prevent infinite loops and an array-based Queue for level-order processing.

### 4.2 Set Intersection (Mutual Friends)
Used to find common connections between User A and User B.
- **Process**: Compares the adjacency lists of both users.
- **Optimization**: The algorithm iterates through the smaller friend list and checks presence in the larger list's `Set`, achieving **O(min(da, db))** complexity.

### 4.3 Force-Directed Layout (Graph Physics)
The visualization logic uses physical simulations to auto-arrange nodes:
1.  **Coulomb's Law (Repulsion)**: Every node pushes every other node away ($Force = \frac{k}{dist^2}$). This prevents overlap.
2.  **Hooke's Law (Attraction)**: Edges act as springs, pulling connected nodes together to form clusters.
3.  **Damping**: Gradual velocity reduction to ensure the simulation settles into a stable "equilibrium" state.

---

## 5. Module Breakdown

### 5.1 `SocialNetwork.js`
The core logic engine.
- `addUser(username, name)`: Adds a node to the graph.
- `addFriend(a, b)`: Adds a bidirectional edge.
- `bfs(startNode)`: Returns nodes grouped by their distance from start.
- `suggestFriends(user)`: Implements Level-2 exploration + ranking logic.
- `save() / load()`: Handles JSON serialization to `localStorage`.

### 5.2 `graph.js`
The custom visualization engine.
- `NetworkGraph` class: Manages a dedicated HTML5 Canvas.
- `setData()`: Syncs the visualizer with the current graph state.
- `highlightBFS()`: Animates traversal levels with specific color palettes.
- `_tick()`: The physics engine, calculating repulsion and attraction forces 60 times per second.

### 5.3 `app.js`
The application controller.
- Manages navigation between 7 distinct view containers.
- Orchestrates UI updates and binds input handlers to engine methods.
- Ensures the Dashboard, User List, and Graph views remain perfectly synchronized.

---

## 6. Persistence & Security
- **Local Storage**: Data is persistently saved under the key `social_graph_data`. All calculations are client-side, ensuring user privacy and instant responsiveness.
- **Input Sanitization**: Usernames are normalized (lowercased and trimmed) to ensure graph integrity and prevent duplicate nodes.

---

## 7. User Interface Design
- **Aesthetic**: Premium "Dark Mode" (`#09090b`) with Indigo accents (`#6366f1`).
- **Glassmorphism**: UI components feature translucent backgrounds and subtle borders for a modern, high-end feel.
- **Interactivity**: Includes real-time search, animated transitions, and interactive graph exploration (drag-and-drop, hover tooltips).

---

## 8. Setup & Execution
1.  **Requirements**: Node.js installed on the environment.
2.  **Installation**: Run `npm install` in the project root.
3.  **Start Server**: Run `npm run dev`.
4.  **Usage**: Navigate to `http://localhost:5173` to interact with the full suite of social network tools.

---
*Generated Documentation for SocialGraph Engine Project | v2.0*
