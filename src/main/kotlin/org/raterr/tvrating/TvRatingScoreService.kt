package org.raterr.tvrating

import org.springframework.stereotype.Service

@Service
class TvRatingScoreService {
    companion object {
        fun score(rating: TvRating): Double =
            (rating.directing + rating.cinematography + rating.acting + rating.soundtrack + rating.screenplay) / 5.0
    }
}
