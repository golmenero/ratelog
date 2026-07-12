-- Migrate genres from names to TMDB IDs in movies table
UPDATE movies
SET genres = (
    SELECT STRING_AGG(
        CASE
            WHEN g = 'Action' THEN '28'
            WHEN g = 'Adventure' THEN '12'
            WHEN g = 'Animation' THEN '16'
            WHEN g = 'Comedy' THEN '35'
            WHEN g = 'Crime' THEN '80'
            WHEN g = 'Documentary' THEN '99'
            WHEN g = 'Drama' THEN '18'
            WHEN g = 'Family' THEN '10751'
            WHEN g = 'Fantasy' THEN '14'
            WHEN g = 'History' THEN '36'
            WHEN g = 'Horror' THEN '27'
            WHEN g = 'Music' THEN '10402'
            WHEN g = 'Mystery' THEN '9648'
            WHEN g = 'Romance' THEN '10749'
            WHEN g = 'Science Fiction' THEN '878'
            WHEN g = 'TV Movie' THEN '10770'
            WHEN g = 'Thriller' THEN '53'
            WHEN g = 'War' THEN '10752'
            WHEN g = 'Western' THEN '37'
            ELSE g
        END,
        ','
    )
    FROM UNNEST(STRING_TO_ARRAY(movies.genres, ',')) AS g
)
WHERE genres IS NOT NULL AND genres != '';

-- Migrate genres from names to TMDB IDs in tv_shows table
UPDATE tv
SET genres = (
    SELECT STRING_AGG(
        CASE
            WHEN g = 'Action' THEN '28'
            WHEN g = 'Adventure' THEN '12'
            WHEN g = 'Animation' THEN '16'
            WHEN g = 'Comedy' THEN '35'
            WHEN g = 'Crime' THEN '80'
            WHEN g = 'Documentary' THEN '99'
            WHEN g = 'Drama' THEN '18'
            WHEN g = 'Family' THEN '10751'
            WHEN g = 'Fantasy' THEN '14'
            WHEN g = 'History' THEN '36'
            WHEN g = 'Horror' THEN '27'
            WHEN g = 'Music' THEN '10402'
            WHEN g = 'Mystery' THEN '9648'
            WHEN g = 'Romance' THEN '10749'
            WHEN g = 'Science Fiction' THEN '878'
            WHEN g = 'Thriller' THEN '53'
            WHEN g = 'War' THEN '10752'
            WHEN g = 'Western' THEN '37'
            WHEN g = 'Action & Adventure' THEN '10759'
            WHEN g = 'Kids' THEN '10762'
            WHEN g = 'News' THEN '10763'
            WHEN g = 'Reality' THEN '10764'
            WHEN g = 'Sci-Fi & Fantasy' THEN '10765'
            WHEN g = 'Soap' THEN '10766'
            WHEN g = 'Talk' THEN '10767'
            WHEN g = 'War & Politics' THEN '10768'
            ELSE g
        END,
        ','
    )
    FROM UNNEST(STRING_TO_ARRAY(tv.genres, ',')) AS g
)
WHERE genres IS NOT NULL AND genres != '';
