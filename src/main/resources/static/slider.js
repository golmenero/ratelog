(function () {
  function clamp(value, min, max) {
    return Math.min(Math.max(value, min), max);
  }

  function snap(value, min, step) {
    return Math.round((value - min) / step) * step + min;
  }

  function setValue(slider, rawValue) {
    const min = parseFloat(slider.dataset.min);
    const max = parseFloat(slider.dataset.max);
    const step = parseFloat(slider.dataset.step);
    const value = clamp(snap(parseFloat(rawValue), min, step), min, max);
    const percent = ((value - min) / (max - min)) * 100;

    const fill = slider.querySelector('.rate-slider-fill');
    const thumb = slider.querySelector('.rate-slider-thumb');
    const valueEl = slider.querySelector('.rate-slider-value');
    const input = slider.querySelector('input[type="hidden"]');

    if (fill) fill.style.width = percent + '%';
    if (thumb) {
      thumb.style.left = percent + '%';
      thumb.setAttribute('aria-valuenow', value.toFixed(2));
    }
    if (valueEl) valueEl.textContent = value.toFixed(2);
    if (input) input.value = value.toFixed(2);

    slider.dataset.value = value.toFixed(2);
  }

  function valueFromPointer(slider, clientX) {
    const track = slider.querySelector('.rate-slider-track');
    const rect = track.getBoundingClientRect();
    const ratio = clamp((clientX - rect.left) / rect.width, 0, 1);
    const min = parseFloat(slider.dataset.min);
    const max = parseFloat(slider.dataset.max);
    return min + ratio * (max - min);
  }

  function init(slider) {
    setValue(slider, slider.dataset.value || slider.dataset.min);

    const thumb = slider.querySelector('.rate-slider-thumb');
    if (thumb) {
      thumb.setAttribute('role', 'slider');
      thumb.setAttribute('tabindex', '0');
      thumb.setAttribute('aria-valuemin', slider.dataset.min);
      thumb.setAttribute('aria-valuemax', slider.dataset.max);
      thumb.setAttribute('aria-valuenow', slider.dataset.value || slider.dataset.min);
      thumb.setAttribute('aria-label', slider.dataset.name || 'rating');
    }

    let dragging = false;

    function updateFromEvent(e) {
      const clientX = e.touches ? e.touches[0].clientX : e.clientX;
      setValue(slider, valueFromPointer(slider, clientX));
    }

    function start(e) {
      dragging = true;
      updateFromEvent(e);
      e.preventDefault();
    }

    function move(e) {
      if (!dragging) return;
      updateFromEvent(e);
    }

    function end() {
      dragging = false;
    }

    slider.addEventListener('mousedown', start);
    slider.addEventListener('touchstart', start, { passive: false });
    window.addEventListener('mousemove', move);
    window.addEventListener('touchmove', move, { passive: false });
    window.addEventListener('mouseup', end);
    window.addEventListener('touchend', end);

    if (thumb) {
      thumb.addEventListener('keydown', (e) => {
        const min = parseFloat(slider.dataset.min);
        const max = parseFloat(slider.dataset.max);
        const step = parseFloat(slider.dataset.step);
        const current = parseFloat(slider.dataset.value);
        const map = {
          ArrowLeft: -step,
          ArrowRight: step,
          ArrowDown: -step,
          ArrowUp: step,
          Home: min - current,
          End: max - current,
          PageDown: -step * 4,
          PageUp: step * 4,
        };
        if (map[e.key] !== undefined) {
          e.preventDefault();
          setValue(slider, current + map[e.key]);
        }
      });
    }
  }

  function initAll() {
    document.querySelectorAll('.rate-slider').forEach(init);
    bindOverallSummary();
  }

  function bindOverallSummary() {
    const overall = document.getElementById('ratingOverallValue');
    if (!overall) return;
    const sliders = document.querySelectorAll('#rateModal .rate-slider');
    if (!sliders.length) return;

    function updateOverall() {
      const values = Array.from(sliders).map((s) => parseFloat(s.dataset.value) || 0);
      const avg = values.reduce((sum, v) => sum + v, 0) / values.length;
      overall.textContent = avg.toFixed(2);
    }

    const observer = new MutationObserver(updateOverall);
    sliders.forEach((s) => {
      observer.observe(s, { attributes: true, attributeFilter: ['data-value'] });
    });
    updateOverall();
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', initAll);
  } else {
    initAll();
  }
})();
