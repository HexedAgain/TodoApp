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
            id = todoItem.id,
            title = todoItem.title.takeIf { it.isNotEmpty() } ?: "No title",
            description = todoItem.description.takeIf { it.isNotEmpty() } ?: "No description",
            date = formatDate(todoItem.timestamp),
            completedTimestamp = todoItem.completedTime
        )
    }
}

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