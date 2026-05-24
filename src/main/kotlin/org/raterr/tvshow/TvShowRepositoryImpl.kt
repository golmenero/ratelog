package org.raterr.tvshow

import org.raterr.Genre
import org.raterr.Overview
import org.raterr.TmdbId
import org.raterr.Title
import org.raterr.Url
import org.springframework.stereotype.Repository
import java.time.LocalDate
import kotlin.jvm.optionals.getOrNull

@Repository
class TvShowRepositoryImpl(val tvShowDAO: TvShowDAO) : TvShowRepository {
    override fun findById(id: TvShow.Id): TvShow? =
        id.value.let(tvShowDAO::findById).getOrNull()?.toDomain()

    override fun findByTmdbId(tmdbId: TmdbId): TvShow? =
        tmdbId.value.let(tvShowDAO::findByTmdbId).getOrNull()?.toDomain()

    override fun save(show: TvShow): TvShow =
        show.toEntity().let(tvShowDAO::save).toDomain()

    private fun TvShowEntity.toDomain(): TvShow {
        val genres = genres?.split(',')?.map(Genre::valueOf) ?: emptyList()

        return TvShow(
            id = TvShow.Id(id!!),
            tmdbId = TmdbId(tmdbId),
            name = Title(name),
            originalName = originalName?.let { Title(it) },
            overview = overview?.let { Overview(it) },
            firstAirDate = firstAirDate?.let { LocalDate.parse(it) },
            firstAirYear = firstAirYear,
            posterPath = posterPath?.let { Url(it) },
            tmdbVoteAverage = tmdbVoteAverage,
            genres = genres
        )
    }

    private fun TvShow.toEntity(): TvShowEntity {
        return TvShowEntity(
            id = id?.value,
            tmdbId = tmdbId.value,
            name = name.value,
            originalName = originalName?.value,
            overview = overview?.value,
            firstAirDate = firstAirDate?.toString(),
            firstAirYear = firstAirYear,
            posterPath = posterPath?.value,
            tmdbVoteAverage = tmdbVoteAverage,
            genres = genres.joinToString(",")
        )
    }
}
