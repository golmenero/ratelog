package org.raterr.rating

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository

@Repository
class RatingRepositoryImpl(
    private val jdbcTemplate: JdbcTemplate
) {

    fun findAllWithoutUser(): List<Rating> {
        return jdbcTemplate.query(
            "SELECT * FROM ratings WHERE user_id IS NULL",
            ratingRowMapper
        )
    }

    private val ratingRowMapper = { rs: java.sql.ResultSet, _: Int ->
        Rating(
            id = rs.getLong("id").takeIf { rs.wasNull() },
            movieId = rs.getLong("movie_id"),
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
