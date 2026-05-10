package org.raterr.rating.add

import org.raterr.TmdbId
import org.raterr.UserId

data class AddRating(
    val tmdbId: TmdbId,
    val userId: UserId,
    val directing: Double,
    val cinematography: Double,
    val acting: Double,
    val soundtrack: Double,
    val screenplay: Double,
)
