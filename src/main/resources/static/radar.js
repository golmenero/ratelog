(function () {
  const VIEWBOX = 260;
  const CENTER = VIEWBOX / 2;
  const RADIUS = 80;
  const MAX_VALUE = 10;
  const GRID_STEPS = 4;

  const FALLBACK_LABELS = {
    directing: 'Direction',
    cinematography: 'Photography',
    acting: 'Acting',
    soundtrack: 'Soundtrack',
    screenplay: 'Script',
  };

  function polar(angle, radius) {
    return {
      x: CENTER + Math.cos(angle) * radius,
      y: CENTER + Math.sin(angle) * radius,
    };
  }

  function pointFor(index, total, value) {
    const angle = -Math.PI / 2 + (index / total) * Math.PI * 2;
    return polar(angle, (value / MAX_VALUE) * RADIUS);
  }

  function labelPointFor(index, total) {
    const angle = -Math.PI / 2 + (index / total) * Math.PI * 2;
    return polar(angle, RADIUS + 22);
  }

  function escape(value) {
    return String(value).replace(/[&<>"']/g, (c) => ({
      '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;',
    }[c]));
  }

  function getLabels() {
    const out = { ...FALLBACK_LABELS };
    document.querySelectorAll('[data-i18n-key]').forEach((el) => {
      out[el.dataset.i18nKey] = el.textContent.trim();
    });
    return out;
  }

  function buildCategories(container) {
    const raw = container.dataset.categories;
    if (!raw) return [];
    return raw.split(',').map((entry) => {
      const [key, value] = entry.split(':');
      return { key: key.trim(), value: parseFloat(value) };
    });
  }

  function render(container) {
    const categories = buildCategories(container);
    if (categories.length < 3) return;

    const total = categories.length;
    const labels = getLabels();

    const gridLines = [];
    for (let step = 1; step <= GRID_STEPS; step++) {
      const radius = (step / GRID_STEPS) * RADIUS;
      const points = [];
      for (let i = 0; i < total; i++) {
        const angle = -Math.PI / 2 + (i / total) * Math.PI * 2;
        const p = polar(angle, radius);
        points.push(`${p.x.toFixed(2)},${p.y.toFixed(2)}`);
      }
      gridLines.push(`<polygon class="grid-line" points="${points.join(' ')}" />`);
    }

    const axisLines = [];
    for (let i = 0; i < total; i++) {
      const angle = -Math.PI / 2 + (i / total) * Math.PI * 2;
      const p = polar(angle, RADIUS);
      axisLines.push(`<line class="axis-line" x1="${CENTER}" y1="${CENTER}" x2="${p.x.toFixed(2)}" y2="${p.y.toFixed(2)}" />`);
    }

    const valuePoints = categories.map((c, i) => pointFor(i, total, c.value));
    const valuePolygon = valuePoints.map((p) => `${p.x.toFixed(2)},${p.y.toFixed(2)}`).join(' ');

    const vertices = valuePoints.map((p, i) => {
      const cat = categories[i];
      const ariaLabel = `${escape(labels[cat.key] || cat.key)}: ${cat.value.toFixed(2)}`;
      return `<circle class="vertex" cx="${p.x.toFixed(2)}" cy="${p.y.toFixed(2)}" r="3.5" tabindex="0" role="img" aria-label="${ariaLabel}"><title>${ariaLabel}</title></circle>`;
    }).join('');

    const labelNodes = categories.map((cat, i) => {
      const p = labelPointFor(i, total);
      const angle = -Math.PI / 2 + (i / total) * Math.PI * 2;
      const valueAbove = Math.sin(angle) > 0.1;
      const valueY = valueAbove ? p.y - 14 : p.y + 14;
      return `
        <text class="label" x="${p.x.toFixed(2)}" y="${p.y.toFixed(2)}">${escape(labels[cat.key] || cat.key)}</text>
        <text class="label-value" x="${p.x.toFixed(2)}" y="${valueY.toFixed(2)}">${cat.value.toFixed(2)}</text>
      `;
    }).join('');

    container.innerHTML = `
      <svg viewBox="0 0 ${VIEWBOX} ${VIEWBOX}" xmlns="http://www.w3.org/2000/svg" role="img" aria-label="Rating breakdown radar chart">
        ${gridLines.join('')}
        ${axisLines.join('')}
        <polygon class="value-shape" points="${valuePolygon}" />
        ${vertices}
        ${labelNodes}
      </svg>
    `;
  }

  function init() {
    document.querySelectorAll('.rating-radar').forEach(render);
    if (window.lucide) lucide.createIcons();
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init);
  } else {
    init();
  }
})();
