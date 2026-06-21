package org.ratelog

import arrow.core.Either
import arrow.core.raise.either
import java.util.Locale

data class TmdbId(val value: Int) {
    init {
        require(value > 0) { "TmdbId must be greater than 0" }
    }
}

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

enum class Status(val value: String) {
    RUMORED("Rumored"),
    PLANNED("Planned"),
    IN_PRODUCTION("In Production"),
    POST_PRODUCTION("Post Production"),
    RELEASED("Released"),
    CANCELED("Canceled"),
    RETURNING_SERIES("Returning Series"),
    ENDED("Ended"),
    PILOT("Pilot");

    companion object {
        fun fromValue(value: String): Status? = entries.find { it.value == value }
    }
}

data class Rank(val value: Int) {
    init {
        require(value > 0) { "Rank must be greater than 0" }
    }
}

data class SeasonNumber(val value: Int) {
    init {
        require(value > 0) { "SeasonNumber must be greater than 0" }
    }
}

data class Username(val value: String) {
    companion object {
        fun parse(value: String): Either<ParseError, Username> = either {
            if (!value.matches(Regex("^[a-zA-Z0-9_-]{3,50}$"))) {
                raise(ParseError.InvalidUsername)
            }
            Username(value)
        }
    }
}

data class Email(val value: String) {
    companion object {
        fun parse(value: String): Either<ParseError, Email> = either {
            if (!value.matches(Regex("^[\\w.-]+@[\\w.-]+\\.\\w{2,}$"))) {
                raise(ParseError.InvalidEmail)
            }
            Email(value)
        }
    }
}

data class Password(val value: String) {
    companion object {
        fun parse(value: String): Either<ParseError, Password> = either {
            if (!value.matches(Regex("^(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]).{8,}$"))) {
                raise(ParseError.InvalidPassword)
            }
            Password(value)
        }
    }
}

data class Score(val value: Double) {
    init {
        require(value in 1.0..10.0) { "Score must be between 1.0 and 10.0" }
    }
}

enum class Lang {
    en, de, es, fr, it, ja, pt, ru, zh;

    val locale: Locale = Locale.of(name)
}

data class Review(val value: String) {
    companion object {
        fun sanitize(raw: String): Review = Review(
            raw.replace(Regex("<[^>]*>"), "")
                .replace(Regex("&[^;]+;"), "")
                .trim()
                .take(1000)
        )
    }
}

sealed interface ParseError {
    data object InvalidUsername : ParseError
    data object InvalidEmail : ParseError
    data object InvalidPassword : ParseError
}
