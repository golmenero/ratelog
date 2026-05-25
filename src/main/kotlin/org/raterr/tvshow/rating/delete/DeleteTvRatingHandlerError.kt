package org.raterr.tvshow.rating.delete

sealed interface DeleteTvRatingHandlerError {
    data object TvShowNotFound : DeleteTvRatingHandlerError
    data object RatingNotFound : DeleteTvRatingHandlerError
}
