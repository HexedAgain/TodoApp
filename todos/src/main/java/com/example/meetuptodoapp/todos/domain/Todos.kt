package com.example.meetuptodoapp.todos.domain

import kotlinx.serialization.Serializable

@Serializable
data class Todos(
    val isMigrated: Boolean = false,
    val todos: List<TodoItem>
) {
    companion object {
        fun default(): Todos {
            return Todos(todos = listOf())
        }
    }
}