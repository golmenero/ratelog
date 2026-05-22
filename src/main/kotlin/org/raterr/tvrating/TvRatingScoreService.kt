package org.raterr.tvrating

import org.springframework.stereotype.Service

@Service
class TvRatingScoreService {
    companion object {
        fun score(rating: TvRating): Double =
            (rating.directing + rating.cinematography + rating.acting + rating.soundtrack + rating.screenplay) / 5.0

        fun score(directing: Double, cinematography: Double, acting: Double, soundtrack: Double, screenplay: Double): Double =
            (directing + cinematography + acting + soundtrack + screenplay) / 5.0
    }
}
