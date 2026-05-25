package org.raterr.tvshow.rating.deleteseason

sealed interface DeleteSeasonRatingHandlerError {
    data object TvShowNotFound : DeleteSeasonRatingHandlerError
    data object RatingNotFound : DeleteSeasonRatingHandlerError
}
