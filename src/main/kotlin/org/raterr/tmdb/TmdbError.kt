package org.raterr.tmdb

import org.raterr.movie.detail.DetailMovieHandlerError
import org.raterr.movie.premieres.MoviePremieresHandlerError
import org.raterr.search.SearchHandlerError
import org.raterr.tvshow.detail.DetailTvShowHandlerError
import org.raterr.tvshow.premieres.TvShowPremieresHandlerError

interface TmdbError:
    DetailMovieHandlerError,
    DetailTvShowHandlerError,
    SearchHandlerError,
    MoviePremieresHandlerError,
    TvShowPremieresHandlerError {
    object MovieNotFound: TmdbError
    object TvShowNotFound: TmdbError
}
