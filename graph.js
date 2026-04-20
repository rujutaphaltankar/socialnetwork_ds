/**
 * graph.js — Interactive Force-Directed Network Graph
 *
 * Algorithm:
 * - Repulsion between all node pairs (Coulomb's law simulation)
 * - Attraction along edges (spring simulation)
 * - Velocity + damping for smooth animation
 * - Canvas 2D rendering with requestAnimationFrame
 */

const COLORS = [
  '#6366f1', '#3b82f6', '#10b981', '#f59e0b',
  '#ec4899', '#06b6d4', '#8b5cf6', '#ef4444'
];

export class NetworkGraph {
  constructor(canvasId) {
    this.canvas = document.getElementById(canvasId);
    this.ctx = this.canvas.getContext('2d');
    this.nodes = [];      // { id, label, x, y, vx, vy, color, radius }
    this.edges = [];      // { source, target }
    this.animId = null;
    this.dragging = null;
    this.hovering = null;
    this.highlighted = null; // highlighted node key from BFS

    // Physics constants
    this.repulsion    = 4000;
    this.attraction   = 0.04;
    this.damping      = 0.82;
    this.centerPull   = 0.008;
    this.minDist      = 60;

    this._bindEvents();
    this._resizeObserver();
  }

  // ── Data ──────────────────────────────────────────────────────────────────

  setData(users, adjacencyList) {
    const w = this.canvas.width;
    const h = this.canvas.height;
    const cx = w / 2, cy = h / 2;

    // Keep existing positions if node already exists
    const existingPos = {};
    this.nodes.forEach(n => { existingPos[n.id] = { x: n.x, y: n.y }; });

    this.nodes = users.map((u, i) => {
      const pos = existingPos[u.username];
      const angle = (i / users.length) * Math.PI * 2;
      const r = Math.min(w, h) * 0.3;
      return {
        id: u.username,
        label: u.displayName,
        friendsCount: u.friendsCount,
        x: pos ? pos.x : cx + Math.cos(angle) * r + (Math.random() - 0.5) * 40,
        y: pos ? pos.y : cy + Math.sin(angle) * r + (Math.random() - 0.5) * 40,
        vx: 0, vy: 0,
        color: COLORS[i % COLORS.length],
        radius: Math.max(22, Math.min(38, 22 + u.friendsCount * 4))
      };
    });

    this.edges = [];
    adjacencyList.forEach((friends, key) => {
      for (const fKey of friends) {
        if (key < fKey) { // avoid duplicates
          this.edges.push({ source: key, target: fKey });
        }
      }
    });

    this.start();
  }

  highlightBFS(levels) {
    // levels: [ [nodeKeys at depth 0], [nodeKeys at depth 1], ... ]
    this._bfsLevels = levels;
    this._bfsStep = -1;
    this._bfsColors = ['#f8fafc', '#6366f1', '#3b82f6', '#10b981', '#f59e0b', '#ec4899'];
    this.nodes.forEach(n => n.bfsColor = null);
  }

  stepBFS() {
    if (!this._bfsLevels) return false;
    this._bfsStep++;
    if (this._bfsStep >= this._bfsLevels.length) {
      this._bfsLevels = null;
      return false;
    }
    const levelKeys = this._bfsLevels[this._bfsStep];
    const color = this._bfsColors[this._bfsStep % this._bfsColors.length];
    this.nodes.forEach(n => {
      if (levelKeys.includes(n.id)) n.bfsColor = color;
    });
    return true;
  }

  resetHighlight() {
    this.nodes.forEach(n => { n.bfsColor = null; n.highlighted = false; });
    this._bfsLevels = null;
  }

  highlightNode(userId) {
    this.nodes.forEach(n => {
      n.highlighted = (n.id === userId.toLowerCase());
    });
  }

  // ── Physics ───────────────────────────────────────────────────────────────

