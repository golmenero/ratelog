package org.raterr.tvshow.rating.addseason

sealed interface AddSeasonRatingHandlerError {
    data object InvalidRatingValue : AddSeasonRatingHandlerError
    data object RatingAlreadyExists : AddSeasonRatingHandlerError
    data object TvShowNotFound : AddSeasonRatingHandlerError
}
