package org.ratelog.customlist.getlistdetail

import arrow.core.Either
import arrow.core.raise.either
import org.ratelog.MediaType
import org.ratelog.customlist.CustomList
import org.ratelog.customlist.CustomListItem
import org.ratelog.customlist.CustomListRepository
import org.ratelog.movie.Movie
import org.ratelog.movie.MovieRepository
import org.ratelog.tvshow.TvShow
import org.ratelog.tvshow.TvShowRepository
import org.ratelog.user.User
import org.springframework.stereotype.Service

sealed interface GetListDetailError {
    data object ListNotFound : GetListDetailError
    data object NotAuthorized : GetListDetailError
}

data class GetListDetailQuery(
    val listId: CustomList.Id,
    val userId: User.Id?
)

data class ListDetail(
    val id: CustomList.Id,
    val userId: User.Id,
    val name: String,
    val isPublic: Boolean,
    val createdAtEpochMs: Long,
    val items: List<ListDetailItem>,
)

data class ListDetailItem(
    val id: CustomListItem.Id,
    val mediaId: Long,
    val tmdbId: Int,
    val mediaType: String,
    val position: Int,
    val title: String,
    val posterPath: String?,
    val year: Int?,
    val score: Double?,
    val isFollowed: Boolean,
)

@Service
class GetListDetailHandler(
    private val customListRepository: CustomListRepository,
    private val movieRepository: MovieRepository,
    private val tvShowRepository: TvShowRepository,
) {
    fun handle(query: GetListDetailQuery): Either<GetListDetailError, ListDetail> = either {
        val list = customListRepository.findById(query.listId)
            ?: raise(GetListDetailError.ListNotFound)

        if (!list.isPublic && list.userId != query.userId) {
            raise(GetListDetailError.NotAuthorized)
        }

        val items = list.items.map { item ->
            toDetailItem(item, list.userId, query.userId)
        }

        ListDetail(
            id = list.id!!,
            userId = list.userId,
            name = list.name.value,
            isPublic = list.isPublic,
            createdAtEpochMs = list.createdAtEpochMs,
            items = items,
        )
    }

    private fun toDetailItem(item: CustomListItem, listOwnerId: User.Id, loggedUserId: User.Id?): ListDetailItem {
        return when (item.mediaType) {
            MediaType.movie -> {
                val movie = movieRepository.findById(Movie.Id(item.mediaId))
                val isFollowed = loggedUserId?.let { movie?.id?.let { movieId -> movieRepository.isFollowed(it, movieId) } } ?: false
                ListDetailItem(
                    id = item.id!!,
                    mediaId = item.mediaId,
                    tmdbId = movie?.tmdbId?.value ?: 0,
                    mediaType = item.mediaType.name,
                    position = item.position,
                    title = movie?.originalTitle?.value ?: "Unknown",
                    posterPath = movie?.posterPath?.value,
                    year = movie?.releaseYear,
                    score = movie?.tmdbVoteAverage,
                    isFollowed = isFollowed,
                )
            }
            MediaType.tvshow -> {
                val tvShow = tvShowRepository.findById(TvShow.Id(item.mediaId))
                val isFollowed = loggedUserId?.let { tvShow?.id?.let { showId -> tvShowRepository.isFollowed(it, showId) } } ?: false
                ListDetailItem(
                    id = item.id!!,
                    mediaId = item.mediaId,
                    tmdbId = tvShow?.tmdbId?.value ?: 0,
                    mediaType = item.mediaType.name,
                    position = item.position,
                    title = tvShow?.originalName?.value ?: "Unknown",
                    posterPath = tvShow?.posterPath?.value,
                    year = tvShow?.firstAirYear,
                    score = tvShow?.tmdbVoteAverage,
                    isFollowed = isFollowed,
                )
            }
        }
    }
}
