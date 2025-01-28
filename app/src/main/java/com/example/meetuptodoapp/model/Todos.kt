package com.example.meetuptodoapp.model

import kotlinx.serialization.Serializable

@Serializable
data class Todos(
    val todos: List<TodoItem>
) {
    @Serializable
    data class TodoItem(
        val title: String
    )
}