ALTER TABLE movie_ratings ADD COLUMN IF NOT EXISTS review_title TEXT;
ALTER TABLE season_ratings ADD COLUMN IF NOT EXISTS review_title TEXT;
