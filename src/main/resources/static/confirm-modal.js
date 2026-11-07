(function () {
  let pendingForm = null;

  function confirmModal() {
    return document.getElementById('confirmModal');
  }

  function close() {
    const modal = confirmModal();
    if (modal) modal.classList.remove('visible');
    pendingForm = null;
  }

  function init() {
    const modal = confirmModal();
    if (!modal) return;

    document.querySelectorAll('.rating-delete-form').forEach((form) => {
      form.addEventListener('submit', (e) => {
        e.preventDefault();
        pendingForm = form;
        modal.classList.add('visible');
      });
    });

    const btnAccept = document.getElementById('btnAcceptConfirmModal');
    if (btnAccept) {
      btnAccept.addEventListener('click', () => {
        const form = pendingForm;
        close();
        if (form) form.submit();
      });
    }

    const btnCancel = document.getElementById('btnCancelConfirmModal');
    if (btnCancel) {
      btnCancel.addEventListener('click', close);
    }

    const btnClose = document.getElementById('btnCloseConfirmModal');
    if (btnClose) {
      btnClose.addEventListener('click', close);
    }

    modal.addEventListener('click', (e) => {
      e.stopPropagation();
    });
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init);
  } else {
    init();
  }
})();
