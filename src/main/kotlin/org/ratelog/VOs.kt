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

enum class Genre(val tmdbId: Int) {
    ACTION(28),
    ADVENTURE(12),
    ANIMATION(16),
    COMEDY(35),
    CRIME(80),
    DOCUMENTARY(99),
    DRAMA(18),
    FAMILY(10751),
    FANTASY(14),
    HISTORY(36),
    HORROR(27),
    MUSIC(10402),
    MYSTERY(9648),
    ROMANCE(10749),
    SCIENCE_FICTION(878),
    TV_MOVIE(10770),
    THRILLER(53),
    WAR(10752),
    WESTERN(37),
    ACTION_ADVENTURE(10759),
    KIDS(10762),
    NEWS(10763),
    REALITY(10764),
    SCI_FI_FANTASY(10765),
    SOAP(10766),
    TALK(10767),
    WAR_POLITICS(10768);

    companion object {
        fun fromTmdbId(tmdbId: Int): Genre? = entries.find { it.tmdbId == tmdbId }
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

    companion object {
        fun parse(value: String): Lang = entries.find { it.name == value } ?: en
    }
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
