package org.raterr.tvshow.rating.add

sealed interface AddTvRatingHandlerError {
    data object InvalidRatingValue : AddTvRatingHandlerError
    data object RatingAlreadyExists : AddTvRatingHandlerError
    data object TvShowNotFound : AddTvRatingHandlerError
}
