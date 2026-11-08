(function () {
  function openModal(modal, trigger) {
    if (!modal) return;
    modal.classList.add('visible');
    modal.dispatchEvent(new CustomEvent('modal:open', { bubbles: false, detail: { trigger } }));
  }

  function closeModal(modal) {
    if (!modal) return;
    modal.classList.remove('visible');
    modal.dispatchEvent(new CustomEvent('modal:close', { bubbles: false }));
  }

  function findModal(id) {
    return document.getElementById(id) || document.querySelector('[data-modal="' + id + '"]');
  }

  function closeAllModals() {
    document.querySelectorAll('.modal.visible').forEach(closeModal);
  }

  document.addEventListener('click', function (e) {
    const openTrigger = e.target.closest('[data-open-modal]');
    if (openTrigger) {
      e.preventDefault();
      const modal = findModal(openTrigger.dataset.openModal);
      if (modal) openModal(modal, openTrigger);
      return;
    }

    const closeTrigger = e.target.closest('[data-close-modal]');
    if (closeTrigger) {
      e.preventDefault();
      const modal = closeTrigger.closest('.modal');
      closeModal(modal);
      return;
    }

    const modal = e.target.closest('.modal');
    if (modal && e.target === modal) {
      closeModal(modal);
    }
  }, true);

  document.addEventListener('keydown', function (e) {
    if (e.key === 'Escape') {
      closeAllModals();
    }
  });

  window.Modals = {
    open: function (id, trigger) { openModal(findModal(id), trigger); },
    close: function (id) { closeModal(findModal(id)); },
    closeAll: closeAllModals
  };
})();
