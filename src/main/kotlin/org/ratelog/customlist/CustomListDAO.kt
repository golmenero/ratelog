package org.ratelog.customlist

import org.springframework.data.annotation.Id
import org.springframework.data.jdbc.repository.query.Modifying
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Table("custom_lists")
data class CustomListEntity(
    @Id val id: Long? = null,
    @Column("user_id") val userId: Long,
    val name: String,
    @Column("is_public") val isPublic: Boolean,
    @Column("created_at_epoch_ms") val createdAtEpochMs: Long
)

@Table("custom_list_items")
data class CustomListItemEntity(
    @Id val id: Long? = null,
    @Column("list_id") val listId: Long,
    @Column("tmdb_id") val tmdbId: Int,
    @Column("media_type") val mediaType: String,
    val position: Int,
    @Column("added_at_epoch_ms") val addedAtEpochMs: Long
)

@Repository
interface CustomListDAO : CrudRepository<CustomListEntity, Long> {
    @Query("SELECT * FROM custom_lists WHERE user_id = :userId ORDER BY created_at_epoch_ms DESC")
    fun findByUserId(userId: Long): List<CustomListEntity>

    @Query("SELECT * FROM custom_lists WHERE user_id = :userId AND is_public = true ORDER BY created_at_epoch_ms DESC")
    fun findPublicByUserId(userId: Long): List<CustomListEntity>

    @Query("SELECT * FROM custom_lists WHERE is_public = true ORDER BY created_at_epoch_ms DESC")
    fun findPublicLists(): List<CustomListEntity>

    @Query("SELECT COUNT(*) FROM custom_lists WHERE user_id = :userId")
    fun countByUserId(userId: Long): Int
}

@Repository
interface CustomListItemDAO : CrudRepository<CustomListItemEntity, Long> {
    @Query("SELECT * FROM custom_list_items WHERE list_id = :listId ORDER BY position ASC")
    fun findByListId(listId: Long): List<CustomListItemEntity>

    @Query("SELECT * FROM custom_list_items WHERE list_id = :listId AND tmdb_id = :tmdbId AND media_type = :mediaType")
    fun findByListIdAndTmdbIdAndMediaType(listId: Long, tmdbId: Int, mediaType: String): Optional<CustomListItemEntity>

    @Modifying
    @Query("DELETE FROM custom_list_items WHERE list_id = :listId")
    fun deleteByListId(listId: Long)
}
