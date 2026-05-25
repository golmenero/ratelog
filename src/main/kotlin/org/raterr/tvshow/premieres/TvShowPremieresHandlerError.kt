package org.raterr.tvshow.premieres

sealed interface TvShowPremieresHandlerError {
    data object TvShowNotFound : TvShowPremieresHandlerError
}
