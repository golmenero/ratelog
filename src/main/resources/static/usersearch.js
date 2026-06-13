const UserSearch = {
    input: null,
    dropdown: null,
    debounceTimer: null,

    init(inputId, dropdownId) {
        this.input = document.getElementById(inputId);
        this.dropdown = document.getElementById(dropdownId);

        if (!this.input || !this.dropdown) return;

        this.input.addEventListener('keydown', (e) => {
            if (e.key === 'Enter') {
                e.preventDefault();
                const query = this.input.value.trim();
                if (query.length > 0) {
                    this.search(query);
                }
            }
        });

        this.input.addEventListener('input', () => {
            clearTimeout(this.debounceTimer);
            const query = this.input.value.trim();
            if (query.length === 0) {
                this.hideDropdown();
                return;
            }
            this.debounceTimer = setTimeout(() => this.search(query), 300);
        });

        document.addEventListener('click', (e) => {
            if (!this.input.contains(e.target) && !this.dropdown.contains(e.target)) {
                this.hideDropdown();
            }
        });
    },

    async search(query) {
        try {
            const response = await fetch(`/api/users/search?q=${encodeURIComponent(query)}`);
            const users = await response.json();
            this.renderDropdown(users);
        } catch (error) {
            console.error('User search failed:', error);
            this.hideDropdown();
        }
    },

    renderDropdown(users) {
        this.dropdown.innerHTML = '';

        if (!users || users.length === 0) {
            const emptyItem = document.createElement('div');
            emptyItem.className = 'search-dropdown-empty';
            emptyItem.textContent = 'No users found';
            this.dropdown.appendChild(emptyItem);
            this.showDropdown();
            return;
        }

        users.forEach(user => {
            const link = document.createElement('a');
            link.className = 'search-dropdown-item';
            link.href = `/profile/${user.id}`;
            link.textContent = user.username;
            this.dropdown.appendChild(link);
        });

        this.showDropdown();
    },

    showDropdown() {
        this.dropdown.classList.add('visible');
    },

    hideDropdown() {
        this.dropdown.classList.remove('visible');
    }
};

document.addEventListener('DOMContentLoaded', () => {
    UserSearch.init('user-search-input', 'user-search-dropdown');
});
