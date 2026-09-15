(function () {
  function init() {
    document.querySelectorAll('.trending-carousel').forEach((carousel) => {
      carousel.addEventListener('wheel', (e) => {
        const delta = e.deltaY !== 0 ? e.deltaY : e.deltaX;
        if (delta === 0) return;
        const atStart = carousel.scrollLeft <= 0 && delta < 0;
        const atEnd = carousel.scrollLeft + carousel.clientWidth >= carousel.scrollWidth - 1 && delta > 0;
        if (atStart || atEnd) return;
        e.preventDefault();
        carousel.scrollLeft += delta;
      }, { passive: false });
    });
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init);
  } else {
    init();
  }
})();
