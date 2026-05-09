package org.raterr

data class TmdbId(val value: Int)

enum class MediaType {
    movie, tvshow
}

data class UserId(val value: Long)