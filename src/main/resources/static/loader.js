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

document.addEventListener('DOMContentLoaded', () => {
    Loader.init();

    document.querySelectorAll('form').forEach(form => {
        form.addEventListener('submit', () => Loader.show());
    });

    window.addEventListener('load', () => Loader.hide());
});
