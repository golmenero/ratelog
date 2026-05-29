package org.ratelog

data class TmdbId(val value: Int)

data class Title(val value: String)

data class Overview(val value: String)

data class Url(val value: String)

enum class MediaType {
    movie, tvshow
}

enum class Genre(val value: String) {
    ACTION("Action"),
    ADVENTURE("Adventure"),
    ANIMATION("Animation"),
    COMEDY("Comedy"),
    CRIME("Crime"),
    DOCUMENTARY("Documentary"),
    DRAMA("Drama"),
    FAMILY("Family"),
    FANTASY("Fantasy"),
    HISTORY("History"),
    HORROR("Horror"),
    MUSIC("Music"),
    MYSTERY("Mystery"),
    ROMANCE("Romance"),
    SCIENCE_FICTION("Science Fiction"),
    TV_MOVIE("TV Movie"),
    THRILLER("Thriller"),
    WAR("War"),
    WESTERN("Western"),
    ACTION_ADVENTURE("Action & Adventure"),
    KIDS("Kids"),
    NEWS("News"),
    REALITY("Reality"),
    SCI_FI_FANTASY("Sci-Fi & Fantasy"),
    SOAP("Soap"),
    TALK("Talk"),
    WAR_POLITICS("War & Politics");

    companion object {
        fun fromValue(value: String): Genre? = entries.find { it.value == value }
    }
}

data class Rank(val value: Int)

data class SeasonNumber(val value: Int)

data class Username(val value: String)

data class Email(val value: String)

data class Password(val value: String)

data class Score(val value: Double) {
    init {
        require(value in 1.0..10.0) { "Score must be between 1.0 and 10.0" }
    }
}
