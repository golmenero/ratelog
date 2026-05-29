ALTER TABLE tv_shows RENAME TO tv;
ALTER TABLE ratings RENAME TO movie_ratings;
ALTER TABLE follows_users RENAME TO users_follows;

ALTER INDEX IF EXISTS tv_shows_pkey RENAME TO tv_pkey;
ALTER INDEX IF EXISTS tv_shows_tmdb_id_key RENAME TO tv_tmdb_id_key;
ALTER INDEX IF EXISTS ratings_pkey RENAME TO movie_ratings_pkey;
ALTER INDEX IF EXISTS follows_users_pkey RENAME TO users_follows_pkey;
ALTER INDEX IF EXISTS follows_users_follower_id_followed_id_key RENAME TO users_follows_follower_id_followed_id_key;

ALTER TABLE tv_ratings DROP CONSTRAINT IF EXISTS tv_ratings_tv_show_id_fkey;
ALTER TABLE tv_ratings ADD CONSTRAINT tv_ratings_tv_show_id_fkey FOREIGN KEY (tv_show_id) REFERENCES tv(id);

ALTER TABLE season_ratings DROP CONSTRAINT IF EXISTS season_ratings_tv_show_id_fkey;
ALTER TABLE season_ratings ADD CONSTRAINT season_ratings_tv_show_id_fkey FOREIGN KEY (tv_show_id) REFERENCES tv(id);

ALTER TABLE tv_follows DROP CONSTRAINT IF EXISTS tv_follows_tv_show_id_fkey;
ALTER TABLE tv_follows ADD CONSTRAINT tv_follows_tv_show_id_fkey FOREIGN KEY (tv_show_id) REFERENCES tv(id);

ALTER TABLE movie_ratings DROP CONSTRAINT IF EXISTS ratings_movie_id_fkey;
ALTER TABLE movie_ratings ADD CONSTRAINT movie_ratings_movie_id_fkey FOREIGN KEY (movie_id) REFERENCES movies(id);

ALTER TABLE movie_ratings DROP CONSTRAINT IF EXISTS ratings_user_id_fkey;
ALTER TABLE movie_ratings ADD CONSTRAINT movie_ratings_user_id_fkey FOREIGN KEY (user_id) REFERENCES users(id);

ALTER TABLE users_follows DROP CONSTRAINT IF EXISTS follows_users_follower_id_fkey;
ALTER TABLE users_follows ADD CONSTRAINT users_follows_follower_id_fkey FOREIGN KEY (follower_id) REFERENCES users(id);

ALTER TABLE users_follows DROP CONSTRAINT IF EXISTS follows_users_followed_id_fkey;
ALTER TABLE users_follows ADD CONSTRAINT users_follows_followed_id_fkey FOREIGN KEY (followed_id) REFERENCES users(id);
