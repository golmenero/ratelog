CREATE TABLE IF NOT EXISTS season_ratings (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    tv_show_id BIGINT NOT NULL,
    season_number INTEGER NOT NULL,
    user_id BIGINT NOT NULL,
    directing REAL NOT NULL,
    cinematography REAL NOT NULL,
    acting REAL NOT NULL,
    soundtrack REAL NOT NULL,
    screenplay REAL NOT NULL,
    created_at_epoch_ms BIGINT NOT NULL,
    FOREIGN KEY (tv_show_id) REFERENCES tv_shows(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX idx_season_ratings_tv_show_season ON season_ratings(tv_show_id, season_number);
CREATE INDEX idx_season_ratings_user_id ON season_ratings(user_id);

INSERT INTO season_ratings (tv_show_id, season_number, user_id, directing, cinematography, acting, soundtrack, screenplay, created_at_epoch_ms)
SELECT tv_show_id, 1, user_id, directing, cinematography, acting, soundtrack, screenplay, created_at_epoch_ms
FROM tv_ratings;

ALTER TABLE tv_ratings DROP COLUMN IF EXISTS directing;
ALTER TABLE tv_ratings DROP COLUMN IF EXISTS cinematography;
ALTER TABLE tv_ratings DROP COLUMN IF EXISTS acting;
ALTER TABLE tv_ratings DROP COLUMN IF EXISTS soundtrack;
ALTER TABLE tv_ratings DROP COLUMN IF EXISTS screenplay;
