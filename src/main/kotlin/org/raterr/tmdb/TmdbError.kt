package org.raterr.tmdb

import org.raterr.movie.detail.DetailMovieHandlerError
import org.raterr.premieres.ListPremiereHandlerError


interface TmdbError: DetailMovieHandlerError, ListPremiereHandlerError {
    object MovieNotFound: TmdbError
    object TvShowNotFound: TmdbError
}