(function () {
  function init() {
    document.querySelectorAll('.trending-carousel').forEach((carousel) => {
      carousel.addEventListener('wheel', (e) => {
        if (e.deltaY === 0) return;
        const atStart = carousel.scrollLeft <= 0 && e.deltaY < 0;
        const atEnd = carousel.scrollLeft + carousel.clientWidth >= carousel.scrollWidth - 1 && e.deltaY > 0;
        if (atStart || atEnd) return;
        e.preventDefault();
        carousel.scrollLeft += e.deltaY;
      }, { passive: false });
    });
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init);
  } else {
    init();
  }
})();
