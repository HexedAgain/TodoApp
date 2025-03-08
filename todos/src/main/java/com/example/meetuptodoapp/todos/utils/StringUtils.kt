package com.example.meetuptodoapp.todos.utils

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

// FIXME - shouldn't be doing this here
fun formatDate(timestamp: Long, isVerbose: Boolean = true): String {
    val format = if (isVerbose) "EE, dd MMMM yyyy HH:mm" else "EE, dd/MM/yyyy"
    return Instant
        .ofEpochMilli(timestamp)
        .atZone(ZoneId.of("GMT"))
        .toLocalDateTime()
        .format(DateTimeFormatter.ofPattern(format))
}

fun formatTime(timestamp: Long): String {
    // TODO maybe mix these up
    val format = "HH:mm"
    val time = Instant
        .ofEpochMilli(timestamp)
        .atZone(ZoneId.of("GMT"))
        .toLocalTime()
        .format(DateTimeFormatter.ofPattern(format))
    return time
}