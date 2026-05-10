package org.raterr.tmdb

import org.raterr.movie.get.GetMovieHandlerError
import org.raterr.premieres.ListPremiereHandlerError
import org.raterr.search.SearchHandlerError
import org.raterr.tvshow.get.GetTvShowHandlerError

interface TmdbError:
    GetMovieHandlerError,
    GetTvShowHandlerError,
    SearchHandlerError,
    ListPremiereHandlerError {
    object MovieNotFound: TmdbError
    object TvShowNotFound: TmdbError
}
