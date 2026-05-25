package org.raterr.movie.premieres

sealed interface MoviePremieresHandlerError {
    data object MovieNotFound : MoviePremieresHandlerError
}
