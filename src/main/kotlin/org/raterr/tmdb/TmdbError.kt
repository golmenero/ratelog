package org.raterr.tmdb

import org.raterr.movie.get.GetMovieHandlerError
import org.raterr.premieres.ListPremiereHandlerError
import org.raterr.search.SearchHandlerError

interface TmdbError:
    GetMovieHandlerError,
    SearchHandlerError,
    ListPremiereHandlerError {
    object MovieNotFound: TmdbError
    object TvShowNotFound: TmdbError
}
