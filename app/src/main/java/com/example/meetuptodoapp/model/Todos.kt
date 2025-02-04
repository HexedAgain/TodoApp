package com.example.meetuptodoapp.model

import kotlinx.serialization.Serializable

@Serializable
data class Todos(
    val todos: List<TodoItem>
) {
    companion object {
        fun default(): Todos {
            return Todos(todos = listOf())
        }
    }
}