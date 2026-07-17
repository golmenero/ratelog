const SearchInfinite = {
    sentinel: null,
    results: null,
    loading: false,

    init() {
        this.sentinel = document.getElementById('scroll-sentinel');
        this.results = document.getElementById('results');

        if (!this.sentinel || !this.results) return;

        const observer = new IntersectionObserver((entries) => {
            if (entries[0].isIntersecting && !this.loading) {
                this.loadMore();
            }
        }, { rootMargin: '200px' });

        observer.observe(this.sentinel);
    },

    async loadMore() {
        this.loading = true;

        const query = this.sentinel.dataset.query;
        const mediaType = this.sentinel.dataset.mediaType;
        const page = parseInt(this.sentinel.dataset.page) + 1;

        try {
            const params = new URLSearchParams({ q: query, page });
            if (mediaType) params.set('mediaType', mediaType);

            const response = await fetch(`/api/search?${params}`);
            const data = await response.json();

            data.items.forEach(item => this.results.appendChild(this.createCard(item)));

            if (data.hasMore) {
                this.sentinel.dataset.page = page;
            } else {
                this.sentinel.remove();
            }
        } catch (error) {
            console.error('Infinite scroll failed:', error);
        } finally {
            this.loading = false;
        }
    },

    createCard(item) {
        const link = document.createElement('a');
        link.href = item.type === 'movie' ? `/movie/${item.tmdbId}` : `/tv/${item.tmdbId}`;
        link.className = 'result-card';

        const posterFrame = document.createElement('div');
        posterFrame.className = 'poster-frame';

        const typeTag = document.createElement('span');
        typeTag.className = 'tag type-tag';
        typeTag.textContent = item.type === 'movie' ? 'Movie' : 'TV';
        posterFrame.appendChild(typeTag);

        if (item.posterPath) {
            const img = document.createElement('img');
            img.src = `https://image.tmdb.org/t/p/w185${item.posterPath}`;
            img.alt = `Poster of ${item.title}`;
            posterFrame.appendChild(img);
        } else {
            const fallback = document.createElement('div');
            fallback.className = 'poster-fallback';
            fallback.textContent = item.title.charAt(0).toUpperCase();
            posterFrame.appendChild(fallback);
        }

        const content = document.createElement('div');
        content.className = 'result-content';

        const heading = document.createElement('h3');
        const titleSpan = document.createElement('span');
        titleSpan.textContent = item.title;
        heading.appendChild(titleSpan);

        const yearSpan = document.createElement('span');
        yearSpan.className = 'year';
        yearSpan.textContent = item.year || 'Unknown year';
        heading.appendChild(yearSpan);

        content.appendChild(heading);

        const overview = document.createElement('p');
        overview.className = 'result-overview';
        overview.textContent = item.overview || 'No synopsis available right now.';
        content.appendChild(overview);

        link.appendChild(posterFrame);
        link.appendChild(content);

        return link;
    }
};

document.addEventListener('DOMContentLoaded', () => SearchInfinite.init());
