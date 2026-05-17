package org.raterr.tmdb

import org.raterr.movie.get.GetMovieHandlerError
import org.raterr.movie.premieres.MoviePremieresHandlerError
import org.raterr.search.SearchHandlerError
import org.raterr.tvshow.get.GetTvShowHandlerError
import org.raterr.tvshow.premieres.TvShowPremieresHandlerError

interface TmdbError:
    GetMovieHandlerError,
    GetTvShowHandlerError,
    SearchHandlerError,
    MoviePremieresHandlerError,
    TvShowPremieresHandlerError {
    object MovieNotFound: TmdbError
    object TvShowNotFound: TmdbError
}
