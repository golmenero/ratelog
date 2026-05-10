package org.raterr.rating.delete

import org.raterr.TmdbId
import org.raterr.UserId

data class DeleteRating(
    val tmdbId: TmdbId,
    val userId: UserId,
)
