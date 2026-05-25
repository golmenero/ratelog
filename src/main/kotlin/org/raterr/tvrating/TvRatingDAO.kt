package org.raterr.tvrating

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Table("tv_ratings")
data class TvRatingEntity(
    @Id val id: Long? = null,
    @Column("tv_show_id") val tvShowId: Long,
    @Column("user_id") val userId: Long,
    val directing: Double,
    val cinematography: Double,
    val acting: Double,
    val soundtrack: Double,
    val screenplay: Double,
    @Column("created_at_epoch_ms") val createdAtEpochMs: Long,
    val rank: Int = 0,
)

@Repository
interface TvRatingDAO : CrudRepository<TvRatingEntity, Long> {
    fun findFirstByTvShowId(tvShowId: Long): Optional<TvRatingEntity>
    fun findByTvShowIdAndUserId(tvShowId: Long, userId: Long): List<TvRatingEntity>
    fun findByUserId(userId: Long): List<TvRatingEntity>
    fun findByUserIdOrderByRank(userId: Long): List<TvRatingEntity>
}
