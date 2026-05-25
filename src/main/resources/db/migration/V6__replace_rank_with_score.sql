ALTER TABLE ratings DROP COLUMN IF EXISTS rank;
ALTER TABLE tv_ratings DROP COLUMN IF EXISTS rank;

DROP INDEX IF EXISTS idx_ratings_user_rank;
DROP INDEX IF EXISTS idx_tv_ratings_user_rank;

ALTER TABLE ratings ADD COLUMN score DOUBLE PRECISION;
ALTER TABLE tv_ratings ADD COLUMN score DOUBLE PRECISION;

UPDATE ratings SET score = (directing + cinematography + acting + soundtrack + screenplay) / 5.0;

UPDATE tv_ratings tr
SET score = (
    SELECT (sr.directing + sr.cinematography + sr.acting + sr.soundtrack + sr.screenplay) / 5.0
    FROM season_ratings sr
    WHERE sr.tv_show_id = tr.tv_show_id
      AND sr.user_id = tr.user_id
      AND sr.season_number = 1
    LIMIT 1
);

ALTER TABLE ratings ALTER COLUMN score SET NOT NULL;
ALTER TABLE tv_ratings ALTER COLUMN score SET NOT NULL;
