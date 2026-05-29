package org.ratelog.movie.rating.add

sealed interface AddRatingHandlerError {
    data object InvalidRatingValue : AddRatingHandlerError
    data object RatingAlreadyExists : AddRatingHandlerError
    data object MovieNotFound : AddRatingHandlerError
}
