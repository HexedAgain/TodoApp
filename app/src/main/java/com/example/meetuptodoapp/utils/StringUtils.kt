package com.example.meetuptodoapp.utils

import com.example.meetuptodoapp.domain.model.Todos
import com.example.meetuptodoapp.ui.model.UITodo
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun Todos.toUI(): List<UITodo> {
    return todos.map { todoItem ->
//        val time = Instant.ofEpochMilli(todoItem.timestamp).atZone(ZoneId.of("GMT"))
//        val formattedTime = time.toLocalDateTime().format(DateTimeFormatter.ofPattern("EE, dd MMMM yyyy HH:mm:ss"))
        UITodo(
            title = todoItem.title.takeIf { it.isNotEmpty() } ?: "No title",
            description = todoItem.description.takeIf { it.isNotEmpty() } ?: "No description",
            date = formatDate(todoItem.timestamp)
        )
    }
}

fun formatDate(timestamp: Long, isVerbose: Boolean = true): String {
    val format = if (isVerbose) "EE, dd MMMM yyyy HH:mm:ss" else "EE, dd/MM/yyyy"
    return Instant
        .ofEpochMilli(timestamp)
        .atZone(ZoneId.of("GMT"))
        .toLocalDateTime()
        .format(DateTimeFormatter.ofPattern(format))
}

fun formatTime(timestamp: Long, hours: Int, mins: Int): String {
    // TODO maybe mix these up
    val time = Instant
        .ofEpochMilli(timestamp)
        .atZone(ZoneId.of("GMT"))
//        .plusHours(hours.toLong())
//        .plusMinutes(mins.toLong())
        .toLocalTime()
    return "${time.hour}:${time.minute}"
}