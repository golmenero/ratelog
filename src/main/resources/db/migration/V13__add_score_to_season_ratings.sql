ALTER TABLE season_ratings ADD COLUMN score DOUBLE PRECISION;

UPDATE season_ratings SET score = (directing + cinematography + acting + soundtrack + screenplay) / 5.0;
