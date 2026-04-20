import { SocialNetwork } from './SocialNetwork.js';
import { NetworkGraph } from './graph.js';

// ── INITIALIZATION ──────────────────────────────────────────────────────────

const engine = new SocialNetwork();
engine.seed(); // Only seeds if no existing data

// Graph visualizations (initialized lazily when canvas is available)
let graph = null;
let dashboardGraph = null;

function getGraph() {
  if (!graph) {
    const canvas = document.getElementById('network-canvas');
    if (canvas) graph = new NetworkGraph('network-canvas');
  }
  return graph;
}

function getDashboardGraph() {
  if (!dashboardGraph) {
    const canvas = document.getElementById('dashboard-graph-canvas');
    if (canvas) dashboardGraph = new NetworkGraph('dashboard-graph-canvas');
  }
  return dashboardGraph;
}

// ── REFRESH UI ──────────────────────────────────────────────────────────────

function refreshUI() {
  const stats = engine.getStats();

  // Dashboard stat cards
  setText('stat-total-users', stats.users);
  setText('stat-total-edges', stats.edges);
  setText('stat-avg-conn', stats.avgDeg);

  // Sidebar + footer stats
  setText('sidebar-user-count', stats.users);
  setText('foot-users', stats.users);
  setText('foot-edges', stats.edges);
  setText('foot-density', stats.density + '%');

  renderDashboardUserList();
  renderActivityFeed();
  populateAllDropdowns();
  renderFullUserList();
  renderFriendshipList();
  refreshGraph();
}

function setText(id, val) {
  const el = document.getElementById(id);
  if (el) el.textContent = val;
}

// ── DASHBOARD: Connected Users List ─────────────────────────────────────────

function renderDashboardUserList(filter = "") {
  const container = document.getElementById('dashboard-user-list');
  if (!container) return;
  container.innerHTML = "";
  const users = engine.getAllUsers();
  const f = filter.toLowerCase();

  users.filter(u => u.displayName.toLowerCase().includes(f)).forEach(u => {
    const item = document.createElement('div');
    item.className = 'user-item';
    const isHub = u.friendsCount >= 3;
    const colors = ['#6366f1', '#3b82f6', '#10b981', '#f59e0b', '#ec4899', '#06b6d4'];
    const color = colors[u.displayName.charCodeAt(0) % colors.length];

    item.innerHTML = `
      <div class="user-avatar" style="background-color: ${color}20; color: ${color}">${u.displayName.substring(0, 2).toUpperCase()}</div>
      <div class="user-info">
        <span class="user-name">${u.displayName}</span>
        <span class="user-sub">${u.friendsCount} friends · active</span>
      </div>
      ${isHub ? '<span class="hub-tag">hub</span>' : '<span class="connect-tag">+</span>'}
    `;
    container.appendChild(item);
  });
}

// ── DASHBOARD: Activity Feed ────────────────────────────────────────────────

function renderActivityFeed() {
  const container = document.getElementById('dashboard-activity-feed');
  if (!container) return;
  container.innerHTML = "";
  const logs = engine.activityLog;

  if (logs.length === 0) {
    container.innerHTML = '<p class="empty-msg">No activity yet. Add users or friends!</p>';
    return;
  }

  logs.slice(0, 15).forEach((log, i) => {
    const item = document.createElement('div');
    item.className = 'activity-item';
    
    let dotClass = 'blue';
    if (log.text.includes('joined')) dotClass = 'blue';
    if (log.text.includes('friends') || log.text.includes('became')) dotClass = 'green';
    if (log.text.includes('suggestion') || log.text.includes('connected')) dotClass = 'purple';

    const cleanText = log.text.replace('✦ ', '').replace('⟷ ', '');
    // Bold usernames
    const boldedText = cleanText.replace(/(\b[A-Z][a-z]+\b)/g, '<strong>$1</strong>');

    item.innerHTML = `
      <div class="activity-dot ${dotClass}"></div>
      <div>
        <p class="activity-text">${boldedText}</p>
        <span class="activity-time">${formatTime(log.timestamp)}</span>
      </div>
    `;
    container.appendChild(item);
  });
}

// ── USERS PAGE: Full Grid ───────────────────────────────────────────────────

