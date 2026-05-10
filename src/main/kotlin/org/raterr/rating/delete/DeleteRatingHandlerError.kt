package org.raterr.rating.delete

sealed interface DeleteRatingHandlerError {
    data object MovieNotFound : DeleteRatingHandlerError
    data object RatingNotFound : DeleteRatingHandlerError
}
