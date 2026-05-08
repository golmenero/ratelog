package org.raterr.tvrating

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository

@Repository
class TvRatingRepositoryImpl(
    private val jdbcTemplate: JdbcTemplate
) {

    private val tvRatingRowMapper = { rs: java.sql.ResultSet, _: Int ->
        TvRating(
            id = rs.getLong("id").takeIf { rs.wasNull() },
            tvShowId = rs.getLong("tv_show_id"),
            userId = rs.getLong("user_id"),
            directing = rs.getDouble("directing"),
            cinematography = rs.getDouble("cinematography"),
            acting = rs.getDouble("acting"),
            soundtrack = rs.getDouble("soundtrack"),
            screenplay = rs.getDouble("screenplay"),
            createdAtEpochMs = rs.getLong("created_at_epoch_ms")
        )
    }
}