function renderFullUserList(filter = "") {
  const container = document.getElementById('users-full-list');
  if (!container) return;
  container.innerHTML = "";

  const users = engine.getAllUsers();
  const f = filter.toLowerCase();
  const filtered = users.filter(u => u.displayName.toLowerCase().includes(f));

  setText('users-result-count', filtered.length + ' users');

  filtered.forEach(u => {
    const friends = engine.getFriendsOf(u.username);
    const colors = ['#6366f1', '#3b82f6', '#10b981', '#f59e0b', '#ec4899', '#06b6d4'];
    const color = colors[u.displayName.charCodeAt(0) % colors.length];

    const card = document.createElement('div');
    card.className = 'user-full-card';
    card.innerHTML = `
      <div class="ufc-top">
        <div class="ufc-avatar" style="background-color: ${color}20; color: ${color}; border-color: ${color}40">${u.displayName.substring(0, 2).toUpperCase()}</div>
        <div class="ufc-name">${u.displayName}</div>
        <div class="ufc-sub">${u.friendsCount} friends</div>
      </div>
      <div class="ufc-friends">
        ${friends.length > 0 
          ? friends.map(f => `<span class="ufc-friend-chip">${f.displayName}</span>`).join('')
          : '<span class="empty-msg small">No connections</span>'
        }
      </div>
      <button class="btn btn-secondary btn-xs ufc-remove" data-user="${u.username}">Remove user</button>
    `;
    container.appendChild(card);
  });

  // Bind remove buttons
  container.querySelectorAll('.ufc-remove').forEach(btn => {
    btn.addEventListener('click', () => {
      const uname = btn.getAttribute('data-user');
      if (confirm(`Remove "${uname}" from the network?`)) {
        engine.removeUser(uname);
        refreshUI();
      }
    });
  });
}

// ── ADD FRIENDSHIP PAGE: List ───────────────────────────────────────────────

function renderFriendshipList() {
  const container = document.getElementById('friendship-list');
  if (!container) return;
  container.innerHTML = "";

  const users = engine.getAllUsers();
  const seen = new Set();

  users.forEach(u => {
    const friends = engine.getFriendsOf(u.username);
    friends.forEach(f => {
      const key = [u.username, f.username].sort().join('-');
      if (!seen.has(key)) {
        seen.add(key);
        const row = document.createElement('div');
        row.className = 'friendship-row';
        row.innerHTML = `
          <div class="fr-pair">
            <span class="fr-name">${u.displayName}</span>
            <span class="fr-arrow">⟷</span>
            <span class="fr-name">${f.displayName}</span>
          </div>
          <button class="btn btn-xs btn-danger fr-remove" data-a="${u.username}" data-b="${f.username}">Remove</button>
        `;
        container.appendChild(row);
      }
    });
  });

  if (seen.size === 0) {
    container.innerHTML = '<p class="empty-msg">No friendships yet</p>';
  }

  container.querySelectorAll('.fr-remove').forEach(btn => {
    btn.addEventListener('click', () => {
      engine.removeFriend(btn.dataset.a, btn.dataset.b);
      refreshUI();
    });
  });
}

// ── DROPDOWN POPULATION ─────────────────────────────────────────────────────

function populateAllDropdowns() {
  const users = engine.getAllUsers();
  
  ['select-suggest-user', 'page-suggest-user', 'bfs-start-user', 'graph-bfs-user'].forEach(id => {
    const sel = document.getElementById(id);
    if (!sel) return;
    const curVal = sel.value;
    sel.innerHTML = id === 'graph-bfs-user'
      ? '<option value="" disabled selected>BFS from user...</option>'
      : '<option value="" disabled selected>Select user...</option>';
    users.forEach(u => {
      const opt = document.createElement('option');
      opt.value = u.username;
      opt.textContent = u.displayName;
      sel.appendChild(opt);
    });
    if (curVal && Array.from(sel.options).some(o => o.value === curVal)) sel.value = curVal;
  });
}

function refreshGraph() {
  const users = engine.getAllUsers();
  const stats = engine.getStats();

  // Full-page graph
  const g = getGraph();
  if (g) {
    g.setData(users, engine.adjacencyList);
    setText('graph-stat-nodes', stats.users + ' nodes');
    setText('graph-stat-edges', stats.edges + ' edges');
  }

  // Dashboard embedded graph
  const dg = getDashboardGraph();
  if (dg) {
    dg.setData(users, engine.adjacencyList);
  }
}

// ── HELPER ──────────────────────────────────────────────────────────────────

