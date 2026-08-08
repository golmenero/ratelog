package org.ratelog.import.letterboxd

import com.fasterxml.jackson.annotation.JsonProperty

data class LetterboxdEntry(
    @JsonProperty("Date") val date: String,
    @JsonProperty("Name") val name: String,
    @JsonProperty("Year") val year: Int?,
    @JsonProperty("Rating") val rating: Double,
    @JsonProperty("Letterboxd URI") val letterboxdUri: String,
)
