package org.ratelog.tvshow.detail

import arrow.core.Either
import arrow.core.raise.either
import org.ratelog.Lang
import org.ratelog.TmdbId
import org.ratelog.tvshow.TvDescriptionRepository
import org.ratelog.tvshow.TvShow
import org.ratelog.tvshow.TvShowRepository
import org.ratelog.tmdb.TmdbClient
import org.ratelog.tvshow.rating.TvRatingRepository
import org.ratelog.user.User
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

data class GetTvShowDetail(
    val userId: User.Id,
    val tmdbId: TmdbId,
    val lang: Lang,
    )

data class GetTvShowDetailResult(
    val id: Long,
    val tmdbId: Int,
    val title: String,
    val originalTitle: String,
    val overview: String?,
    val releaseDate: String?,
    val firstAirYear: Int?,
    val posterPath: String?,
    val tmdbVoteAverage: Double?,
    val genres: List<String>,
    val status: String?,
    val lastSeasonNumber: Int?,
    val lastSeasonAirDate: String?,
    val nextSeasonAirDate: String?,
    val seasons: List<SeasonInfo>,
    val overallScore: Double?,
    val isRated: Boolean,
    val isFollowed: Boolean,
)

data class SeasonInfo(
    val seasonNumber: Int,
    val rating: SeasonRatingInfo?,
)

data class SeasonRatingInfo(
    val seasonNumber: Int,
    val directing: Double,
    val cinematography: Double,
    val acting: Double,
    val soundtrack: Double,
    val screenplay: Double,
    val score: Double,
    val review: String?,
)

@Component
class DetailTvShowHandler(
    private val tmdbClient: TmdbClient,
    private val tvShowRepository: TvShowRepository,
    private val tvDescriptionRepository: TvDescriptionRepository,
    private val tvRatingRepository: TvRatingRepository,
) {
    @Transactional
    fun handle(query: GetTvShowDetail): Either<DetailTvShowHandlerError, GetTvShowDetailResult> = either {
        val tmdbShow = tmdbClient.tvShowDetails(query.tmdbId)
            .mapLeft { DetailTvShowHandlerError.TvShowNotFound }
            .bind()

        val show = query.tmdbId.let(tvShowRepository::findByTmdbId)
        val savedShow = show ?: tmdbShow.let(tvShowRepository::save)

        if (!tvDescriptionRepository.existsAnyByTmdbId(savedShow.tmdbId)) {
            tmdbClient.tvTranslations(savedShow.tmdbId, savedShow.originalName).fold(
                { },
                { tvDescriptionRepository.saveAll(it) }
            )
        }

        val description = tvDescriptionRepository.findByTmdbIdAndLang(savedShow.tmdbId, query.lang)
        val title = description?.name?.value ?: savedShow.originalName.value
        val overview = description?.overview?.value

        val rating = tvRatingRepository.findByTvShowIdAndUserId(savedShow.id!!, query.userId)
        val isFollowed = tvShowRepository.isFollowed(query.userId, savedShow.id)
        val ratingMap = rating?.seasonRatings?.associateBy { it.seasonNumber.value } ?: emptyMap()

        val lastSeason = savedShow.lastSeasonNumber ?: 0
        val seasons = (1..lastSeason)
            .map { seasonNumber ->
                val seasonRating = ratingMap[seasonNumber]
                val ratingInfo = seasonRating?.let { sr ->
                    SeasonRatingInfo(
                        seasonNumber = sr.seasonNumber.value,
                        directing = sr.directing.value,
                        cinematography = sr.cinematography.value,
                        acting = sr.acting.value,
                        soundtrack = sr.soundtrack.value,
                        screenplay = sr.screenplay.value,
                        score = sr.score.value,
                        review = sr.review?.value,
                    )
                }
                SeasonInfo(
                    seasonNumber = seasonNumber,
                    rating = ratingInfo,
                )
            }

        GetTvShowDetailResult(
            id = savedShow.id.value,
            tmdbId = savedShow.tmdbId.value,
            title = title,
            originalTitle = savedShow.originalName.value,
            overview = overview,
            releaseDate = savedShow.firstAirDate?.toString(),
            firstAirYear = savedShow.firstAirYear,
            posterPath = savedShow.posterPath?.value,
            tmdbVoteAverage = savedShow.tmdbVoteAverage,
            genres = savedShow.genres.map { it.value },
            status = savedShow.status?.value,
            lastSeasonNumber = savedShow.lastSeasonNumber,
            lastSeasonAirDate = savedShow.lastSeasonAirDate?.toString(),
            nextSeasonAirDate = savedShow.nextSeasonAirDate?.toString(),
            seasons = seasons,
            overallScore = rating?.score?.value,
            isRated = rating != null,
            isFollowed = isFollowed,
        )
    }
}
