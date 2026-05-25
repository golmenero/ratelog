package org.raterr.tvrating

import org.springframework.stereotype.Service

@Service
class TvRatingScoreService {
    companion object {
        fun score(rating: TvRating): Double =
            (rating.directing.value + rating.cinematography.value + rating.acting.value + rating.soundtrack.value + rating.screenplay.value) / 5.0
    }
}
