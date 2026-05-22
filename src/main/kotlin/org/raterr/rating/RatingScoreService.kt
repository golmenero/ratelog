package org.raterr.rating

import org.springframework.stereotype.Service

@Service
class RatingScoreService {
    companion object {
        fun score(rating: Rating): Double =
            (rating.directing + rating.cinematography + rating.acting + rating.soundtrack + rating.screenplay) / 5.0
    }
}