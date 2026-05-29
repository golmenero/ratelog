package org.ratelog.movie.premieres

interface MoviePremieresHandlerError {
    data object MovieNotFound : MoviePremieresHandlerError
}
