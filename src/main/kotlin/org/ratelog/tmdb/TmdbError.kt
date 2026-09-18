package org.ratelog.tmdb

import org.ratelog.movie.detail.DetailMovieHandlerError
import org.ratelog.movie.premieres.MoviePremieresHandlerError
import org.ratelog.search.SearchHandlerError
import org.ratelog.search.trending.SearchTrendingHandlerError
import org.ratelog.tvshow.detail.DetailTvShowHandlerError
import org.ratelog.tvshow.premieres.TvShowPremieresHandlerError

interface TmdbError:
    DetailMovieHandlerError,
    DetailTvShowHandlerError,
    SearchHandlerError,
    SearchTrendingHandlerError,
    MoviePremieresHandlerError,
    TvShowPremieresHandlerError {
    object MovieNotFound: TmdbError
    object TvShowNotFound: TmdbError
    object ApiKeyMissing: TmdbError
}
