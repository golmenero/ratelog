package org.raterr.tvrating.add

sealed interface AddTvRatingHandlerError {
    data object InvalidRatingValue : AddTvRatingHandlerError
    data object RatingAlreadyExists : AddTvRatingHandlerError
    data object TvShowNotFound : AddTvRatingHandlerError
}
