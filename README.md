# SocialGraph | Mini Engine v2.0

> A high-performance, algorithm-driven social network engine built with **JavaScript** and modern web technologies.

This project visualizes and interacts with a graph-based network. It implements essential data structures (Hash Tables, Graphs, Queues, Max-Heaps) from scratch to drive features like mutual friend calculations, network visualization, and advanced friend suggestions.

---

## 👥 Team Contributions

Our team divided the architecture, styling, and algorithmic implementations to deliver a complete, full-stack experience:

### **Member 1 (You): Lead Architecture & Dashboard UI**
- **Vite Setup:** Initialized the modern frontend tooling environment.
- **Project Structure:** Designed the multi-view single-page application (SPA) architecture in `app.js`.
- **Dashboard UI Implementation:** Developed the main dashboard interface, including the live real-time statistics cards, Activity Feed log system, user listings, and unified sidebar navigation.

### **Member 2: Core Algorithms (BFS & Max-Heap)**
- **BFS Explorer:** Implemented the Breadth-First Search queue logic to calculate and visualize network topography level-by-level without recursion.
- **Priority Queue & Suggestions:** Engineered the "Friend Suggestions" system. Built a Level-2 BFS traversal to discover "friends of friends", sorting candidates natively via Priority Queue (Max-Heap) simulations based on the number of mutual connections.
- **Engine Methods:** Authored the mathematical logic for set-intersections (`findMutualFriends`) and graph-traversal (`suggestFriends`) in `SocialNetwork.js`.

### **Member 3: Interactive Network Graph Engine**
- **Force-Directed Physics:** Built a custom JavaScript physics engine in `graph.js` using Coulomb's law (repulsion) and Hooke's law (spring attraction) to automatically structure the network graph aesthetically.
- **Canvas Rendering:** Developed raw HTML5 `<canvas>` rendering logic to visualize the nodes, edges, hover-states, and friend-count tooltips without relying on any external libraries like D3.js.
- **BFS Animation:** Wired the graph to safely interact with the engine and visually step-animate the BFS algorithm's tiers from a specific user node.

### **Member 4: Styling, CSS Architecture & Data Persistence**
- **Design System:** Engineered the premium dark theme in `index.css`, establishing standard CSS variables (glassmorphism effects, specific hex pairings, shadows, gradients, and custom scrolling).
- **Responsive UI/UX:** Added smooth CSS transitions, intelligent toast notifications, and `animate-in` keyframes for tool cards.
- **State Management (Database):** Implemented the complete `save()` and `load()` architecture using the browser's persistent `localStorage`. The application intelligently serializes the active active Maps/Sets into a `JSON` string, guaranteeing the graph structure actively survives computer/server restarts.

---

## 🧠 Core Data Structures Used

| Structure | ES6 Implementation | Algorithmic Purpose |
|---|---|---|
| **Hash Table** | `Map<String, Object>` | Guaranteed **O(1)** time-complexity for fast user lookup and modification. |
| **Adjacency List** | `Map<String, Set<String>>` | The primary Graph structure; maps users to sets of their connected friends. |
| **Queue (BFS)** | Array Operations (`push/shift`) | Used heavily for Level-1 / Level-2 friend traversal and mutual connection discovery. |
| **Max-Heap** | Array Sort Simulation | Ranks friend suggestions by sorting candidate objects based on their mutual connection count. |
| **Set** | `Set<String>` | Enables ultra-fast **O(1)** intersection calculations when determining mutual friends. |

---

## 🚀 How to Run Locally

You must have **Node.js** installed on your computer.

1. **Install dependencies:**
   ```bash
   npm install
   ```

2. **Run the development server:**
   ```bash
   npm run dev
   ```

3. Open your browser and navigate to the provided host (usually `http://localhost:5173`).

---

## 🎨 UI & Additional Features

- **Dynamic Network Graph:** Live physics simulation of nodes and edges built entirely in vanilla JS.
- **Fully Persistent:** All added users and friendships are saved automatically to your browser cache; no backend database configuration is required for teachers/graders to test the app.
- **BFS Visualizer:** Select a user and run a live visualization of their network depth tier-by-tier.

*Built with Vite · HTML5 Canvas · Vanilla JavaScript · Pure CSS*
