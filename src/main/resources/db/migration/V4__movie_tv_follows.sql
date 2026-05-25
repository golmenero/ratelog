CREATE TABLE IF NOT EXISTS movie_follows (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id BIGINT NOT NULL,
    movie_id BIGINT NOT NULL,
    created_at_epoch_ms BIGINT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (movie_id) REFERENCES movies(id),
    UNIQUE(user_id, movie_id)
);

CREATE TABLE IF NOT EXISTS tv_follows (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id BIGINT NOT NULL,
    tv_show_id BIGINT NOT NULL,
    created_at_epoch_ms BIGINT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (tv_show_id) REFERENCES tv_shows(id),
    UNIQUE(user_id, tv_show_id)
);

INSERT INTO movie_follows (user_id, movie_id, created_at_epoch_ms)
SELECT f.user_id, m.id, f.created_at_epoch_ms
FROM follows f
JOIN movies m ON m.tmdb_id = f.content_tmdb_id
WHERE f.content_type = 'movie';

INSERT INTO tv_follows (user_id, tv_show_id, created_at_epoch_ms)
SELECT f.user_id, t.id, f.created_at_epoch_ms
FROM follows f
JOIN tv_shows t ON t.tmdb_id = f.content_tmdb_id
WHERE f.content_type = 'tvshow';

DROP TABLE IF EXISTS follows;