function formatTime(ts) {
  const diff = Math.floor((Date.now() - ts) / 1000);
  if (diff < 60) return 'Just now';
  if (diff < 3600) return `${Math.floor(diff / 60)}m ago`;
  if (diff < 86400) return `${Math.floor(diff / 3600)}h ago`;
  return `${Math.floor(diff / 86400)}d ago`;
}

function showToast(id, msg, type = 'success') {
  const el = document.getElementById(id);
  if (!el) return;
  el.textContent = msg;
  el.className = `toast show ${type}`;
  setTimeout(() => el.className = 'toast', 2500);
}

// ── NAVIGATION ──────────────────────────────────────────────────────────────

const viewMap = {
  'dashboard': 'view-dashboard',
  'users': 'view-users',
  'add-friendship': 'view-add-friendship',
  'mutual-friends': 'view-mutual-friends',
  'suggestions': 'view-suggestions',
  'bfs-explorer': 'view-bfs-explorer',
  'network-graph': 'view-network-graph'
};

const titleMap = {
  'dashboard': 'Dashboard',
  'users': 'Users',
  'add-friendship': 'Add Friendship',
  'mutual-friends': 'Mutual Friends',
  'suggestions': 'Suggestions',
  'bfs-explorer': 'BFS Explorer',
  'network-graph': 'Network Graph'
};

function navigateTo(viewName) {
  // Hide all views
  document.querySelectorAll('.view-container').forEach(v => v.classList.remove('active'));
  
  // Show selected view
  const targetView = document.getElementById(viewMap[viewName]);
  if (targetView) targetView.classList.add('active');
  
  // Update sidebar active state
  document.querySelectorAll('.nav-item').forEach(btn => btn.classList.remove('active'));
  const activeBtn = document.querySelector(`.nav-item[data-view="${viewName}"]`);
  if (activeBtn) activeBtn.classList.add('active');
  
  // Update title
  setText('current-page-title', titleMap[viewName] || 'Dashboard');
}

document.querySelectorAll('.nav-item').forEach(btn => {
  btn.addEventListener('click', () => {
    const view = btn.getAttribute('data-view');
    navigateTo(view);
    refreshUI();
    // Wake graph animation when navigating to graph view
    if (view === 'network-graph') {
      setTimeout(() => { getGraph()?.wake(); }, 50);
    }
  });
});

// "View all" link on dashboard goes to Users
document.getElementById('link-view-users')?.addEventListener('click', (e) => {
  e.preventDefault();
  navigateTo('users');
  refreshUI();
});

// "Full view" link on dashboard graph goes to Network Graph page
document.getElementById('link-view-graph')?.addEventListener('click', (e) => {
  e.preventDefault();
  navigateTo('network-graph');
  refreshUI();
  setTimeout(() => { getGraph()?.wake(); }, 50);
});

// ── EVENT HANDLERS: DASHBOARD TOOLS ─────────────────────────────────────────

// Add User (Dashboard)
document.getElementById('btn-add-user')?.addEventListener('click', () => {
  const name = document.getElementById('input-full-name').value.trim();
  const username = document.getElementById('input-username').value.trim();

  if (!username) return showToast('toast-add-user', 'Username is required', 'error');

  const success = engine.addUser(username, name);
  if (success) {
    document.getElementById('input-full-name').value = "";
    document.getElementById('input-username').value = "";
    showToast('toast-add-user', `✓ ${username} added to network`);
    refreshUI();
  } else {
    showToast('toast-add-user', 'User already exists', 'error');
  }
});

// Add Friendship (Dashboard)
document.getElementById('btn-add-friend')?.addEventListener('click', () => {
  const uA = document.getElementById('input-friend-a').value.trim();
  const uB = document.getElementById('input-friend-b').value.trim();

  if (!uA || !uB) return showToast('toast-add-friend', 'Both fields are required', 'error');

  const success = engine.addFriend(uA, uB);
  if (success) {
    document.getElementById('input-friend-a').value = "";
    document.getElementById('input-friend-b').value = "";
    showToast('toast-add-friend', `✓ ${uA} ⟷ ${uB} connected`);
    refreshUI();
  } else {
    showToast('toast-add-friend', 'Check usernames or already friends', 'error');
  }
});

document.getElementById('btn-clear-friend')?.addEventListener('click', () => {
  document.getElementById('input-friend-a').value = "";
  document.getElementById('input-friend-b').value = "";
});

