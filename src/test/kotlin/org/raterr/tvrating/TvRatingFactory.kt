package org.raterr.tvrating

import org.raterr.Score
import org.raterr.tvshow.TvShow
import org.raterr.user.User
import java.time.Instant

fun aTvRating(
    id: TvRating.Id? = null,
    tvShowId: TvShow.Id = TvShow.Id(1),
    userId: User.Id = User.Id(1),
    directing: Double = 5.0,
    cinematography: Double = 5.0,
    acting: Double = 5.0,
    soundtrack: Double = 5.0,
    screenplay: Double = 5.0,
    createdAt: Instant = Instant.now(),
    rank: TvRating.Rank = TvRating.Rank(0)
): TvRating =
    TvRating(
        id = id,
        tvShowId = tvShowId,
        userId = userId,
        directing = Score(directing),
        cinematography = Score(cinematography),
        acting = Score(acting),
        soundtrack = Score(soundtrack),
        screenplay = Score(screenplay),
        createdAt = createdAt,
        rank = rank
    )
