(function () {
  const CATEGORIES = ['directing', 'cinematography', 'acting', 'soundtrack', 'screenplay'];
  const DEFAULT_SCORE = 5;

  function ratingModal() {
    return document.getElementById('rateModal');
  }

  function ratingForm() {
    const modal = ratingModal();
    return modal ? modal.querySelector('form') : null;
  }

  function setMode(mode) {
    const form = ratingForm();
    if (!form) return;
    const action = mode === 'edit' ? form.dataset.updateAction : form.dataset.createAction;
    if (action) form.setAttribute('action', action);
  }

  function setScore(name, value) {
    if (value === undefined || value === null || value === '') return;
    const slider = document.querySelector(`#rateModal .rate-slider[data-name="${name}"]`);
    if (slider && window.rateLogSliders) window.rateLogSliders.set(slider, value);
  }

  function setReview(value) {
    const review = document.querySelector('#rateModal textarea[name="review"]');
    if (review) review.value = value || '';
  }

  function setSeason(value) {
    const season = document.getElementById('seasonNumber');
    if (season && value !== undefined && value !== null && value !== '') season.value = value;
  }

  function reset() {
    CATEGORIES.forEach((name) => setScore(name, DEFAULT_SCORE));
    setReview('');
  }

  function fill(data) {
    CATEGORIES.forEach((name) => setScore(name, data[name]));
    setReview(data.review);
  }

  function init() {
    const modal = ratingModal();
    if (!modal) return;

    const btnOpen = document.getElementById('btnOpenRateModal');
    if (btnOpen) {
      btnOpen.addEventListener('click', () => {
        setMode('create');
        reset();
        modal.classList.add('visible');
      });
    }

    document.querySelectorAll('.season-rate-btn').forEach((btn) => {
      btn.addEventListener('click', () => {
        setMode('create');
        setSeason(btn.getAttribute('data-season'));
        reset();
        modal.classList.add('visible');
      });
    });

    document.querySelectorAll('.edit-rating-btn').forEach((btn) => {
      btn.addEventListener('click', () => {
        setMode('edit');
        setSeason(btn.getAttribute('data-season'));
        fill(btn.dataset);
        modal.classList.add('visible');
      });
    });

    const btnClose = document.getElementById('btnCloseModal');
    if (btnClose) {
      btnClose.addEventListener('click', () => modal.classList.remove('visible'));
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
