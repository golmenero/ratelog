(function () {
  const MAX_VALUE = 10;

  const FALLBACK_LABELS = {
    directing: 'Direction',
    cinematography: 'Photography',
    acting: 'Acting',
    soundtrack: 'Soundtrack',
    screenplay: 'Script',
  };

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
    if (categories.length === 0) return;

    const labels = getLabels();

    const rows = categories.map((cat) => {
      const pct = Math.max(0, Math.min(100, (cat.value / MAX_VALUE) * 100));
      const displayValue = cat.value.toFixed(2).replace('.', ',');
      return `
        <div class="breakdown-row">
          <span class="breakdown-label">${escape(labels[cat.key] || cat.key)}</span>
          <div class="breakdown-track">
            <div class="breakdown-fill" style="width: ${pct.toFixed(2)}%"></div>
          </div>
          <span class="rating sm">${displayValue}</span>
        </div>
      `;
    }).join('');

    container.innerHTML = `<div class="breakdown-list">${rows}</div>`;
  }

  function init() {
    document.querySelectorAll('.rating-breakdown').forEach(render);
    if (window.lucide) lucide.createIcons();
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init);
  } else {
    init();
  }
})();
