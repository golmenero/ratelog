package org.raterr.rating

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Table("ratings")
data class RatingEntity(
    @Id val id: Long? = null,
    @Column("movie_id") val movieId: Long,
    @Column("user_id") val userId: Long,
    val directing: Double,
    val cinematography: Double,
    val acting: Double,
    val soundtrack: Double,
    val screenplay: Double,
    @Column("created_at_epoch_ms") val createdAtEpochMs: Long,
    val rank: Int = 0,
)

data class RatingWithUsernameEntity(
    val id: Long? = null,
    val movieId: Long,
    val userId: Long,
    val directing: Double,
    val cinematography: Double,
    val acting: Double,
    val soundtrack: Double,
    val screenplay: Double,
    val createdAtEpochMs: Long,
    val username: String
)

@Repository
interface RatingDAO : CrudRepository<RatingEntity, Long> {
    fun findFirstByMovieId(movieId: Long): Optional<RatingEntity>
    fun findByMovieIdAndUserId(movieId: Long, userId: Long): List<RatingEntity>
    fun findByUserId(userId: Long): List<RatingEntity>
    fun findByUserIdOrderByRank(userId: Long): List<RatingEntity>
}
