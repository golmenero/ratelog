const Loader = {
    el: null,

    init() {
        this.el = document.getElementById('loader');
    },

    show() {
        if (!this.el) this.init();
        if (this.el) this.el.classList.add('visible');
    },

    hide() {
        if (!this.el) this.init();
        if (this.el) this.el.classList.remove('visible');
    }
};

window.addEventListener('beforeunload', () => Loader.show());
window.addEventListener('pageshow', () => Loader.hide());
window.addEventListener('load', () => Loader.hide());
