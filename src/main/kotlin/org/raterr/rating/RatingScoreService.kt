package org.raterr.rating

import org.raterr.Score
import org.springframework.stereotype.Service

@Service
class RatingScoreService {
    companion object {
        fun score(rating: Rating): Double =
            (rating.directing.value + rating.cinematography.value + rating.acting.value + rating.soundtrack.value + rating.screenplay.value) / 5.0
    }
}
