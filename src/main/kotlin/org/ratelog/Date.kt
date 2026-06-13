package org.ratelog

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val shortDateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy").withZone(ZoneId.systemDefault())


fun Long.toDateString():String {
    val instant = Instant.ofEpochMilli(this)
    return shortDateFormatter.format(instant)
}

fun String.toLocalDate(): LocalDate? =
    takeIf { isNotBlank() }?.let { LocalDate.parse(it) }