package org.ratelog.movie.rating.update

sealed interface UpdateRatingHandlerError {
    data object InvalidRatingValue : UpdateRatingHandlerError
    data object RatingNotFound : UpdateRatingHandlerError
    data object InvalidReviewTitle : UpdateRatingHandlerError
    data object InvalidReviewText : UpdateRatingHandlerError
}
