package org.ratelog.tvshow.rating.addseason

sealed interface AddSeasonRatingHandlerError {
    data object InvalidRatingValue : AddSeasonRatingHandlerError
    data object RatingAlreadyExists : AddSeasonRatingHandlerError
    data object TvShowNotFound : AddSeasonRatingHandlerError
    data object InvalidReviewTitle : AddSeasonRatingHandlerError
    data object InvalidReviewText : AddSeasonRatingHandlerError
}