// Find Mutuals (Dashboard)
document.getElementById('btn-find-mutuals')?.addEventListener('click', () => {
  const uA = document.getElementById('input-mutual-a').value.trim();
  const uB = document.getElementById('input-mutual-b').value.trim();
  const container = document.getElementById('mutuals-results');
  
  if (!uA || !uB || !container) return;

  const mutuals = engine.findMutualFriends(uA, uB);
  container.innerHTML = "";

  if (mutuals.length === 0) {
    container.innerHTML = `<p class="result-header">${uA} and ${uB} have no mutual friends</p>`;
  } else {
    container.innerHTML = `<p class="result-header">${mutuals.length} mutual friends found</p><div class="chips-row">${
      mutuals.map(m => `<div class="mutual-chip"><div class="chip-avatar">${m.displayName[0].toUpperCase()}</div><span>${m.displayName}</span></div>`).join('')
    }</div>`;
  }
});

// Suggestions (Dashboard)
document.getElementById('btn-run-suggestions')?.addEventListener('click', () => {
  const user = document.getElementById('select-suggest-user').value;
  const container = document.getElementById('suggestions-results');
  if (!user || !container) return;
  runSuggestions(user, container);
});

function runSuggestions(username, container) {
  const suggestions = engine.suggestFriends(username, 4);
  container.innerHTML = "";

  if (suggestions.length === 0) {
    container.innerHTML = '<p class="empty-msg" style="grid-column: span 2">No suggestions available for this user</p>';
  } else {
    const colors = ['#6366f1', '#3b82f6', '#10b981', '#f59e0b', '#ec4899', '#06b6d4'];
    suggestions.forEach(s => {
      const color = colors[s.user.displayName.charCodeAt(0) % colors.length];
      const item = document.createElement('div');
      item.className = 'suggest-item animate-in';
      item.innerHTML = `
        <div class="suggest-avatar" style="background-color: ${color}20; color: ${color}; border-color: ${color}40">${s.user.displayName.substring(0, 2).toUpperCase()}</div>
        <span class="suggest-name">${s.user.displayName}</span>
        <span class="suggest-mutuals">${s.mutualCount} mutuals</span>
        <button class="btn btn-secondary btn-xs add-sug-btn" data-target="${s.user.displayName}" data-source="${username}">+ Connect</button>
      `;
      container.appendChild(item);
    });

    container.querySelectorAll('.add-sug-btn').forEach(btn => {
      btn.addEventListener('click', () => {
        engine.addFriend(btn.dataset.source, btn.dataset.target);
        refreshUI();
        runSuggestions(btn.dataset.source, container);
      });
    });
  }
}

// ── EVENT HANDLERS: DEDICATED PAGES ─────────────────────────────────────────

// Add Friendship Page
document.getElementById('page-add-friend')?.addEventListener('click', () => {
  const uA = document.getElementById('page-friend-a').value.trim();
  const uB = document.getElementById('page-friend-b').value.trim();
  if (!uA || !uB) return showToast('toast-page-friend', 'Both fields are required', 'error');
  const success = engine.addFriend(uA, uB);
  if (success) {
    document.getElementById('page-friend-a').value = "";
    document.getElementById('page-friend-b').value = "";
    showToast('toast-page-friend', `✓ ${uA} ⟷ ${uB} connected`);
    refreshUI();
  } else {
    showToast('toast-page-friend', 'Check usernames or already friends', 'error');
  }
});

document.getElementById('page-clear-friend')?.addEventListener('click', () => {
  document.getElementById('page-friend-a').value = "";
  document.getElementById('page-friend-b').value = "";
});

// Mutual Friends Page
document.getElementById('page-find-mutuals')?.addEventListener('click', () => {
  const uA = document.getElementById('page-mutual-a').value.trim();
  const uB = document.getElementById('page-mutual-b').value.trim();
  const container = document.getElementById('page-mutuals-results');
  if (!uA || !uB || !container) return;

  const mutuals = engine.findMutualFriends(uA, uB);
  container.innerHTML = "";

  if (mutuals.length === 0) {
    container.innerHTML = `<p class="result-header">${uA} and ${uB} have no mutual friends</p>`;
  } else {
    container.innerHTML = `<p class="result-header">${mutuals.length} mutual friends found between <strong>${uA}</strong> and <strong>${uB}</strong></p><div class="chips-row">${
      mutuals.map(m => `<div class="mutual-chip"><div class="chip-avatar">${m.displayName[0].toUpperCase()}</div><span>${m.displayName}</span></div>`).join('')
    }</div>`;
  }
});

