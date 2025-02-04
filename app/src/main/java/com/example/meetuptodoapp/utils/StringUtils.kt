package com.example.meetuptodoapp.utils

import android.os.SystemClock
import com.example.meetuptodoapp.model.Todos
import com.example.meetuptodoapp.model.UITodo
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun Todos.toUI(): List<UITodo> {
    return todos.map { todoItem ->
        val time = Instant.ofEpochMilli(todoItem.timestamp).atZone(ZoneId.of("GMT"))
        val formattedTime = time.toLocalDateTime().format(DateTimeFormatter.ofPattern("EE, dd MMMM yyyy HH:mm:ss"))
        UITodo(
            title = todoItem.title ?: "No title",
            description = todoItem.description ?: "No description",
            date = formattedTime
        )
    }
}