  _tick() {
    const { repulsion, attraction, damping, centerPull, minDist } = this;
    const cx = this.canvas.width / 2;
    const cy = this.canvas.height / 2;

    const nodeMap = {};
    this.nodes.forEach(n => { nodeMap[n.id] = n; });

    // Repulsion between all pairs
    for (let i = 0; i < this.nodes.length; i++) {
      for (let j = i + 1; j < this.nodes.length; j++) {
        const a = this.nodes[i], b = this.nodes[j];
        let dx = b.x - a.x, dy = b.y - a.y;
        let dist = Math.sqrt(dx * dx + dy * dy) || 1;
        if (dist < minDist) dist = minDist;
        const force = repulsion / (dist * dist);
        const fx = (dx / dist) * force;
        const fy = (dy / dist) * force;
        a.vx -= fx; a.vy -= fy;
        b.vx += fx; b.vy += fy;
      }
    }

    // Attraction along edges (spring)
    this.edges.forEach(e => {
      const a = nodeMap[e.source], b = nodeMap[e.target];
      if (!a || !b) return;
      const dx = b.x - a.x, dy = b.y - a.y;
      const dist = Math.sqrt(dx * dx + dy * dy) || 1;
      const force = (dist - 140) * attraction;
      const fx = (dx / dist) * force;
      const fy = (dy / dist) * force;
      a.vx += fx; a.vy += fy;
      b.vx -= fx; b.vy -= fy;
    });

    // Center gravity
    this.nodes.forEach(n => {
      n.vx += (cx - n.x) * centerPull;
      n.vy += (cy - n.y) * centerPull;
    });

    // Integration + damping + boundary
    const pad = 50;
    this.nodes.forEach(n => {
      if (this.dragging === n) { n.vx = 0; n.vy = 0; return; }
      n.vx *= damping;
      n.vy *= damping;
      n.x += n.vx;
      n.y += n.vy;
      n.x = Math.max(pad + n.radius, Math.min(this.canvas.width - pad - n.radius, n.x));
      n.y = Math.max(pad + n.radius, Math.min(this.canvas.height - pad - n.radius, n.y));
    });
  }

  // ── Rendering ─────────────────────────────────────────────────────────────

  _draw() {
    const ctx = this.ctx;
    const w = this.canvas.width, h = this.canvas.height;
    ctx.clearRect(0, 0, w, h);

    const nodeMap = {};
    this.nodes.forEach(n => { nodeMap[n.id] = n; });

    // Draw edges
    this.edges.forEach(e => {
      const a = nodeMap[e.source], b = nodeMap[e.target];
      if (!a || !b) return;

      const isHighlighted = a.highlighted || b.highlighted ||
                            a.bfsColor || b.bfsColor;

      ctx.beginPath();
      ctx.moveTo(a.x, a.y);
      ctx.lineTo(b.x, b.y);
      ctx.strokeStyle = isHighlighted
        ? 'rgba(99, 102, 241, 0.7)'
        : 'rgba(255, 255, 255, 0.08)';
      ctx.lineWidth = isHighlighted ? 2 : 1;
      ctx.stroke();
    });

    // Draw nodes
    this.nodes.forEach(n => {
      const isHovered = this.hovering === n;
      const fillColor = n.bfsColor || n.color;
      const r = n.radius + (isHovered ? 4 : 0);

      // Glow for highlighted / hovered
      if (isHovered || n.highlighted || n.bfsColor) {
        ctx.beginPath();
        ctx.arc(n.x, n.y, r + 10, 0, Math.PI * 2);
        const grad = ctx.createRadialGradient(n.x, n.y, r, n.x, n.y, r + 14);
        grad.addColorStop(0, fillColor + '50');
        grad.addColorStop(1, 'transparent');
        ctx.fillStyle = grad;
        ctx.fill();
      }

      // Node circle
      ctx.beginPath();
      ctx.arc(n.x, n.y, r, 0, Math.PI * 2);
      ctx.fillStyle = fillColor + '22';
      ctx.fill();
      ctx.strokeStyle = fillColor;
      ctx.lineWidth = isHovered ? 3 : 2;
      ctx.stroke();

      // Initials text
      ctx.fillStyle = fillColor;
      ctx.font = `bold ${Math.max(11, r * 0.55)}px Inter, sans-serif`;
      ctx.textAlign = 'center';
      ctx.textBaseline = 'middle';
      ctx.fillText(n.label.substring(0, 2).toUpperCase(), n.x, n.y);

      // Name label
      ctx.fillStyle = isHovered ? '#f8fafc' : 'rgba(248,250,252,0.7)';
      ctx.font = `${isHovered ? 600 : 500} 12px Inter, sans-serif`;
      ctx.textAlign = 'center';
      ctx.textBaseline = 'top';
      ctx.fillText(n.label, n.x, n.y + r + 6);

      // Friend count badge for hovered
      if (isHovered) {
        const badge = `${n.friendsCount} friends`;
        const bw = ctx.measureText(badge).width + 16;
        ctx.fillStyle = '#1e1e28';
        ctx.beginPath();
        ctx.roundRect(n.x - bw / 2, n.y - r - 30, bw, 22, 6);
        ctx.fill();
        ctx.fillStyle = fillColor;
        ctx.font = '11px Inter, sans-serif';
        ctx.textAlign = 'center';
        ctx.textBaseline = 'middle';
        ctx.fillText(badge, n.x, n.y - r - 19);
      }
    });
  }