// Suggestions Page
document.getElementById('page-run-suggestions')?.addEventListener('click', () => {
  const user = document.getElementById('page-suggest-user').value;
  const container = document.getElementById('page-suggestions-results');
  if (!user || !container) return;
  runSuggestions(user, container);
});

// BFS Explorer
document.getElementById('btn-run-bfs')?.addEventListener('click', () => {
  const startUser = document.getElementById('bfs-start-user').value;
  const container = document.getElementById('bfs-results');
  if (!startUser || !container) return;

  const result = engine.bfs(startUser);
  container.innerHTML = "";

  if (!result || result.length === 0) {
    container.innerHTML = '<p class="empty-msg">No traversal results. Check if user exists.</p>';
    return;
  }

  result.forEach((level, depth) => {
    const levelDiv = document.createElement('div');
    levelDiv.className = 'bfs-level animate-in';
    levelDiv.innerHTML = `
      <div class="bfs-level-header">
        <span class="bfs-depth">Level ${depth}</span>
        <span class="bfs-count">${level.length} node${level.length > 1 ? 's' : ''}</span>
      </div>
      <div class="bfs-nodes">
        ${level.map(name => {
          const colors = ['#6366f1', '#3b82f6', '#10b981', '#f59e0b', '#ec4899', '#06b6d4'];
          const color = colors[name.charCodeAt(0) % colors.length];
          return `<div class="bfs-node" style="border-color: ${color}40">
            <div class="bfs-node-avatar" style="background-color: ${color}20; color: ${color}">${name.substring(0, 2).toUpperCase()}</div>
            <span>${name}</span>
          </div>`;
        }).join('')}
      </div>
    `;
    container.appendChild(levelDiv);
  });
});

// Search (filters dashboard user list)
document.getElementById('global-search')?.addEventListener('input', (e) => {
  renderDashboardUserList(e.target.value);
});

// Users page filter
document.getElementById('users-filter')?.addEventListener('input', (e) => {
  renderFullUserList(e.target.value);
});

// ── GRAPH CONTROLS ──────────────────────────────────────────────────────────

// Reset layout: scatter nodes randomly to restart physics
document.getElementById('btn-graph-reset')?.addEventListener('click', () => {
  const g = getGraph();
  if (!g) return;
  const w = g.canvas.width, h = g.canvas.height;
  g.nodes.forEach(n => {
    n.x = w / 2 + (Math.random() - 0.5) * w * 0.6;
    n.y = h / 2 + (Math.random() - 0.5) * h * 0.6;
    n.vx = (Math.random() - 0.5) * 8;
    n.vy = (Math.random() - 0.5) * 8;
  });
  g.resetHighlight();
  g.start();
});

// BFS animation: step through levels at intervals
let bfsInterval = null;
const bfsBtn = document.getElementById('btn-graph-bfs-toggle');

bfsBtn?.addEventListener('click', () => {
  const g = getGraph();
  if (!g) return;

  // If already running, stop
  if (bfsInterval) {
    clearInterval(bfsInterval);
    bfsInterval = null;
    bfsBtn.textContent = '▶ Animate BFS';
    bfsBtn.classList.remove('running');
    g.resetHighlight();
    g.wake();
    return;
  }

  const user = document.getElementById('graph-bfs-user')?.value;
  if (!user) { alert('Select a user for BFS first'); return; }

  const levels = engine.bfs(user);
  if (!levels.length) return;

  // Convert display name levels → key levels
  const keyLevels = levels.map(lvl =>
    lvl.map(name => name.toLowerCase())
  );

  g.resetHighlight();
  g.highlightBFS(keyLevels);
  g.wake();

  bfsBtn.textContent = '⏹ Stop BFS';
  bfsBtn.classList.add('running');

  bfsInterval = setInterval(() => {
    const hasMore = g.stepBFS();
    g.wake();
    if (!hasMore) {
      clearInterval(bfsInterval);
      bfsInterval = null;
      bfsBtn.textContent = '▶ Animate BFS';
      bfsBtn.classList.remove('running');
    }
  }, 800);
});

// ── BOOT ────────────────────────────────────────────────────────────────────

refreshUI();
console.log("✓ SocialGraph Mini Engine v2.0 Booted");
