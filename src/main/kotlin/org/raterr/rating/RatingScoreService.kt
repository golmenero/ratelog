package org.raterr.rating

import org.springframework.stereotype.Service

@Service
class RatingScoreService {
    companion object {
        fun score(rating: Rating): Double =
            (rating.directing + rating.cinematography + rating.acting + rating.soundtrack + rating.screenplay) / 5.0

        fun score(directing: Double, cinematography: Double, acting: Double, soundtrack: Double, screenplay: Double): Double =
            (directing + cinematography + acting + soundtrack + screenplay) / 5.0
    }
}