  // ── Animation loop ────────────────────────────────────────────────────────

  start() {
    if (this.animId) cancelAnimationFrame(this.animId);
    let ticks = 0;
    const loop = () => {
      this._tick();
      this._draw();
      ticks++;
      // After 400 ticks with no interaction, slow down to save CPU
      if (ticks < 400 || this.dragging || this.hovering) {
        this.animId = requestAnimationFrame(loop);
      } else {
        // idle: just draw, no physics
        this.animId = requestAnimationFrame(() => { this._draw(); this.animId = null; });
      }
    };
    this.animId = requestAnimationFrame(loop);
  }

  wake() {
    if (!this.animId) this.start();
  }

  stop() {
    if (this.animId) { cancelAnimationFrame(this.animId); this.animId = null; }
  }

  // ── Events ────────────────────────────────────────────────────────────────

  _nodeAt(x, y) {
    return this.nodes.find(n => {
      const dx = n.x - x, dy = n.y - y;
      return Math.sqrt(dx * dx + dy * dy) <= n.radius + 4;
    }) || null;
  }

  _canvasXY(e) {
    const rect = this.canvas.getBoundingClientRect();
    const clientX = e.touches ? e.touches[0].clientX : e.clientX;
    const clientY = e.touches ? e.touches[0].clientY : e.clientY;
    return { x: clientX - rect.left, y: clientY - rect.top };
  }

  _bindEvents() {
    const c = this.canvas;

    c.addEventListener('mousedown', e => {
      const { x, y } = this._canvasXY(e);
      const node = this._nodeAt(x, y);
      if (node) { this.dragging = node; this.wake(); }
    });

    c.addEventListener('mousemove', e => {
      const { x, y } = this._canvasXY(e);
      if (this.dragging) {
        this.dragging.x = x;
        this.dragging.y = y;
      }
      const prev = this.hovering;
      this.hovering = this._nodeAt(x, y);
      c.style.cursor = this.hovering ? 'grab' : 'default';
      if (this.hovering !== prev) this.wake();
    });

    c.addEventListener('mouseup', () => { this.dragging = null; });
    c.addEventListener('mouseleave', () => { this.dragging = null; this.hovering = null; });
  }

  _resizeObserver() {
    const resize = () => {
      const parent = this.canvas.parentElement;
      this.canvas.width  = parent.clientWidth;
      this.canvas.height = parent.clientHeight;
      this.wake();
    };
    resize();
    new ResizeObserver(resize).observe(this.canvas.parentElement);
  }
}
