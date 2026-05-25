package org.raterr

data class TmdbId(val value: Int)

data class Title(val value: String)

data class Overview(val value: String)

data class Url(val value: String)

enum class MediaType {
    movie, tvshow
}

enum class Genre {
    Action,
    Adventure,
    Animation,
    Comedy,
    Crime,
    Documentary,
    Drama,
    Family,
    Fantasy,
    History,
    Horror,
    Music,
    Mystery,
    Romance,
    ScienceFiction,
    TvMovie,
    Thriller,
    War,
    Western,
    ActionAdventure,
    Kids,
    News,
    Reality,
    SciFiFantasy,
    Soap,
    Talk,
    WarPolitics
}

data class Username(val value: String)

data class Email(val value: String)

data class Password(val value: String)