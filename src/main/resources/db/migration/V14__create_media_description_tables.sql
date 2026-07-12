CREATE TABLE movie_descriptions (
    tmdb_id  INTEGER      NOT NULL,
    lang     VARCHAR(10)  NOT NULL,
    title    VARCHAR(255) NOT NULL,
    overview TEXT,
    PRIMARY KEY (tmdb_id, lang),
    CONSTRAINT fk_movie_descriptions_movie
        FOREIGN KEY (tmdb_id) REFERENCES movies(tmdb_id) ON DELETE CASCADE
);

CREATE TABLE tv_descriptions (
    tmdb_id  INTEGER      NOT NULL,
    lang     VARCHAR(10)  NOT NULL,
    name     VARCHAR(255) NOT NULL,
    overview TEXT,
    PRIMARY KEY (tmdb_id, lang),
    CONSTRAINT fk_tv_descriptions_tv
        FOREIGN KEY (tmdb_id) REFERENCES tv(tmdb_id) ON DELETE CASCADE
);
