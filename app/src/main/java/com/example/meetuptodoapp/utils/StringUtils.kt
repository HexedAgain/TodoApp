package com.example.meetuptodoapp.utils

import com.example.meetuptodoapp.domain.model.Todos
import com.example.meetuptodoapp.ui.model.UITodo
import java.sql.Timestamp
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun Todos.toUI(): List<UITodo> {
    return todos.map { todoItem ->
//        val time = Instant.ofEpochMilli(todoItem.timestamp).atZone(ZoneId.of("GMT"))
//        val formattedTime = time.toLocalDateTime().format(DateTimeFormatter.ofPattern("EE, dd MMMM yyyy HH:mm:ss"))
        UITodo(
            title = todoItem.title ?: "No title",
            description = todoItem.description ?: "No description",
            date = formatDate(todoItem.timestamp)
        )
    }
}

fun formatDate(timestamp: Long, includeHoursMinsSeconds: Boolean = true): String {
    val format = if (includeHoursMinsSeconds) "EE, dd MMMM yyyy HH:mm:ss" else "EE, dd MMMM yyyy"
    return Instant
        .ofEpochMilli(timestamp)
        .atZone(ZoneId.of("GMT"))
        .toLocalDateTime()
        .format(DateTimeFormatter.ofPattern(format))
}
