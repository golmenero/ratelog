ALTER TABLE movie_ratings ADD COLUMN IF NOT EXISTS review_text TEXT;
ALTER TABLE season_ratings ADD COLUMN IF NOT EXISTS review_text TEXT;
