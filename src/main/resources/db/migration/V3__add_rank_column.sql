ALTER TABLE ratings ADD COLUMN IF NOT EXISTS rank INTEGER NOT NULL DEFAULT 0;
ALTER TABLE tv_ratings ADD COLUMN IF NOT EXISTS rank INTEGER NOT NULL DEFAULT 0;

CREATE INDEX idx_ratings_user_rank ON ratings(user_id, rank);
CREATE INDEX idx_tv_ratings_user_rank ON tv_ratings(user_id, rank);
