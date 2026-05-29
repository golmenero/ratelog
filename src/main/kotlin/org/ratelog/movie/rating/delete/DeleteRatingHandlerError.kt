package org.ratelog.movie.rating.delete

sealed interface DeleteRatingHandlerError {
    data object MovieNotFound : DeleteRatingHandlerError
    data object RatingNotFound : DeleteRatingHandlerError
}
