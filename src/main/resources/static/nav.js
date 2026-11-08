(function () {
  const btn = document.getElementById('btnNavSettings');
  const sheet = document.getElementById('navSettingsSheet');
  const backdrop = document.getElementById('navSheetBackdrop');
  const btnClose = document.getElementById('btnNavSettingsClose');
  if (!btn || !sheet || !backdrop) return;

  function isOpen() {
    return btn.getAttribute('aria-expanded') === 'true';
  }

  function open() {
    backdrop.hidden = false;
    sheet.hidden = false;
    void sheet.offsetWidth;
    backdrop.classList.add('open');
    sheet.classList.add('open');
    btn.setAttribute('aria-expanded', 'true');
  }

  function close() {
    if (!isOpen()) return;
    function onTransitionEnd(e) {
      if (e.propertyName !== 'transform') return;
      sheet.removeEventListener('transitionend', onTransitionEnd);
      sheet.hidden = true;
      backdrop.hidden = true;
    }
    sheet.addEventListener('transitionend', onTransitionEnd);
    backdrop.classList.remove('open');
    sheet.classList.remove('open');
    btn.setAttribute('aria-expanded', 'false');
  }

  btn.addEventListener('click', () => isOpen() ? close() : open());
  if (btnClose) btnClose.addEventListener('click', close);
  backdrop.addEventListener('click', close);
  document.addEventListener('keydown', (e) => { if (e.key === 'Escape') close(); });
})();
