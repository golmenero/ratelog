package org.ratelog.movie.rating.update

sealed interface UpdateRatingHandlerError {
    data object InvalidRatingValue : UpdateRatingHandlerError
    data object RatingNotFound : UpdateRatingHandlerError
}
