package org.ratelog.config

import org.springframework.data.annotation.Id
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.Optional

@Table("general_config")
data class GeneralConfigEntity(
    @Id val id: Long? = null,
    @Column("key") val key: String,
    @Column("value") val value: String,
    @Column("updated_at_epoch_ms") val updatedAtEpochMs: Long,
)

@Repository
interface GeneralConfigDAO : CrudRepository<GeneralConfigEntity, Long> {

    @Query("SELECT * FROM general_config WHERE key = :key")
    fun findByKey(key: String): Optional<GeneralConfigEntity>
}
