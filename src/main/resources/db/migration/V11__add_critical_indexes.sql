DROP INDEX IF EXISTS idx_ratings_user_rank;
DROP INDEX IF EXISTS idx_tv_ratings_user_rank;
DROP INDEX IF EXISTS idx_season_ratings_tv_show_season;
DROP INDEX IF EXISTS idx_season_ratings_user_id;

CREATE INDEX idx_movie_ratings_user_created ON movie_ratings(user_id, created_at_epoch_ms DESC);
CREATE INDEX idx_tv_ratings_user_created ON tv_ratings(user_id, created_at_epoch_ms DESC);

CREATE INDEX idx_movie_follows_user ON movie_follows(user_id);
CREATE INDEX idx_tv_follows_user ON tv_follows(user_id);
CREATE INDEX idx_users_follows_follower ON users_follows(follower_id);
