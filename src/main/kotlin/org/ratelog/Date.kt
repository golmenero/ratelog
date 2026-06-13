package org.ratelog

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val shortDateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy").withZone(ZoneId.systemDefault())

fun Instant.toShortDateString(): String = shortDateFormatter.format(this)

fun Long.formatMs():String {
    val instant = Instant.ofEpochMilli(this)
    return shortDateFormatter.format(instant)
}

fun Long.formatSec():String {
    val instant = Instant.ofEpochSecond(this)
    return shortDateFormatter.format(instant)
}