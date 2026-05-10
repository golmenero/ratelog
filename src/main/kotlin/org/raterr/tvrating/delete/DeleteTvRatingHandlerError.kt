package org.raterr.tvrating.delete

sealed interface DeleteTvRatingHandlerError {
    data object TvShowNotFound : DeleteTvRatingHandlerError
    data object RatingNotFound : DeleteTvRatingHandlerError
}
