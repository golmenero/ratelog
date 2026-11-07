package org.ratelog.tvshow.rating.updateseason

sealed interface UpdateSeasonRatingHandlerError {
    data object InvalidRatingValue : UpdateSeasonRatingHandlerError
    data object RatingNotFound : UpdateSeasonRatingHandlerError
    data object SeasonRatingNotFound : UpdateSeasonRatingHandlerError
}
